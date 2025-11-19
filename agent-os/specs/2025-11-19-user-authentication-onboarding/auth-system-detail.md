# 상세 기술 설계: Authentication System (인증 시스템)

## 1. 개요

Memoir 안드로이드 앱의 Authentication System은 **Kakao OAuth 2.0** 기반의 사용자 인증 및 세션 관리를 담당합니다. 한국 시장의 특성상 Kakao는 가장 자연스러운 인증 수단이며, 장기 세션 지원으로 모바일 앱의 UX를 최적화합니다.

### 핵심 특성
- **단일 인증 제공자**: Kakao 로그인만 지원 (Google, Naver 미포함)
- **토큰 기반 세션**: JWT Access Token (30일) + Refresh Token (90일) with rotation
- **한국 특화**: Kakao SDK, KST 타임존, 한국어 에러 메시지
- **보안**: 토큰 암호화 저장 (Android Keystore), HTTPS/TLS 1.3 강제

---

## 2. Kakao OAuth 토큰 교환 흐름

### 2.1 전체 시퀀스 다이어그램

```
Android App                     Kakao Login SDK              Memoir Backend              Kakao API
    |                                  |                           |                       |
    |-- 1. Login button tap ---------> |                           |                       |
    |                                  |-- 2. Show Kakao WebView ---|                       |
    |                                  |    (User enters credentials) |                       |
    |                                  |                           |-- 3. Request Auth Code  |
    |                                  |                           |--- (Verify + issue) ---|
    |                                  |<-- 4. Receive Auth Code -----|                       |
    |<-- 5. Return Auth Code ---------|                           |                       |
    |                                  |                           |                       |
    |-- 6. POST /api/v1/auth/kakao --> |                           |                       |
    |    { kakao_oauth_code }          |                           |                       |
    |                                  |                           |-- 7. POST /oauth/token |
    |                                  |                           |     (Server-to-server) |
    |                                  |                           |<-- 8. access_token ----|
    |                                  |                           |      + kakao_id        |
    |                                  |                           |                       |
    |                                  |                           |-- 9. Check if user exists
    |                                  |                           |    (Query by kakao_id)
    |                                  |                           |                       |
    |<-- 10. Response w/ JWT tokens ---<-------------------------- |                       |
    |     { access_token, refresh_token, user_exists, ...}        |                       |
    |                                                             |                       |
    |-- 11. Store tokens in DataStore (encrypted) -->             |                       |
    |                                                             |                       |
```

### 2.2 Kakao 인증 코드 획득 프로세스

#### Android 클라이언트 구현

```kotlin
// AuthenticationViewModel.kt
class AuthenticationViewModel(
    private val kakaoAuthRepository: KakaoAuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun initiateKakaoLogin(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Step 1: Kakao SDK를 통해 인증 코드 획득
                val authCode = kakaoAuthRepository.getKakaoAuthCode(context)

                if (authCode != null) {
                    // Step 2: 백엔드로 토큰 교환 요청
                    exchangeTokens(authCode)
                } else {
                    _authState.value = AuthState.Error("카카오 인증 실패: 인증 코드를 받지 못했습니다")
                }
            } catch (e: KakaoAuthException) {
                handleKakaoError(e)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("예상치 못한 오류가 발생했습니다")
            }
        }
    }

    private suspend fun exchangeTokens(kakaoAuthCode: String) {
        try {
            val response = sessionManager.exchangeKakaoCode(kakaoAuthCode)

            // Step 3: 토큰 및 사용자 정보 저장
            sessionManager.saveTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                accessTokenExpiresAt = System.currentTimeMillis() + (30L * 24 * 3600 * 1000)
            )

            // Step 4: 사용자 상태 판별
            if (response.userExists) {
                if (response.onboardingCompleted) {
                    _authState.value = AuthState.AuthenticatedAndOnboarded
                } else {
                    _authState.value = AuthState.AuthenticatedRequiresOnboarding
                }
            } else {
                _authState.value = AuthState.NewUserRequiresOnboarding
            }

            // 분석 이벤트
            analyticsService.logEvent("kakao_login_success", mapOf(
                "user_new" to !response.userExists,
                "onboarding_required" to !response.onboardingCompleted
            ))
        } catch (e: Exception) {
            _authState.value = AuthState.Error("토큰 교환 실패: ${e.message}")
            analyticsService.logEvent("kakao_login_failure", mapOf(
                "error_type" to e::class.simpleName
            ))
        }
    }

    private fun handleKakaoError(exception: KakaoAuthException) {
        val message = when (exception.errorCode) {
            "E_CANCELLED_OPERATION" -> "카카오 로그인이 취소되었습니다"
            "E_NETWORK" -> "네트워크 연결을 확인해주세요"
            "E_SERVICE_CONFIGURATION" -> "카카오 로그인 설정에 문제가 있습니다"
            else -> "카카오 로그인 중 오류가 발생했습니다"
        }
        _authState.value = AuthState.Error(message)
    }
}

// KakaoAuthRepository.kt
class KakaoAuthRepository(private val context: Context) {

    suspend fun getKakaoAuthCode(context: Context): String? = withContext(Dispatchers.IO) {
        return@withContext suspendCancellableCoroutine { continuation ->
            // Kakao SDK를 통해 로그인 (OAuth 2.0 Authorization Code Flow)
            UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
                if (error != null) {
                    continuation.resume(null)
                } else if (token != null) {
                    // token.accessToken은 Kakao Access Token이지만,
                    // 백엔드에서는 Authorization Code가 필요하므로
                    // Kakao SDK의 인증 코드를 별도로 추출해야 함
                    continuation.resume(extractAuthCode(token))
                }
            }
        }
    }

    private fun extractAuthCode(token: OAuthToken): String? {
        // 실제 구현에서는 Kakao SDK가 반환하는 토큰에서
        // Authorization Code를 추출하거나, 별도 API 호출을 통해 획득
        return token.accessToken // 임시 - 실제로는 Authorization Code 필요
    }
}
```

#### Kakao Login 버튼 UI (Jetpack Compose)

```kotlin
// AuthenticationScreen.kt
@Composable
fun AuthenticationScreen(
    viewModel: AuthenticationViewModel,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToMainApp: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 앱 로고 및 설명
        Image(
            painter = painterResource(id = R.drawable.memoir_logo),
            contentDescription = "Memoir Logo",
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "매주 한 걸음 더 성장하는\n경험을 시작해보세요",
            style = MaterialTheme.typography.headlineMedium.copy(
                textAlign = TextAlign.Center
            ),
            color = MemoirTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(64.dp))

        // Kakao 로그인 버튼
        KakaoLoginButton(
            onClick = { viewModel.initiateKakaoLogin(context) },
            isLoading = authState is AuthState.Loading,
            modifier = Modifier.fillMaxWidth()
        )

        // 에러 메시지 표시
        when (authState) {
            is AuthState.Error -> {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
            AuthState.AuthenticatedAndOnboarded -> {
                LaunchedEffect(Unit) { onNavigateToMainApp() }
            }
            is AuthState.AuthenticatedRequiresOnboarding,
            is AuthState.NewUserRequiresOnboarding -> {
                LaunchedEffect(Unit) { onNavigateToOnboarding() }
            }
            else -> {}
        }

        // 약관 동의
        Spacer(modifier = Modifier.height(32.dp))
        TermsAndPrivacyFooter()
    }
}

@Composable
fun KakaoLoginButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier.height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFE812), // Kakao Yellow
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.Black,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Image(
            painter = painterResource(id = R.drawable.kakao_icon),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "카카오로 시작하기",
            style = MaterialTheme.typography.labelLarge
        )
    }
}
```

### 2.3 백엔드 토큰 교환 구현

#### Express.js 엔드포인트

