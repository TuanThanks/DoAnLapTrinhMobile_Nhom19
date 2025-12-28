package com.example.appandroid

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.appandroid.data.SupabaseClient
import com.example.appandroid.navigation.AppNavigation
import com.example.appandroid.navigation.ScreenRoutes
import com.example.appandroid.ui.theme.AppAndroidTheme
import io.github.jan.supabase.gotrue.handleDeeplinks

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Vẫn giữ Splash mặc định của Android cho mượt
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        val supabase = SupabaseClient.client
        supabase.handleDeeplinks(intent = intent) {
            Log.d("Auth", "Deep link handled!")
        }

        // Tắt màn hình logo Android ngay lập tức để hiện màn hình Intro của bạn
        splashScreen.setKeepOnScreenCondition { false }

        setContent {
            AppAndroidTheme {
                // QUAN TRỌNG: Luôn bắt đầu bằng INTRO
                // IntroScreen bây giờ sẽ lo việc kiểm tra đăng nhập và chờ 1.5s
                AppNavigation(startDestination = ScreenRoutes.INTRO)
            }
        }
    }
}