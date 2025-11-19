package com.memoir.app

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Memoir app
 * Initializes Kakao SDK and Hilt dependency injection
 */
@HiltAndroidApp
class MemoirApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Kakao SDK
        val kakaoAppKey = BuildConfig.KAKAO_APP_KEY
        if (kakaoAppKey.isNotEmpty()) {
            KakaoSdk.init(this, kakaoAppKey)
        }
    }
}