```typescript
// src/routes/auth.routes.ts
import express, { Request, Response } from 'express';
import { AuthController } from '../controllers/auth.controller';
import { validateKakaoTokenRequest } from '../middleware/validation';
import { rateLimitAuth } from '../middleware/rate-limit';

const router = express.Router();
const authController = new AuthController();

/**
 * POST /api/v1/auth/kakao
 * Kakao OAuth Authorization Code를 JWT 토큰 쌍으로 교환
 *
 * Request: { kakao_oauth_code: string }
 * Response: {
 *   access_token: string (JWT),
 *   refresh_token: string (JWT),
 *   user_exists: boolean,
 *   onboarding_completed: boolean
 * }
 */
router.post(
  '/kakao',
  rateLimitAuth,
  validateKakaoTokenRequest,
  authController.exchangeKakaoToken.bind(authController)
);

export default router;

// src/controllers/auth.controller.ts
import axios from 'axios';
import { TokenService } from '../services/token.service';
import { UserService } from '../services/user.service';
import { Logger } from '../utils/logger';

export class AuthController {
  constructor(
    private tokenService: TokenService,
    private userService: UserService,
    private logger: Logger
  ) {}

  async exchangeKakaoToken(req: Request, res: Response) {
    try {
      const { kakao_oauth_code } = req.body;

      // Step 1: Kakao API로 Authorization Code 검증 및 Access Token 획득
      const kakaoTokenResponse = await this.exchangeCodeForKakaoToken(
        kakao_oauth_code
      );

      if (!kakaoTokenResponse.access_token) {
        this.logger.warn('Failed to get Kakao access token', {
          code: kakao_oauth_code.substring(0, 10) // 로그에는 부분만 표시
        });
        return res.status(400).json({
          error: {
            code: 'INVALID_KAKAO_CODE',
            message: '유효하지 않은 카카오 인증 코드입니다'
          }
        });
      }

      // Step 2: Kakao API에서 사용자 정보 조회
      const kakaoUserInfo = await this.fetchKakaoUserInfo(
        kakaoTokenResponse.access_token
      );

      if (!kakaoUserInfo.id) {
        return res.status(502).json({
          error: {
            code: 'KAKAO_API_ERROR',
            message: '카카오 서비스 연결에 실패했습니다. 잠시 후 다시 시도해주세요'
          }
        });
      }

      const kakaoId = String(kakaoUserInfo.id);

      // Step 3: 기존 사용자 조회 또는 새 사용자 생성
      let user = await this.userService.findByKakaoId(kakaoId);
      const userExists = !!user;

      if (!user) {
        // 새 사용자 생성
        user = await this.userService.createUser({
          kakao_id: kakaoId,
          kakao_nickname: kakaoUserInfo.kakao_account?.profile?.nickname || 'Unknown',
          kakao_profile_image: kakaoUserInfo.kakao_account?.profile?.profile_image_url || null,
          status: 'active'
        });
        this.logger.info('New user created', { user_id: user.id, kakao_id: kakaoId });
      } else {
        // 기존 사용자 업데이트
        await this.userService.updateLastLogin(user.id);
      }

      // Step 4: JWT 토큰 쌍 생성
      const { accessToken, refreshToken, expiresAt } =
        await this.tokenService.generateTokenPair(user.id);

      // Step 5: Refresh Token을 해시하여 저장 (DB 유출 시 보안)
      await this.tokenService.saveRefreshToken(user.id, refreshToken);

      // Step 6: 온보딩 완료 상태 확인
      const onboardingCompleted = await this.userService.isOnboardingCompleted(
        user.id
      );

      // 분석 이벤트 로깅
      this.logger.info('Kakao login successful', {
        user_id: user.id,
        user_new: !userExists,
        onboarding_completed: onboardingCompleted
      });

      // Response
      return res.status(200).json({
        access_token: accessToken,
        refresh_token: refreshToken,
        user_exists: userExists,
        onboarding_completed: onboardingCompleted,
        expires_in: Math.floor((expiresAt - Date.now()) / 1000) // 초 단위
      });

    } catch (error) {
      this.logger.error('Token exchange failed', { error: error.message });

      if (error.code === 'ECONNREFUSED') {
        return res.status(502).json({
          error: {
            code: 'SERVICE_UNAVAILABLE',
            message: '서비스를 이용할 수 없습니다. 잠시 후 다시 시도해주세요'
          }
        });
      }

      return res.status(500).json({
        error: {
          code: 'INTERNAL_ERROR',
          message: '인증 처리 중 오류가 발생했습니다'
        }
      });
    }
  }

  /**
   * Kakao REST API: Authorization Code를 Access Token으로 교환
   * https://kauth.kakao.com/oauth/token
   */
  private async exchangeCodeForKakaoToken(authCode: string) {
    const params = new URLSearchParams({
      grant_type: 'authorization_code',
      client_id: process.env.KAKAO_CLIENT_ID!,
      client_secret: process.env.KAKAO_CLIENT_SECRET!,
      code: authCode,
      redirect_uri: process.env.KAKAO_REDIRECT_URI!
    });

    try {
      const response = await axios.post(
        'https://kauth.kakao.com/oauth/token',
        params,
        {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          },
          timeout: 5000
        }
      );

      return response.data;
    } catch (error) {
      throw new Error(`Kakao token exchange failed: ${error.message}`);
    }
  }

  /**
   * Kakao User API: Access Token으로 사용자 정보 조회
   * GET https://kapi.kakao.com/v2/user/me
   */
  private async fetchKakaoUserInfo(kakaoAccessToken: string) {
    try {
      const response = await axios.get('https://kapi.kakao.com/v2/user/me', {
        headers: {
          Authorization: `Bearer ${kakaoAccessToken}`,
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        timeout: 5000
      });

      return response.data;
    } catch (error) {
      throw new Error(`Failed to fetch Kakao user info: ${error.message}`);
    }
  }
}
```

---

## 3. JWT 토큰 생성 & 검증

### 3.1 토큰 구조 및 스펙

#### Access Token
- **만료 기간**: 30일 (2,592,000초)
- **알고리즘**: HS256 (HMAC SHA-256)
- **페이로드**:
  ```json
  {
    "sub": "user-uuid",           // 주체 (사용자 ID)
    "iat": 1700000000,             // 발급 시간
    "exp": 1702592000,             // 만료 시간 (30일 후)
    "type": "access",              // 토큰 타입
    "kakao_id": "1234567890",       // Kakao ID (선택사항)
    "version": "1"                  // 토큰 버전 (이후 호환성)
  }
  ```

#### Refresh Token
- **만료 기간**: 90일 (7,776,000초)
- **알고리즘**: HS256
- **페이로드**:
  ```json
  {
    "sub": "user-uuid",
    "iat": 1700000000,
    "exp": 1707792000,              // 만료 시간 (90일 후)
    "type": "refresh",
    "rotation_count": 1,            // 로테이션 횟수
    "family": "token-family-uuid"   // 토큰 Family ID (로테이션 추적)
  }
  ```

### 3.2 토큰 생성 서비스

```typescript
// src/services/token.service.ts
import jwt from 'jsonwebtoken';
import crypto from 'crypto';
import { prisma } from '../db/prisma';

export class TokenService {
  private readonly accessTokenSecret = process.env.JWT_ACCESS_SECRET!;
  private readonly refreshTokenSecret = process.env.JWT_REFRESH_SECRET!;

  // 토큰 만료 시간 (초 단위)
  private readonly accessTokenExpiry = 30 * 24 * 60 * 60; // 30일
  private readonly refreshTokenExpiry = 90 * 24 * 60 * 60; // 90일

  /**
   * Access Token + Refresh Token 쌍 생성
   */
  async generateTokenPair(
    userId: string,
    options?: { kakaoId?: string; familyId?: string }
  ) {
    const now = Math.floor(Date.now() / 1000);
    const accessTokenExpiresAt = now + this.accessTokenExpiry;
    const refreshTokenExpiresAt = now + this.refreshTokenExpiry;

    // Token Family ID: 같은 로그인 세션의 토큰들을 그룹화
    const familyId = options?.familyId || crypto.randomUUID();

    // Access Token 생성
    const accessToken = jwt.sign(
      {
        sub: userId,
        type: 'access',
        kakao_id: options?.kakaoId,
        version: '1'
      },
      this.accessTokenSecret,
      {
        algorithm: 'HS256',
        expiresIn: this.accessTokenExpiry,
        issuer: 'memoir-auth',
        audience: 'memoir-api'
      }
    );

    // Refresh Token 생성
    const refreshToken = jwt.sign(
      {
        sub: userId,
        type: 'refresh',
        family: familyId,
        rotation_count: 1
      },
      this.refreshTokenSecret,
      {
        algorithm: 'HS256',
        expiresIn: this.refreshTokenExpiry,
        issuer: 'memoir-auth',
        audience: 'memoir-api'
      }
    );

    return {
      accessToken,
      refreshToken,
      accessTokenExpiresAt: new Date(accessTokenExpiresAt * 1000),
      refreshTokenExpiresAt: new Date(refreshTokenExpiresAt * 1000),
      expiresAt: Date.now() + (this.accessTokenExpiry * 1000)
    };
  }

  /**
   * Refresh Token을 해시하여 DB에 저장
   * (DB 유출 시에도 토큰을 직접 사용할 수 없도록)
   */
  async saveRefreshToken(
    userId: string,
    refreshToken: string,
    familyId?: string
  ) {
    const decoded = jwt.decode(refreshToken) as any;
    const tokenHash = crypto
      .createHash('sha256')
      .update(refreshToken)
      .digest('hex');

    const expiresAt = new Date(decoded.exp * 1000);

    // 기존 refresh token 무효화 (다른 family는 유지)
    await prisma.refreshTokenStore.deleteMany({
      where: {
        user_id: userId,
        family: decoded.family
      }
    });

    // 새 token 저장
    return prisma.refreshTokenStore.create({
      data: {
        user_id: userId,
        token_hash: tokenHash,
        family: decoded.family,
        rotation_count: decoded.rotation_count,
        expires_at: expiresAt,
        created_at: new Date()
      }
    });
  }

  /**
   * Token 검증
   */
  verifyAccessToken(token: string): { valid: boolean; payload?: any; error?: string } {
    try {
      const payload = jwt.verify(token, this.accessTokenSecret, {
        algorithms: ['HS256'],
        issuer: 'memoir-auth',
        audience: 'memoir-api'
      });

      if (payload.type !== 'access') {
        return { valid: false, error: 'Invalid token type' };
      }

      return { valid: true, payload };
    } catch (error) {
      if (error.name === 'TokenExpiredError') {
        return { valid: false, error: 'Token expired' };
      }
      return { valid: false, error: error.message };
    }
  }

  /**
   * Refresh Token 검증 (DB에 저장된 해시 비교)
   */
  async verifyRefreshToken(
    token: string,
    userId: string
  ): Promise<{ valid: boolean; payload?: any; error?: string }> {
    try {
      const payload = jwt.verify(token, this.refreshTokenSecret, {
        algorithms: ['HS256'],
        issuer: 'memoir-auth',
        audience: 'memoir-api'
      }) as any;

      if (payload.type !== 'refresh') {
        return { valid: false, error: 'Invalid token type' };
      }

      if (payload.sub !== userId) {
        return { valid: false, error: 'User mismatch' };
      }

      // DB에서 token 확인
      const tokenHash = crypto
        .createHash('sha256')
        .update(token)
        .digest('hex');

      const storedToken = await prisma.refreshTokenStore.findFirst({
        where: {
          user_id: userId,
          token_hash: tokenHash,
          family: payload.family
        }
      });

      if (!storedToken) {
        return { valid: false, error: 'Token not found in store' };
      }

      // 만료 확인
      if (storedToken.expires_at < new Date()) {
        await prisma.refreshTokenStore.delete({ where: { id: storedToken.id } });
        return { valid: false, error: 'Token expired' };
      }

      return { valid: true, payload };
    } catch (error) {
      return { valid: false, error: error.message };
    }
  }
}
```

