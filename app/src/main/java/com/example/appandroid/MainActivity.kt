package com.example.appandroid

import android.content.Intent // <--- Nhớ import cái này
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

        // 1. Xử lý Deep Link khi App mở từ trạng thái tắt hoàn toàn (Cold Start)
        val supabase = SupabaseClient.client
        supabase.handleDeeplinks(intent = intent) {
            Log.d("Auth", "Deep link handled in onCreate!")
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

    // --- BỔ SUNG QUAN TRỌNG ---
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Cập nhật Intent mới nhất để IntroScreen có thể đọc được dữ liệu Deep Link
        setIntent(intent)

        // Supabase xử lý auth
        SupabaseClient.client.handleDeeplinks(intent) {
            Log.d("Auth", "Deep link handled in onNewIntent!")
        }
    }
}