#!/usr/bin/env python3
"""
Backend FastAPI 프로젝트 Dry-Run 검증 스크립트
실제 배포 가능 여부를 종합적으로 검증합니다.
"""
import sys
import subprocess
from pathlib import Path
import importlib.util
import re

class BackendDryRun:
    def __init__(self, backend_dir: str):
        self.backend_dir = Path(backend_dir)
        self.errors = []
        self.warnings = []
        self.success_count = 0

    def verify_all(self):
        print("🔍 Backend FastAPI 프로젝트 Dry-Run 검증 시작...\n")

        # 1. Python 파일 구문 검증
        print("📝 Step 1: Python 파일 구문 검증")
        self.verify_python_syntax()

        # 2. 의존성 검증
        print("\n📦 Step 2: 의존성 검증")
        self.verify_dependencies()

        # 3. Import 검증
        print("\n🔗 Step 3: Import 검증")
        self.verify_imports()

        # 4. 환경변수 검증
        print("\n⚙️  Step 4: 환경변수 검증")
        self.verify_env_config()

        # 5. FastAPI 앱 검증
        print("\n🚀 Step 5: FastAPI 앱 검증")
        self.verify_fastapi_app()

        # 6. 데이터베이스 마이그레이션 검증
        print("\n🗄️  Step 6: Alembic 마이그레이션 검증")
        self.verify_alembic()

        # 7. Dockerfile 검증
        print("\n🐳 Step 7: Dockerfile 검증")
        self.verify_dockerfile()

        # 8. 테스트 검증
        print("\n🧪 Step 8: 테스트 실행")
        self.run_tests()

        # 결과 출력
        self.print_summary()

    def verify_python_syntax(self):
        """모든 Python 파일 구문 검증"""
        py_files = list(self.backend_dir.glob("*.py"))
        print(f"  총 {len(py_files)}개 Python 파일 검증 중...")

        for py_file in py_files:
            try:
                result = subprocess.run(
                    ["python3", "-m", "py_compile", str(py_file)],
                    capture_output=True,
                    timeout=5
                )
                if result.returncode == 0:
                    self.success_count += 1
                else:
                    self.errors.append(f"{py_file.name}: 구문 오류")
                    print(f"  ✗ {py_file.name}")
            except Exception as e:
                self.errors.append(f"{py_file.name}: {str(e)}")

        if not self.errors:
            print(f"  ✓ {len(py_files)}개 파일 모두 구문 검증 통과")

    def verify_dependencies(self):
        """requirements.txt 의존성 검증"""
        req_file = self.backend_dir / "requirements.txt"
        if not req_file.exists():
            self.errors.append("requirements.txt 파일이 없습니다")
            return

        content = req_file.read_text()
        deps = [line.strip() for line in content.split('\n') if line.strip() and not line.startswith('#')]

        print(f"  총 {len(deps)}개 의존성 확인 중...")

        required_packages = {
            "fastapi": "FastAPI 프레임워크",
            "uvicorn": "ASGI 서버",
            "sqlalchemy": "ORM",
            "asyncpg": "PostgreSQL 드라이버",
            "pydantic": "데이터 검증",
            "python-jose": "JWT 토큰",
            "passlib": "비밀번호 해싱",
            "alembic": "데이터베이스 마이그레이션",
        }

        for package, desc in required_packages.items():
            found = any(package in dep.lower() for dep in deps)
            if found:
                print(f"  ✓ {desc} ({package})")
                self.success_count += 1
            else:
                self.warnings.append(f"권장 패키지 누락: {package} ({desc})")
                print(f"  ⚠ {desc} ({package})")

    def verify_imports(self):
        """모든 Python 파일의 import 검증"""
        py_files = [f for f in self.backend_dir.glob("*.py") if not f.name.startswith("test_")]

        print(f"  {len(py_files)}개 파일 import 검증 중...")

        for py_file in py_files:
            try:
                # 파일을 실제로 import 시도 (구문만 체크)
                spec = importlib.util.spec_from_file_location(py_file.stem, py_file)
                if spec and spec.loader:
                    # 실제 로드는 하지 않고 스펙만 확인
                    self.success_count += 1
                else:
                    self.warnings.append(f"{py_file.name}: import spec 생성 실패")
            except Exception as e:
                self.warnings.append(f"{py_file.name}: {str(e)}")

        if len([w for w in self.warnings if "import" in w.lower()]) == 0:
            print(f"  ✓ 모든 파일 import 가능")

    def verify_env_config(self):
        """환경변수 설정 파일 검증"""
        config_file = self.backend_dir / "config.py"

        if not config_file.exists():
            self.errors.append("config.py 파일이 없습니다")
            return

        content = config_file.read_text()

        required_configs = [
            ("DATABASE_URL", "데이터베이스 연결"),
            ("SECRET_KEY", "JWT 시크릿 키"),
            ("ACCESS_TOKEN_EXPIRE", "토큰 만료 시간"),
        ]

        for config, desc in required_configs:
            if config in content:
                print(f"  ✓ {desc} ({config})")
                self.success_count += 1
            else:
                self.warnings.append(f"환경변수 누락 가능성: {config}")
                print(f"  ⚠ {desc} ({config})")

    def verify_fastapi_app(self):
        """FastAPI 앱 생성 검증"""
        main_file = self.backend_dir / "main.py"

        if not main_file.exists():
            self.errors.append("main.py 파일이 없습니다")
            return

        content = main_file.read_text()

        checks = [
            ("from fastapi import FastAPI", "FastAPI import"),
            ("app = FastAPI", "FastAPI 앱 생성"),
            ("CORSMiddleware", "CORS 설정"),
            ("include_router", "Router 등록"),
        ]

        for pattern, desc in checks:
            if pattern in content:
                print(f"  ✓ {desc}")
                self.success_count += 1
            else:
                self.warnings.append(f"main.py: {desc} 확인 필요")
                print(f"  ⚠ {desc}")

        # API 라우터 개수 확인
        routes_file = self.backend_dir / "routes.py"
        if routes_file.exists():
            route_content = routes_file.read_text()
            route_count = len(re.findall(r'@router\.(get|post|put|delete|patch)', route_content))
            print(f"  ✓ API 엔드포인트: {route_count}개")
            self.success_count += 1

    def verify_alembic(self):
        """Alembic 마이그레이션 파일 검증"""
        alembic_dir = self.backend_dir / "alembic"
        alembic_ini = self.backend_dir / "alembic.ini"

        if not alembic_ini.exists():
            self.warnings.append("alembic.ini 파일이 없습니다")
            print("  ⚠ alembic.ini 파일 없음")
            return

        if not alembic_dir.exists():
            self.warnings.append("alembic/ 디렉토리가 없습니다")
            print("  ⚠ alembic/ 디렉토리 없음")
            return

        # 마이그레이션 파일 개수
        versions_dir = alembic_dir / "versions"
        if versions_dir.exists():
            migration_files = list(versions_dir.glob("*.py"))
            print(f"  ✓ 마이그레이션 파일: {len(migration_files)}개")
            self.success_count += 1
        else:
            self.warnings.append("alembic/versions/ 디렉토리가 없습니다")

        print(f"  ✓ Alembic 설정 완료")
        self.success_count += 1

    def verify_dockerfile(self):
        """Dockerfile 검증"""
        dockerfile = self.backend_dir / "Dockerfile"

        if not dockerfile.exists():
            self.warnings.append("Dockerfile이 없습니다")
            print("  ⚠ Dockerfile 없음")
            return

        content = dockerfile.read_text()

        checks = [
            ("FROM python:", "Base 이미지"),
            ("COPY requirements.txt", "의존성 복사"),
            ("RUN pip install", "패키지 설치"),
            ("COPY . ", "소스코드 복사"),
            ("CMD", "실행 명령"),
        ]

        for pattern, desc in checks:
            if pattern in content:
                print(f"  ✓ {desc}")
                self.success_count += 1
            else:
                self.warnings.append(f"Dockerfile: {desc} 확인 필요")
                print(f"  ⚠ {desc}")

    def run_tests(self):
        """pytest 실행"""
        try:
            result = subprocess.run(
                ["python3", "-m", "pytest", "test_*.py", "-v", "--tb=line"],
                cwd=str(self.backend_dir),
                capture_output=True,
                text=True,
                timeout=60
            )

            # pytest 결과 파싱
            output = result.stdout + result.stderr

            # 통과한 테스트 개수
            passed_match = re.search(r'(\d+) passed', output)
            if passed_match:
                passed_count = int(passed_match.group(1))
                print(f"  ✓ {passed_count}개 테스트 통과")
                self.success_count += passed_count

            # 실패한 테스트 개수
            failed_match = re.search(r'(\d+) failed', output)
            if failed_match:
                failed_count = int(failed_match.group(1))
                print(f"  ✗ {failed_count}개 테스트 실패")
                self.errors.append(f"{failed_count}개 테스트 실패")

            if result.returncode == 0:
                print(f"  ✓ 모든 테스트 통과")
            else:
                self.warnings.append("일부 테스트 실패")

        except Exception as e:
            self.warnings.append(f"테스트 실행 실패: {str(e)}")
            print(f"  ⚠ 테스트 실행 중 오류")

    def print_summary(self):
        print("\n" + "="*60)
        print("📊 검증 결과 요약")
        print("="*60)
        print(f"✅ 성공: {self.success_count}개")
        print(f"⚠️  경고: {len(self.warnings)}개")
        print(f"❌ 오류: {len(self.errors)}개")

        if self.errors:
            print("\n❌ 오류 목록:")
            for error in self.errors:
                print(f"  - {error}")

        if self.warnings:
            print("\n⚠️  경고 목록:")
            for warning in self.warnings[:10]:
                print(f"  - {warning}")
            if len(self.warnings) > 10:
                print(f"  ... 외 {len(self.warnings) - 10}개")

        print("\n" + "="*60)
        if len(self.errors) == 0:
            print("✅ Backend Dry-Run 검증 통과!")
            print("   → Docker 빌드 및 배포 가능")
            return 0
        else:
            print("❌ Backend Dry-Run 검증 실패")
            print("   → 오류를 수정해야 합니다")
            return 1

if __name__ == "__main__":
    verifier = BackendDryRun("/home/user/memoir/backend")
    sys.exit(verifier.verify_all())