### 3.3 토큰 검증 미들웨어

```typescript
// src/middleware/auth.middleware.ts
import { Request, Response, NextFunction } from 'express';
import { TokenService } from '../services/token.service';
import { Logger } from '../utils/logger';

export interface AuthenticatedRequest extends Request {
  user?: {
    id: string;
    kakaoId?: string;
  };
}

export function createAuthMiddleware(
  tokenService: TokenService,
  logger: Logger
) {
  return (req: AuthenticatedRequest, res: Response, next: NextFunction) => {
    try {
      // Authorization 헤더에서 토큰 추출
      const authHeader = req.headers.authorization;

      if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({
          error: {
            code: 'UNAUTHORIZED',
            message: '인증 토큰이 필요합니다'
          }
        });
      }

      const token = authHeader.substring(7); // "Bearer " 제거

      // 토큰 검증
      const result = tokenService.verifyAccessToken(token);

      if (!result.valid) {
        // 토큰 만료인 경우 별도 처리 가능
        if (result.error === 'Token expired') {
          return res.status(401).json({
            error: {
              code: 'TOKEN_EXPIRED',
              message: '토큰이 만료되었습니다. 다시 로그인해주세요'
            }
          });
        }

        return res.status(401).json({
          error: {
            code: 'INVALID_TOKEN',
            message: '유효하지 않은 토큰입니다'
          }
        });
      }

      // 검증된 사용자 정보를 request에 설정
      req.user = {
        id: result.payload.sub,
        kakaoId: result.payload.kakao_id
      };

      // 토큰 만료 시간이 임박한 경우 클라이언트에 알림
      const expiresIn = result.payload.exp - Math.floor(Date.now() / 1000);
      const EXPIRY_WARNING_THRESHOLD = 24 * 60 * 60; // 24시간

      if (expiresIn < EXPIRY_WARNING_THRESHOLD) {
        res.set('X-Token-Expires-Soon', 'true');
        res.set('X-Token-Expires-In', String(expiresIn));
      }

      next();
    } catch (error) {
      logger.error('Auth middleware error', { error: error.message });
      return res.status(500).json({
        error: {
          code: 'INTERNAL_ERROR',
          message: '인증 처리 중 오류가 발생했습니다'
        }
      });
    }
  };
}
```

---

## 4. 토큰 로테이션 메커니즘

### 4.1 Refresh Token 로테이션 프로세스

토큰 로테이션은 **refresh token의 자동 갱신**으로, 탈취된 토큰의 수명을 제한합니다.

#### 로테이션 흐름도

```
Client              Memoir Backend          Token Store
  |                      |                      |
  |-- POST /refresh ----> |                      |
  |  { refresh_token }    |                      |
  |                       |-- Verify Token ----> |
  |                       |<-- Token Valid ----  |
  |                       |                      |
  |                       |-- Invalidate Old ---|
  |                       |    Refresh Token    |
  |                       |                      |
  |                       |-- Generate New ---> |
  |                       |    Token Pair       |
  |                       |                      |
  |<-- New Tokens --------|                      |
  |  (access + refresh)   |                      |
  |                       |
```

### 4.2 로테이션 구현

```typescript
// src/controllers/auth.controller.ts (계속)

async refreshTokens(req: Request, res: Response) {
  try {
    const { refresh_token } = req.body;

    if (!refresh_token) {
      return res.status(400).json({
        error: {
          code: 'MISSING_TOKEN',
          message: '갱신 토큰이 필요합니다'
        }
      });
    }

    // Step 1: 기존 refresh token 검증
    let decoded: any;
    try {
      decoded = jwt.verify(refresh_token, this.refreshTokenSecret, {
        algorithms: ['HS256']
      }) as any;
    } catch (error) {
      if (error.name === 'TokenExpiredError') {
        return res.status(401).json({
          error: {
            code: 'REFRESH_TOKEN_EXPIRED',
            message: '갱신 토큰이 만료되었습니다. 다시 로그인해주세요'
          }
        });
      }
      return res.status(401).json({
        error: {
          code: 'INVALID_REFRESH_TOKEN',
          message: '유효하지 않은 갱신 토큰입니다'
        }
      });
    }

    const userId = decoded.sub;
    const familyId = decoded.family;

    // Step 2: DB에서 refresh token 확인 (중복 사용 감지)
    const verification = await this.tokenService.verifyRefreshToken(
      refresh_token,
      userId
    );

    if (!verification.valid) {
      // 토큰이 DB에 없으면 탈취 가능성 → 모든 토큰 무효화
      this.logger.warn('Suspicious refresh token attempt', {
        user_id: userId,
        family: familyId,
        reason: verification.error
      });

      await this.tokenService.revokeAllTokens(userId);

      return res.status(409).json({
        error: {
          code: 'TOKEN_ROTATION_CONFLICT',
          message: '토큰 갱신 실패. 보안상 모든 세션이 종료되었습니다. 다시 로그인해주세요'
        }
      });
    }

    // Step 3: 새 토큰 쌍 생성
    const newTokens = await this.tokenService.generateTokenPair(
      userId,
      {
        kakaoId: decoded.kakao_id,
        familyId: familyId // 같은 family 유지
      }
    );

    // Step 4: 새 refresh token 저장 (자동으로 이전 토큰 무효화)
    await this.tokenService.saveRefreshToken(
      userId,
      newTokens.refreshToken,
      familyId
    );

    this.logger.info('Token rotation successful', {
      user_id: userId,
      family: familyId
    });

    return res.status(200).json({
      access_token: newTokens.accessToken,
      refresh_token: newTokens.refreshToken,
      expires_in: Math.floor((newTokens.expiresAt - Date.now()) / 1000)
    });

  } catch (error) {
    this.logger.error('Token refresh failed', { error: error.message });
    return res.status(500).json({
      error: {
        code: 'INTERNAL_ERROR',
        message: '토큰 갱신 중 오류가 발생했습니다'
      }
    });
  }
}

/**
 * 동시 요청 충돌 처리 (Race Condition)
 * 여러 기기에서 동시에 refresh 요청 시 처리
 */
private async handleRaceCondition(
  userId: string,
  familyId: string
): Promise<boolean> {
  // Distributed Lock 사용 (Redis/DB)
  const lockKey = `refresh_lock:${userId}:${familyId}`;
  const lockValue = crypto.randomUUID();
  const lockTtl = 5000; // 5초

  try {
    // 잠금 획득 시도
    const acquired = await this.lockService.tryAcquire(lockKey, lockValue, lockTtl);

    if (!acquired) {
      // 잠금 실패 = 다른 요청이 진행 중
      // 기존 로테이션이 완료될 때까지 대기
      await new Promise(resolve => setTimeout(resolve, 1000));
      return false;
    }

    // 잠금 획득 성공 → 로테이션 진행
    return true;
  } finally {
    // 잠금 해제
    await this.lockService.release(lockKey, lockValue);
  }
}
```

### 4.3 Android 클라이언트에서 자동 토큰 갱신

