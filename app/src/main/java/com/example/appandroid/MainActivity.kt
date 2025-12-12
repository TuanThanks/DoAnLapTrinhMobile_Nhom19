package com.example.appandroid

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.example.appandroid.data.SupabaseClient
import com.example.appandroid.navigation.AppNavigation
import com.example.appandroid.navigation.ScreenRoutes
import com.example.appandroid.ui.theme.AppAndroidTheme
import io.github.jan.supabase.gotrue.handleDeeplinks

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val supabase = SupabaseClient.client

        // 1. Xử lý Deep Link: Tự động nạp Token từ URL vào bộ nhớ
        // CHỈ CẦN DÒNG NÀY LÀ ĐỦ
        supabase.handleDeeplinks(intent = intent) {
            Log.d("Auth", "Deep link handled! Recovery session restored.")
        }

        // 2. Kiểm tra xem có phải link Reset Password không?
        val isRecovery = intent?.data?.host == "reset-callback"

        // ❌ ĐÃ XÓA ĐOẠN refreshCurrentSession() Ở ĐÂY ❌
        // Việc refresh lúc này sẽ làm hỏng token phục hồi vừa nhận được.

        setContent {
            AppAndroidTheme {
                // 3. Điều hướng
                val startDest = if (isRecovery) ScreenRoutes.RESET_PASSWORD else ScreenRoutes.SPLASH
                AppNavigation(startDestination = startDest)
            }
        }
    }
}