```kotlin
// SessionManager.kt
class SessionManager(
  private val dataStore: DataStore<Preferences>,
  private val apiService: MemoirApiService
) {
  companion object {
    private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    private val ACCESS_TOKEN_EXPIRES_AT_KEY = longPreferencesKey("access_token_expires_at")
  }

  /**
   * 토큰이 곧 만료될 예정이면 자동 갱신
   */
  suspend fun ensureValidAccessToken(): String? {
    return withContext(Dispatchers.IO) {
      val data = dataStore.data.first()
      val accessToken = data[ACCESS_TOKEN_KEY]
      val expiresAt = data[ACCESS_TOKEN_EXPIRES_AT_KEY] ?: 0L
      val now = System.currentTimeMillis()

      // 만료까지 1시간 미만 남으면 갱신
      val refreshThreshold = 60 * 60 * 1000 // 1시간

      if (expiresAt - now < refreshThreshold) {
        val refreshToken = data[REFRESH_TOKEN_KEY]
        if (refreshToken != null) {
          return@withContext refreshAccessToken(refreshToken)
        }
      }

      accessToken
    }
  }

  /**
   * Refresh Token으로 새 Access Token 획득
   */
  suspend fun refreshAccessToken(refreshToken: String): String? {
    return try {
      val response = apiService.refreshToken(
        RefreshTokenRequest(refreshToken)
      )

      // 새 토큰 저장
      dataStore.edit { preferences ->
        preferences[ACCESS_TOKEN_KEY] = response.accessToken
        preferences[REFRESH_TOKEN_KEY] = response.refreshToken
        preferences[ACCESS_TOKEN_EXPIRES_AT_KEY] =
          System.currentTimeMillis() + (30L * 24 * 3600 * 1000)
      }

      analyticsService.logEvent("session_token_refreshed", mapOf(
        "rotation" to true
      ))

      response.accessToken
    } catch (e: Exception) {
      // 갱신 실패 → 재로그인 필요
      dataStore.edit { preferences ->
        preferences.clear()
      }
      null
    }
  }

  /**
   * 네트워크 요청 중 401 응답 시 자동 재시도
   */
  fun setupTokenRefreshInterceptor(): OkHttpClient {
    val httpClient = OkHttpClient.Builder()
      .addInterceptor { chain ->
        val request = chain.request()
        var response = chain.proceed(request)

        // 401 응답 처리
        if (response.code == 401) {
          val refreshToken = runBlocking {
            dataStore.data.first()[REFRESH_TOKEN_KEY]
          }

          if (refreshToken != null) {
            try {
              val newAccessToken = runBlocking {
                refreshAccessToken(refreshToken)
              }

              if (newAccessToken != null) {
                // 새 토큰으로 원래 요청 재시도
                val newRequest = request.newBuilder()
                  .header("Authorization", "Bearer $newAccessToken")
                  .build()

                response.close()
                response = chain.proceed(newRequest)
              }
            } catch (e: Exception) {
              // 재시도 실패 → 에러 반환
            }
          }
        }

        response
      }
      .build()

    return httpClient
  }
}

// NetworkModule.kt (Hilt Dependency Injection)
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

  @Provides
  @Singleton
  fun provideOkHttpClient(sessionManager: SessionManager): OkHttpClient {
    return sessionManager.setupTokenRefreshInterceptor()
  }

  @Provides
  @Singleton
  fun provideMemoirApiService(okHttpClient: OkHttpClient): MemoirApiService {
    val retrofit = Retrofit.Builder()
      .baseUrl(BuildConfig.API_BASE_URL)
      .client(okHttpClient)
      .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
      .build()

    return retrofit.create(MemoirApiService::class.java)
  }
}
```

---

## 5. 세션 관리 전략

### 5.1 클라이언트 측 (Android)

#### DataStore에 토큰 저장

```kotlin
// src/data/local/TokenPreferences.kt
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenPreferences @Inject constructor(
  private val dataStore: DataStore<Preferences>
) {
  companion object {
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    val ACCESS_TOKEN_EXPIRES_AT = longPreferencesKey("access_token_expires_at")
    val REFRESH_TOKEN_EXPIRES_AT = longPreferencesKey("refresh_token_expires_at")
    val USER_ID = stringPreferencesKey("user_id")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
  }

  val accessToken: Flow<String?> = dataStore.data.map { preferences ->
    preferences[ACCESS_TOKEN]
  }

  val refreshToken: Flow<String?> = dataStore.data.map { preferences ->
    preferences[REFRESH_TOKEN]
  }

  val onboardingCompleted: Flow<Boolean> = dataStore.data.map { preferences ->
    preferences[ONBOARDING_COMPLETED] ?: false
  }

  /**
   * 토큰 저장 (암호화됨 - DataStore는 기본적으로 암호화)
   */
  suspend fun saveTokens(
    accessToken: String,
    refreshToken: String,
    accessTokenExpiresAt: Long,
    refreshTokenExpiresAt: Long
  ) {
    dataStore.edit { preferences ->
      preferences[ACCESS_TOKEN] = accessToken
      preferences[REFRESH_TOKEN] = refreshToken
      preferences[ACCESS_TOKEN_EXPIRES_AT] = accessTokenExpiresAt
      preferences[REFRESH_TOKEN_EXPIRES_AT] = refreshTokenExpiresAt
    }
  }

  suspend fun saveOnboardingCompleted() {
    dataStore.edit { preferences ->
      preferences[ONBOARDING_COMPLETED] = true
    }
  }

  /**
   * 로그아웃: 모든 토큰 제거
   */
  suspend fun clearTokens() {
    dataStore.edit { preferences ->
      preferences.remove(ACCESS_TOKEN)
      preferences.remove(REFRESH_TOKEN)
      preferences.remove(ACCESS_TOKEN_EXPIRES_AT)
      preferences.remove(REFRESH_TOKEN_EXPIRES_AT)
    }
  }
}
```

#### 앱 시작 시 자동 토큰 갱신

```kotlin
// src/data/repository/AuthRepository.kt
@Singleton
class AuthRepository @Inject constructor(
  private val tokenPreferences: TokenPreferences,
  private val apiService: MemoirApiService,
  private val userRepository: UserRepository
) {

  /**
   * 앱 시작 시 호출 - 저장된 토큰 검증 및 필요시 갱신
   */
  suspend fun initializeSession(): SessionStatus {
    return try {
      val accessToken = tokenPreferences.accessToken.first()
      val refreshToken = tokenPreferences.refreshToken.first()
      val expiresAt = tokenPreferences.ACCESS_TOKEN_EXPIRES_AT.first()

      // 토큰이 없으면 미인증
      if (accessToken == null || refreshToken == null) {
        return SessionStatus.Unauthenticated
      }

      val now = System.currentTimeMillis()
      val timeUntilExpiry = expiresAt - now

      // Access Token이 1시간 이내로 만료 예정 → 갱신
      if (timeUntilExpiry < 60 * 60 * 1000) {
        val newTokens = apiService.refreshToken(
          RefreshTokenRequest(refreshToken)
        )

        tokenPreferences.saveTokens(
          accessToken = newTokens.accessToken,
          refreshToken = newTokens.refreshToken,
          accessTokenExpiresAt = System.currentTimeMillis() + (30L * 24 * 3600 * 1000),
          refreshTokenExpiresAt = System.currentTimeMillis() + (90L * 24 * 3600 * 1000)
        )
      }

      // 온보딩 완료 상태 확인
      val onboardingCompleted = tokenPreferences.onboardingCompleted.first()
      return if (onboardingCompleted) {
        SessionStatus.AuthenticatedAndOnboarded
      } else {
        SessionStatus.AuthenticatedRequiresOnboarding
      }

    } catch (e: HttpException) {
      when (e.code()) {
        401 -> {
          // 토큰 만료 또는 무효 → 재로그인 필요
          tokenPreferences.clearTokens()
          SessionStatus.Unauthenticated
        }
        else -> SessionStatus.Error(e.message ?: "Unknown error")
      }
    } catch (e: Exception) {
      SessionStatus.Error(e.message ?: "Session initialization failed")
    }
  }
}

sealed class SessionStatus {
  object Unauthenticated : SessionStatus()
  object AuthenticatedRequiresOnboarding : SessionStatus()
  object AuthenticatedAndOnboarded : SessionStatus()
  data class Error(val message: String) : SessionStatus()
}
```

#### 로그아웃 처리

```kotlin
// src/data/repository/AuthRepository.kt (계속)

suspend fun logout() {
  try {
    // Backend에 로그아웃 요청 (optional - 토큰 블랙리스트 처리)
    apiService.logout()
  } catch (e: Exception) {
    // Backend 요청 실패해도 로컬 토큰은 제거
  }

  // 로컬 토큰 제거
  tokenPreferences.clearTokens()
}
```

### 5.2 서버 측 (선택적 Refresh Token 저장소)

#### 데이터베이스 스키마

```sql
-- Refresh Token 저장소 (로테이션 추적)
CREATE TABLE refresh_token_store (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash VARCHAR(64) NOT NULL, -- SHA-256 hash
  family VARCHAR(36) NOT NULL,      -- Token Family ID
  rotation_count INTEGER NOT NULL DEFAULT 1,
  expires_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

  UNIQUE(user_id, family),
  INDEX idx_user_id (user_id),
  INDEX idx_family (family)
);

-- 토큰 블랙리스트 (로그아웃 처리)
CREATE TABLE token_blacklist (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash VARCHAR(64) NOT NULL,
  reason VARCHAR(50) NOT NULL, -- 'logout' | 'password_change' | 'security_issue'
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  expires_at TIMESTAMPTZ NOT NULL,

  INDEX idx_user_id (user_id),
  INDEX idx_expires_at (expires_at)
);

-- 사용자 세션 관리 (기기별 추적 - Phase 2+)
CREATE TABLE user_sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  device_id VARCHAR(255) NOT NULL,
  device_name VARCHAR(255),
  app_version VARCHAR(20),
  last_activity_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_active BOOLEAN DEFAULT true,

  UNIQUE(user_id, device_id)
);
```

---

## 6. 보안 고려사항

### 6.1 암호화 및 전송 보안

#### HTTPS/TLS 1.3 강제

```typescript
// src/middleware/security.middleware.ts
import helmet from 'helmet';
import express from 'express';

export function setupSecurityHeaders(app: express.Application) {
  // Helmet: 보안 헤더 설정
  app.use(helmet({
    contentSecurityPolicy: {
      directives: {
        defaultSrc: ["'self'"],
        scriptSrc: ["'self'"],
        styleSrc: ["'self'", "'unsafe-inline'"],
        imgSrc: ["'self'", "data:", "https:"],
        connectSrc: ["'self'"],
        fontSrc: ["'self'"],
        objectSrc: ["'none'"],
        mediaSrc: ["'none'"],
        frameSrc: ["'none'"]
      }
    },
    // HSTS: 1년간 HTTPS만 사용
    hsts: {
      maxAge: 31536000,
      includeSubDomains: true,
      preload: true
    },
    // XSS 방지
    xssFilter: true,
    // Clickjacking 방지
    frameguard: { action: 'deny' },
    // MIME sniffing 방지
    noSniff: true,
    // Referrer 정책
    referrerPolicy: { policy: 'strict-origin-when-cross-origin' }
  }));

  // HTTPS 리다이렉트
  app.use((req, res, next) => {
    if (process.env.NODE_ENV === 'production' && !req.secure) {
      return res.redirect(`https://${req.headers.host}${req.url}`);
    }
    next();
  });

  // TLS 버전 확인 (Node.js 설정)
  // production 환경에서는 다음과 같이 설정:
  // const options = {
  //   key: fs.readFileSync('private.key'),
  //   cert: fs.readFileSync('certificate.crt'),
  //   minVersion: 'TLSv1.3'
  // };
  // const server = https.createServer(options, app);
}
```

#### Android의 암호화된 토큰 저장

```kotlin
// src/data/local/EncryptedTokenPreferences.kt
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedTokenPreferences @Inject constructor(
  private val context: Context
) {
  private val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

  private val sharedPreferences = EncryptedSharedPreferences.create(
    context,
    "memoir_tokens",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
  )

  /**
   * Android Keystore를 사용한 자동 암호화
   * - 토큰은 디바이스 고유 키로 암호화되어 저장됨
   * - 기기 로스트 시 토큰 복구 불가능 (보안 우위)
   */
  fun saveToken(key: String, value: String) {
    sharedPreferences.edit().putString(key, value).apply()
  }

  fun getToken(key: String): String? {
    return sharedPreferences.getString(key, null)
  }

  fun clearAllTokens() {
    sharedPreferences.edit().clear().apply()
  }
}
```

### 6.2 Rate Limiting (인증 엔드포인트)

```typescript
// src/middleware/rate-limit.ts
import rateLimit from 'express-rate-limit';
import RedisStore from 'rate-limit-redis';
import redis from 'redis';

const redisClient = redis.createClient({
  host: process.env.REDIS_HOST || 'localhost',
  port: parseInt(process.env.REDIS_PORT || '6379')
});

/**
 * 카카오 로그인 엔드포인트: IP당 5분마다 10회 제한
 */
export const rateLimitAuth = rateLimit({
  store: new RedisStore({
    client: redisClient,
    prefix: 'rate_limit:auth:'
  }),
  windowMs: 5 * 60 * 1000, // 5분
  max: 10, // 최대 10회
  message: {
    error: {
      code: 'RATE_LIMIT_EXCEEDED',
      message: '너무 많은 요청이 발생했습니다. 잠시 후 다시 시도해주세요'
    }
  },
  standardHeaders: true,
  legacyHeaders: false,
  skip: (req) => {
    // 테스트 환경에서는 rate limit 무시
    return process.env.NODE_ENV === 'test';
  },
  keyGenerator: (req) => {
    // IP 주소 기반 rate limiting
    return req.ip || req.socket.remoteAddress || 'unknown';
  }
});

/**
 * 토큰 갱신 엔드포인트: 사용자당 1분마다 5회 제한
 */
export const rateLimitRefresh = rateLimit({
  store: new RedisStore({
    client: redisClient,
    prefix: 'rate_limit:refresh:'
  }),
  windowMs: 60 * 1000, // 1분
  max: 5,
  message: {
    error: {
      code: 'RATE_LIMIT_EXCEEDED',
      message: '토큰 갱신 요청이 너무 많습니다'
    }
  },
  keyGenerator: (req) => {
    // 사용자별 rate limiting
    return req.user?.id || req.ip || 'unknown';
  }
});
```

### 6.3 입력 검증 (Prisma 파라미터화)

```typescript
// src/services/user.service.ts
import { prisma } from '../db/prisma';
import { validateKoreanName } from '../utils/validation';

export class UserService {

  /**
   * Kakao ID로 사용자 조회 (Prisma 자동 파라미터화)
   */
  async findByKakaoId(kakaoId: string) {
    // Prisma는 자동으로 파라미터화되므로 SQL Injection 위험 없음
    return prisma.user.findUnique({
      where: { kakao_id: kakaoId }
    });
  }

  /**
   * 사용자 생성
   */
  async createUser(data: {
    kakao_id: string;
    kakao_nickname: string;
    kakao_profile_image?: string;
    status: 'active' | 'inactive' | 'locked';
  }) {
    // 입력 검증
    if (!data.kakao_id || data.kakao_id.length > 50) {
      throw new Error('Invalid kakao_id');
    }

    return prisma.user.create({
      data: {
        kakao_id: data.kakao_id,
        status: data.status,
        created_at: new Date(),
        updated_at: new Date()
      }
    });
  }

  /**
   * 사용자 프로필 생성
   */
  async createUserProfile(userId: string, data: {
    name: string;
    role: string;
    industry_code: string;
    growth_goals: string;
    profile_photo_url?: string;
  }) {
    // 이름 검증: 한글 2-4자
    if (!validateKoreanName(data.name)) {
      throw new Error('Invalid Korean name format');
    }

    // 역할 검증: 길이 제한
    if (data.role.length < 2 || data.role.length > 64) {
      throw new Error('Role must be between 2 and 64 characters');
    }

    // 성장 목표 검증: 최소 100자
    if (data.growth_goals.length < 100 || data.growth_goals.length > 500) {
      throw new Error('Growth goals must be between 100 and 500 characters');
    }

    return prisma.userProfile.create({
      data: {
        user_id: userId,
        name: data.name,
        role: data.role,
        industry_code: data.industry_code,
        growth_goals: data.growth_goals,
        profile_photo_url: data.profile_photo_url || null,
        onboarding_completed_at: new Date(),
        created_at: new Date(),
        updated_at: new Date()
      }
    });
  }
}

// src/utils/validation.ts
/**
 * 한글 이름 검증 (2-4자의 한글만 허용)
 */
export function validateKoreanName(name: string): boolean {
  const koreanNameRegex = /^[가-힣]{2,4}$/;
  return koreanNameRegex.test(name);
}

/**
 * 이메일 검증
 */
export function validateEmail(email: string): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

/**
 * Industry Code 검증
 */
export const VALID_INDUSTRIES = [
  'STARTUP',
  'ENTERPRISE',
  'SME',
  'PUBLIC',
  'FOREIGN',
  'FREELANCER',
  'NONPROFIT',
  'OTHER'
] as const;

export function validateIndustryCode(code: string): code is typeof VALID_INDUSTRIES[number] {
  return VALID_INDUSTRIES.includes(code as any);
}
```

### 6.4 로깅 (민감한 정보 제외)

```typescript
// src/utils/logger.ts
import winston from 'winston';

const logger = winston.createLogger({
  level: process.env.LOG_LEVEL || 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.errors({ stack: true }),
    winston.format.json()
  ),
  defaultMeta: { service: 'memoir-auth' },
  transports: [
    new winston.transports.File({ filename: 'error.log', level: 'error' }),
    new winston.transports.File({ filename: 'combined.log' })
  ]
});

// 민감한 정보 마스킹
function maskSensitiveData(data: any): any {
  if (typeof data !== 'object' || data === null) {
    return data;
  }

  const masked = { ...data };

  // 토큰 마스킹
  if (masked.token) {
    masked.token = '***MASKED***';
  }
  if (masked.access_token) {
    masked.access_token = '***MASKED***';
  }
  if (masked.refresh_token) {
    masked.refresh_token = '***MASKED***';
  }
  if (masked.kakao_oauth_code) {
    masked.kakao_oauth_code = masked.kakao_oauth_code.substring(0, 5) + '***';
  }
  if (masked.password) {
    masked.password = '***MASKED***';
  }

  return masked;
}

export class Logger {
  info(message: string, data?: any) {
    logger.info(message, maskSensitiveData(data));
  }

  error(message: string, data?: any) {
    logger.error(message, maskSensitiveData(data));
  }

  warn(message: string, data?: any) {
    logger.warn(message, maskSensitiveData(data));
  }

  debug(message: string, data?: any) {
    logger.debug(message, maskSensitiveData(data));
  }
}
```

---

## 7. 에러 처리 & 복구

### 7.1 에러 분류 및 처리 전략

| 에러 코드 | HTTP 상태 | 설명 | 클라이언트 처리 | 재시도 |
|---------|---------|------|---------------|-------|
| INVALID_KAKAO_CODE | 400 | 유효하지 않은 인증 코드 | 사용자에게 표시, 재로그인 유도 | 아니오 |
| KAKAO_API_ERROR | 502 | Kakao 서비스 오류 | 잠시 후 재시도 메시지 | 지수 백오프 |
| TOKEN_INVALID | 401 | 유효하지 않은 토큰 | 토큰 제거, 재로그인 | 아니오 |
| TOKEN_EXPIRED | 401 | 토큰 만료 | 자동 갱신 시도 | 예 (1회) |
| REFRESH_TOKEN_EXPIRED | 401 | Refresh 토큰 만료 | 재로그인 유도 | 아니오 |
| TOKEN_ROTATION_CONFLICT | 409 | 토큰 탈취 의심 | 모든 세션 종료, 재로그인 | 아니오 |
| VALIDATION_ERROR | 400 | 입력 데이터 오류 | 필드별 에러 메시지 표시 | 아니오 |
| NETWORK_TIMEOUT | (네트워크) | 연결 타임아웃 | 재시도 옵션 제시 | 지수 백오프 |
| RATE_LIMIT_EXCEEDED | 429 | 요청 제한 | "잠시 후 다시" 메시지 | 대기 후 재시도 |
| INTERNAL_ERROR | 500 | 서버 오류 | 일반 에러 메시지 | 지수 백오프 |

### 7.2 Kakao API 오류 처리

```typescript
// src/services/kakao.service.ts
export class KakaoService {

  private async handleKakaoError(error: any): Promise<never> {
    if (error.response?.status === 400) {
      // 400: 부정확한 파라미터
      throw {
        code: 'INVALID_KAKAO_CODE',
        message: '유효하지 않은 카카오 인증 코드입니다',
        statusCode: 400
      };
    }

    if (error.response?.status === 401) {
      // 401: 미인증 (Client ID 오류 등)
      throw {
        code: 'KAKAO_UNAUTHORIZED',
        message: '카카오 서비스 설정에 오류가 있습니다',
        statusCode: 502
      };
    }

    if (error.response?.status === 429) {
      // 429: Rate Limiting
      throw {
        code: 'KAKAO_RATE_LIMIT',
        message: '카카오 서비스 요청이 너무 많습니다. 잠시 후 다시 시도해주세요',
        statusCode: 429
      };
    }

    if (error.response?.status >= 500 || error.code === 'ECONNREFUSED') {
      // 5xx: 서버 오류 또는 연결 실패
      throw {
        code: 'KAKAO_SERVICE_UNAVAILABLE',
        message: '카카오 서비스를 이용할 수 없습니다. 잠시 후 다시 시도해주세요',
        statusCode: 502
      };
    }

    throw {
      code: 'KAKAO_UNKNOWN_ERROR',
      message: '카카오 서비스 연결에 실패했습니다',
      statusCode: 502
    };
  }
}
```

### 7.3 네트워크 타임아웃 및 재시도 로직

```kotlin
// src/data/remote/MemoirApiClient.kt
class MemoirApiClient(
  private val baseUrl: String,
  private val sessionManager: SessionManager
) {

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .writeTimeout(10, TimeUnit.SECONDS)
    .addInterceptor(RetryInterceptor())
    .addInterceptor(TokenRefreshInterceptor(sessionManager))
    .build()

  /**
   * 지수 백오프를 사용한 재시도 인터셉터
   */
  private inner class RetryInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
      var request = chain.request()
      var response: Response? = null
      var exception: Exception? = null

      // 최대 3회 재시도
      for (attempt in 1..3) {
        try {
          response = chain.proceed(request)

          // 재시도 가능한 상태코드: 408, 429, 5xx
          if (response.code in listOf(408, 429) || response.code >= 500) {
            response.close()

            if (attempt < 3) {
              // 지수 백오프: 1초 → 2초 → 4초
              val delayMs = 1000L * (1 shl (attempt - 1))
              Thread.sleep(delayMs)
              continue
            }
          }

          // 성공 또는 재시도 불가능한 상태
          return response

        } catch (e: SocketTimeoutException) {
          exception = e
          if (attempt < 3) {
            val delayMs = 1000L * (1 shl (attempt - 1))
            Thread.sleep(delayMs)
          }
        } catch (e: Exception) {
          exception = e
          // 다른 예외는 재시도하지 않음
          throw e
        }
      }

      // 모든 재시도 실패
      throw exception ?: Exception("Network request failed")
    }
  }

  /**
   * 토큰 갱신 인터셉터
   */
  private inner class TokenRefreshInterceptor(
    private val sessionManager: SessionManager
  ) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
      val request = chain.request()
      var response = chain.proceed(request)

      // 401 응답 처리
      if (response.code == 401) {
        synchronized(this) {
          // 재확인 (double-check locking)
          val updatedRequest = addAuthHeader(chain.request())
          if (updatedRequest !== request) {
            response.close()
            response = chain.proceed(updatedRequest)
          }
        }
      }

      return response
    }

    private fun addAuthHeader(request: Request): Request {
      val token = runBlocking {
        sessionManager.ensureValidAccessToken()
      }

      return if (token != null) {
        request.newBuilder()
          .header("Authorization", "Bearer $token")
          .build()
      } else {
        request
      }
    }
  }
}
```

```typescript
// src/middleware/error-handler.ts
export function globalErrorHandler(
  err: any,
  req: express.Request,
  res: express.Response,
  next: express.NextFunction
) {
  const logger = new Logger();

  // 이미 응답을 보낸 경우
  if (res.headersSent) {
    return next(err);
  }

  // 에러 로깅
  logger.error('Unhandled error', {
    message: err.message,
    code: err.code,
    statusCode: err.statusCode || 500,
    path: req.path,
    method: req.method
  });

  // 클라이언트 에러 (4xx)
  if (err.statusCode && err.statusCode >= 400 && err.statusCode < 500) {
    return res.status(err.statusCode).json({
      error: {
        code: err.code || 'CLIENT_ERROR',
        message: err.message || '요청에 오류가 있습니다'
      }
    });
  }

  // 서버 에러 (5xx)
  res.status(err.statusCode || 500).json({
    error: {
      code: err.code || 'INTERNAL_ERROR',
      message: process.env.NODE_ENV === 'production'
        ? '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요'
        : err.message
    }
  });
}
```

---

## 8. 분석 이벤트 (Analytics)

### 8.1 이벤트 스키마

| 이벤트 | 속성 | 설명 | 발생 시점 |
|-------|------|------|---------|
| `kakao_login_initiated` | source (button) | Kakao 로그인 시작 | 사용자가 로그인 버튼 클릭 |
| `kakao_login_success` | user_new (bool), onboarding_required (bool) | Kakao 로그인 성공 | 토큰 교환 완료 |
| `kakao_login_failure` | reason (string) | Kakao 로그인 실패 | 에러 발생 |
| `onboarding_started` | - | 온보딩 시작 | 첫 화면 표시 |
| `onboarding_screen_viewed` | screen_index (1-5), duration_ms (long) | 온보딩 스크린 조회 | 각 화면 진입 |
| `onboarding_abandoned` | last_screen_index (1-5) | 온보딩 포기 | 앱 강제 종료 또는 뒤로가기 |
| `onboarding_completed` | total_duration_ms (long) | 온보딩 완료 | 프로필 제출 성공 |
| `profile_submission_started` | chars_growth_goals (int) | 프로필 제출 시작 | 제출 버튼 클릭 |
| `profile_submission_success` | industry_code (string) | 프로필 제출 성공 | API 응답 200 |
| `profile_submission_failure` | error_code (string), retry_count (int) | 프로필 제출 실패 | API 에러 또는 네트워크 오류 |
| `terms_accepted` | tos_version (string), privacy_version (string) | 약관 동의 | 체크박스 표시 |
| `session_token_refreshed` | rotation (bool), elapsed_ms (long) | 세션 토큰 갱신 | 토큰 로테이션 완료 |
| `session_token_refresh_failed` | reason (string), retry_attempted (bool) | 세션 토큰 갱신 실패 | 갱신 API 오류 |

### 8.2 클라이언트 분석 구현

```kotlin
// src/data/analytics/AnalyticsService.kt
interface AnalyticsService {
  fun logEvent(name: String, params: Map<String, Any>?)
  fun setUserId(userId: String)
  fun clearUserId()
}

@Singleton
class FirebaseAnalyticsService @Inject constructor(
  private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsService {

  override fun logEvent(name: String, params: Map<String, Any>?) {
    val bundle = Bundle().apply {
      params?.forEach { (key, value) ->
        when (value) {
          is String -> putString(key, value)
          is Int -> putInt(key, value)
          is Long -> putLong(key, value)
          is Boolean -> putBoolean(key, value)
          is Double -> putDouble(key, value)
          is Float -> putFloat(key, value)
        }
      }
    }

    firebaseAnalytics.logEvent(name, bundle)
  }

  override fun setUserId(userId: String) {
    firebaseAnalytics.setUserId(userId)
  }

  override fun clearUserId() {
    firebaseAnalytics.setUserId(null)
  }
}

// 사용 예제
@Singleton
class AuthenticationViewModel @Inject constructor(
  private val analytics: AnalyticsService
) : ViewModel() {

  fun initiateKakaoLogin() {
    analytics.logEvent("kakao_login_initiated", mapOf(
      "source" to "button"
    ))
  }

  fun onLoginSuccess(isNewUser: Boolean, requiresOnboarding: Boolean) {
    analytics.logEvent("kakao_login_success", mapOf(
      "user_new" to isNewUser,
      "onboarding_required" to requiresOnboarding
    ))
  }

  fun onLoginFailure(error: String) {
    analytics.logEvent("kakao_login_failure", mapOf(
      "reason" to error
    ))
  }
}

// OnboardingViewModel.kt
@Singleton
class OnboardingViewModel @Inject constructor(
  private val analytics: AnalyticsService,
  private val userRepository: UserRepository
) : ViewModel() {

  private var onboardingStartTime = System.currentTimeMillis()
  private var screenStartTime = System.currentTimeMillis()

  fun onScreenViewed(screenIndex: Int) {
    val previousScreenStartTime = screenStartTime
    screenStartTime = System.currentTimeMillis()

    analytics.logEvent("onboarding_screen_viewed", mapOf(
      "screen_index" to screenIndex,
      "duration_ms" to (screenStartTime - previousScreenStartTime)
    ))
  }

  fun onOnboardingCompleted() {
    val totalDuration = System.currentTimeMillis() - onboardingStartTime

    analytics.logEvent("onboarding_completed", mapOf(
      "total_duration_ms" to totalDuration
    ))
  }

  fun onProfileSubmitted(industryCode: String) {
    analytics.logEvent("profile_submission_success", mapOf(
      "industry_code" to industryCode
    ))
  }

  fun onProfileSubmissionFailure(errorCode: String, retryAttempted: Boolean) {
    analytics.logEvent("profile_submission_failure", mapOf(
      "error_code" to errorCode,
      "retry_count" to 1
    ))
  }
}
```

### 8.3 서버 분석 로깅

```typescript
// src/services/analytics.service.ts
@Injectable()
export class AnalyticsService {
  private readonly logger = new Logger('AnalyticsService');

  /**
   * Firebase Analytics로 이벤트 전송 (Admin SDK)
   */
  async logUserEvent(
    userId: string,
    eventName: string,
    eventData?: Record<string, any>
  ) {
    try {
      await admin.analytics().app().analytics().log(eventName, {
        user_id: userId,
        ...eventData,
        timestamp: new Date().toISOString()
      });
    } catch (error) {
      this.logger.error(`Failed to log event ${eventName}`, error);
    }
  }

  /**
   * 구조화된 로그로 분석 데이터 저장
   */
  logAuthEvent(
    userId: string,
    eventType: 'login_success' | 'login_failure' | 'token_refresh',
    data?: Record<string, any>
  ) {
    this.logger.log({
      event: eventType,
      user_id: userId,
      timestamp: new Date().toISOString(),
      ...data
    });
  }
}
```

---

## 9. 테스트 전략

### 9.1 단위 테스트

#### Android (Kotlin + JUnit 5)

```kotlin
// test/kotlin/com/memoir/auth/domain/usecases/ValidateKoreanNameUseCaseTest.kt
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("한글 이름 검증 Use Case")
class ValidateKoreanNameUseCaseTest {

  private val useCase = ValidateKoreanNameUseCase()

  @Test
  @DisplayName("2자 한글 이름은 유효해야 한다")
  fun `valid two-character Korean name`() {
    assertTrue(useCase("김철"))
  }

  @Test
  @DisplayName("4자 한글 이름은 유효해야 한다")
  fun `valid four-character Korean name`() {
    assertTrue(useCase("박민준이"))
  }

  @Test
  @DisplayName("1자 한글 이름은 무효해야 한다")
  fun `invalid one-character Korean name`() {
    assertFalse(useCase("김"))
  }

  @Test
  @DisplayName("5자 이상 한글 이름은 무효해야 한다")
  fun `invalid five-plus character Korean name`() {
    assertFalse(useCase("김철수입니다"))
  }

  @Test
  @DisplayName("영문 이름은 무효해야 한다")
  fun `invalid English name`() {
    assertFalse(useCase("John"))
  }

  @Test
  @DisplayName("숫자가 포함된 이름은 무효해야 한다")
  fun `invalid name with numbers`() {
    assertFalse(useCase("김철2"))
  }
}

// test/kotlin/com/memoir/auth/data/repository/AuthRepositoryTest.kt
@DisplayName("Authentication Repository")
class AuthRepositoryTest {

  private val mockApiService = mockk<MemoirApiService>()
  private val mockTokenPreferences = mockk<TokenPreferences>()
  private val authRepository = AuthRepository(mockTokenPreferences, mockApiService)

  @Test
  @DisplayName("새로운 사용자 로그인 시 user_exists=true를 반환해야 한다")
  fun `successful kakao login for new user`() = runTest {
    // Arrange
    val mockResponse = KakaoTokenExchangeResponse(
      accessToken = "mock_access_token",
      refreshToken = "mock_refresh_token",
      userExists = false,
      onboardingCompleted = false
    )

    coEvery { mockApiService.exchangeKakaoCode(any()) } returns mockResponse
    coEvery { mockTokenPreferences.saveTokens(any(), any(), any(), any()) } just runs

    // Act
    val result = authRepository.exchangeKakaoCode("mock_code")

    // Assert
    assertFalse(result.userExists)
    assertFalse(result.onboardingCompleted)
    assertEquals("mock_access_token", result.accessToken)
  }

  @Test
  @DisplayName("유효하지 않은 인증 코드 시 에러 반환")
  fun `failed kakao exchange with invalid code`() = runTest {
    // Arrange
    coEvery { mockApiService.exchangeKakaoCode(any()) } throws
      HttpException(Response.error(400, "".toResponseBody()))

    // Act & Assert
    assertFailsWith<HttpException> {
      authRepository.exchangeKakaoCode("invalid_code")
    }
  }
}
```

#### Backend (TypeScript + Jest)

```typescript
// test/services/token.service.test.ts
import { TokenService } from '../../src/services/token.service';
import jwt from 'jsonwebtoken';

describe('TokenService', () => {
  let tokenService: TokenService;

  beforeEach(() => {
    process.env.JWT_ACCESS_SECRET = 'test_access_secret';
    process.env.JWT_REFRESH_SECRET = 'test_refresh_secret';
    tokenService = new TokenService();
  });

  describe('generateTokenPair', () => {
    it('should generate valid access and refresh tokens', async () => {
      const userId = 'test-user-id';
      const result = await tokenService.generateTokenPair(userId);

      expect(result.accessToken).toBeDefined();
      expect(result.refreshToken).toBeDefined();

      const decodedAccess = jwt.decode(result.accessToken) as any;
      const decodedRefresh = jwt.decode(result.refreshToken) as any;

      expect(decodedAccess.sub).toBe(userId);
      expect(decodedAccess.type).toBe('access');
      expect(decodedRefresh.sub).toBe(userId);
      expect(decodedRefresh.type).toBe('refresh');
    });

    it('should set correct expiration times', async () => {
      const result = await tokenService.generateTokenPair('test-user');

      const decodedAccess = jwt.decode(result.accessToken) as any;
      const decodedRefresh = jwt.decode(result.refreshToken) as any;

      const now = Math.floor(Date.now() / 1000);
      const thirtyDaysInSeconds = 30 * 24 * 60 * 60;
      const ninetyDaysInSeconds = 90 * 24 * 60 * 60;

      expect(decodedAccess.exp - decodedAccess.iat).toBeCloseTo(thirtyDaysInSeconds, -2);
      expect(decodedRefresh.exp - decodedRefresh.iat).toBeCloseTo(ninetyDaysInSeconds, -2);
    });
  });

  describe('verifyAccessToken', () => {
    it('should verify valid token', async () => {
      const userId = 'test-user';
      const { accessToken } = await tokenService.generateTokenPair(userId);

      const result = tokenService.verifyAccessToken(accessToken);

      expect(result.valid).toBe(true);
      expect(result.payload?.sub).toBe(userId);
    });

    it('should reject invalid token', () => {
      const result = tokenService.verifyAccessToken('invalid_token');

      expect(result.valid).toBe(false);
      expect(result.error).toBeDefined();
    });
  });
});
```

### 9.2 통합 테스트

```typescript
// test/integration/auth.integration.test.ts
import request from 'supertest';
import { app } from '../../src/app';
import { prisma } from '../../src/db/prisma';
import jwt from 'jsonwebtoken';

describe('Authentication Integration Tests', () => {
  beforeEach(async () => {
    await prisma.user.deleteMany({});
    await prisma.refreshTokenStore.deleteMany({});
  });

  afterAll(async () => {
    await prisma.$disconnect();
  });

  describe('POST /api/v1/auth/kakao', () => {
    it('should exchange valid kakao code for JWT tokens', async () => {
      // Mock Kakao API
      jest.mock('axios');
      const axios = require('axios');

      axios.post.mockResolvedValueOnce({
        data: {
          access_token: 'kakao_access_token',
          token_type: 'bearer'
        }
      });

      axios.get.mockResolvedValueOnce({
        data: {
          id: 1234567890,
          kakao_account: {
            profile: {
              nickname: '김철수',
              profile_image_url: 'https://example.com/photo.jpg'
            }
          }
        }
      });

      const response = await request(app)
        .post('/api/v1/auth/kakao')
        .send({ kakao_oauth_code: 'valid_code' })
        .expect(200);

      expect(response.body.access_token).toBeDefined();
      expect(response.body.refresh_token).toBeDefined();
      expect(response.body.user_exists).toBe(false);
      expect(response.body.onboarding_completed).toBe(false);

      // Verify tokens are valid JWTs
      const decodedAccess = jwt.decode(response.body.access_token) as any;
      expect(decodedAccess.type).toBe('access');

      const decodedRefresh = jwt.decode(response.body.refresh_token) as any;
      expect(decodedRefresh.type).toBe('refresh');
    });

    it('should return 400 for invalid kakao code', async () => {
      const response = await request(app)
        .post('/api/v1/auth/kakao')
        .send({ kakao_oauth_code: 'invalid_code' })
        .expect(400);

      expect(response.body.error.code).toBe('INVALID_KAKAO_CODE');
    });
  });

  describe('POST /api/v1/auth/refresh', () => {
    it('should rotate tokens successfully', async () => {
      // 먼저 사용자 생성 및 토큰 발급
      const user = await prisma.user.create({
        data: {
          kakao_id: '123456',
          status: 'active'
        }
      });

      const { refreshToken } = await tokenService.generateTokenPair(user.id);
      await tokenService.saveRefreshToken(user.id, refreshToken);

      // Refresh 요청
      const response = await request(app)
        .post('/api/v1/auth/refresh')
        .send({ refresh_token: refreshToken })
        .expect(200);

      expect(response.body.access_token).toBeDefined();
      expect(response.body.refresh_token).toBeDefined();

      // 이전 토큰은 더 이상 유효하지 않아야 함
      const reuseResponse = await request(app)
        .post('/api/v1/auth/refresh')
        .send({ refresh_token: refreshToken })
        .expect(401);

      expect(reuseResponse.body.error.code).toBe('TOKEN_ROTATION_CONFLICT');
    });
  });
});
```

### 9.3 UI 테스트 (Compose)

```kotlin
// androidTest/kotlin/com/memoir/auth/ui/AuthenticationScreenTest.kt
@RunWith(AndroidJUnit4::class)
class AuthenticationScreenTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun kakaoLoginButton_isVisible() {
    composeTestRule.setContent {
      AuthenticationScreen(
        viewModel = MockAuthenticationViewModel(),
        onNavigateToOnboarding = {},
        onNavigateToMainApp = {}
      )
    }

    composeTestRule
      .onNodeWithText("카카오로 시작하기")
      .assertIsDisplayed()
  }

  @Test
  fun kakaoLoginButton_click_initiatesLogin() {
    val viewModel = MockAuthenticationViewModel()
    var loginInitiated = false

    viewModel.onLoginInitiated = { loginInitiated = true }

    composeTestRule.setContent {
      AuthenticationScreen(
        viewModel = viewModel,
        onNavigateToOnboarding = {},
        onNavigateToMainApp = {}
      )
    }

    composeTestRule
      .onNodeWithText("카카오로 시작하기")
      .performClick()

    assertTrue(loginInitiated)
  }

  @Test
  fun errorMessage_displayedOnLoginFailure() {
    val viewModel = MockAuthenticationViewModel(
      initialState = AuthState.Error("로그인 실패")
    )

    composeTestRule.setContent {
      AuthenticationScreen(
        viewModel = viewModel,
        onNavigateToOnboarding = {},
        onNavigateToMainApp = {}
      )
    }

    composeTestRule
      .onNodeWithText("로그인 실패")
      .assertIsDisplayed()
  }
}
```

### 9.4 엔드-투-엔드 테스트

```kotlin
// androidTest/kotlin/com/memoir/auth/e2e/AuthenticationE2ETest.kt
/**
 * Kakao 테스트 계정으로 실제 로그인 검증
 * - 테스트 환경에서만 실행
 * - Kakao 테스트 앱 설정 필수
 */
@RunWith(AndroidJUnit4::class)
class AuthenticationE2ETest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Before
  fun setup() {
    // 테스트 계정 인증정보 로드
    val testKakaoId = BuildConfig.TEST_KAKAO_ID
    val testPassword = BuildConfig.TEST_KAKAO_PASSWORD

    assumeTrue("Test Kakao account not configured",
               testKakaoId.isNotEmpty() && testPassword.isNotEmpty())
  }

  @Test
  @LargeTest
  fun completeKakaoLoginFlow() {
    // 1. 앱 시작
    composeTestRule.setContent {
      MemoirApp()
    }

    // 2. 로그인 화면 확인
    composeTestRule
      .onNodeWithText("카카오로 시작하기")
      .assertIsDisplayed()

    // 3. Kakao 로그인 버튼 클릭
    composeTestRule
      .onNodeWithText("카카오로 시작하기")
      .performClick()

    // 4. Kakao 인증 다이얼로그 처리 (UI 자동화로 불가능)
    // → 수동 테스트 또는 mock 필요

    // 5. 온보딩 화면 표시 확인
    composeTestRule
      .onNodeWithText("매주 한 걸음 더 성장하는")
      .assertIsDisplayed(timeout = Duration.seconds(10))
  }
}
```

---

## 10. 구현 체크리스트

### Phase 1: Kakao OAuth 통합
- [ ] Kakao Developers 앱 등록 및 설정
- [ ] Kakao SDK 의존성 추가 (build.gradle.kts)
- [ ] KakaoAuthRepository 구현
- [ ] AuthenticationController 구현 (Kakao 토큰 교환)
- [ ] 토큰 검증 미들웨어 구현
- [ ] Error handling 추가

### Phase 2: JWT 토큰 관리
- [ ] TokenService 구현 (생성, 검증, 로테이션)
- [ ] Refresh Token Store 테이블 생성
- [ ] Token rotation endpoint 구현
- [ ] 자동 갱신 인터셉터 구현 (클라이언트)
- [ ] Rate limiting 미들웨어 추가

### Phase 3: 세션 관리
- [ ] DataStore 토큰 저장소 구현
- [ ] 앱 시작 시 자동 갱신 로직
- [ ] 로그아웃 구현
- [ ] 보안 헤더 설정

### Phase 4: 테스트
- [ ] 단위 테스트 작성 (TokenService, Validators)
- [ ] 통합 테스트 (Kakao mock + 토큰 교환)
- [ ] UI 테스트 (Compose)
- [ ] 수동 E2E 테스트

### Phase 5: 모니터링
- [ ] Firebase Analytics 이벤트 추가
- [ ] 구조화된 로깅 설정
- [ ] 에러 모니터링 (Crashlytics)
- [ ] 성능 모니터링

---

## 11. 환경 변수 설정

```bash
# .env.development
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret
KAKAO_REDIRECT_URI=https://dev.memoir.app/auth/kakao/callback

JWT_ACCESS_SECRET=your_random_secret_key_min_32_chars
JWT_REFRESH_SECRET=your_random_secret_key_min_32_chars

REDIS_HOST=localhost
REDIS_PORT=6379

# .env.production
KAKAO_CLIENT_ID=prod_kakao_client_id
KAKAO_CLIENT_SECRET=prod_kakao_client_secret
KAKAO_REDIRECT_URI=https://memoir.app/auth/kakao/callback

# secrets manager에서 가져옴
JWT_ACCESS_SECRET=${SECRET_JWT_ACCESS}
JWT_REFRESH_SECRET=${SECRET_JWT_REFRESH}
```

---

## 12. 문서 참고

- [Kakao 개발자 가이드](https://developers.kakao.com/)
- [JWT.io](https://jwt.io/)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [Android Security Best Practices](https://developer.android.com/training/articles/security-tips)

---

## 마지막 체크사항

### 보안 검증
- [ ] 모든 토큰은 HTTPS를 통해서만 전송
- [ ] Refresh Token은 DB에 해시되어 저장
- [ ] Android Keystore를 통해 암호화 저장
- [ ] 민감한 정보는 로그에서 제외
- [ ] Rate limiting은 모든 인증 엔드포인트에 적용

### 성능 검증
- [ ] Kakao 토큰 교환: <3초
- [ ] 프로필 제출: <2초 (p95)
- [ ] 토큰 갱신: <1.5초
- [ ] 앱 시작: <3초

### 사용성 검증
- [ ] 에러 메시지는 모두 한국어
- [ ] 에러 메시지는 사용자 친화적 (기술용어 제외)
- [ ] 네트워크 오류 시 재시도 옵션 제시
- [ ] 토큰 만료 시 자동 갱신 시도

---

**작성일**: 2025년 11월 19일
**버전**: 1.0
**상태**: Phase 1 MVP 구현 준비 완료
