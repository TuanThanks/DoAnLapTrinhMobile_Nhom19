package com.example.appandroid.screen

import android.app.Activity
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appandroid.R
import com.example.appandroid.data.SupabaseClient
import com.example.appandroid.navigation.ScreenRoutes
import com.example.appandroid.viewmodel.AuthViewModel
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// Class nhỏ để hứng dữ liệu Role từ DB
@Serializable
data class UserRoleCheck(val role: String = "user")

@Composable
fun IntroScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel() // Inject ViewModel vào đây
) {

    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.8f) }
    val context = LocalContext.current
    val activity = context as? Activity
    val intentData = activity?.intent?.data
    // --- LOGIC FULL SCREEN ---
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onDispose {
            val windowOnExit = (context as? Activity)?.window
            if (windowOnExit != null) {
                val insetsController = WindowCompat.getInsetsController(windowOnExit, windowOnExit.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // --- 1. LOGIC LẮNG NGHE SỰ KIỆN RESET PASSWORD (QUAN TRỌNG) ---
    // Phải để riêng ra một LaunchedEffect để nó chạy song song và bắt sự kiện ngay lập tức
    LaunchedEffect(Unit) {
        authViewModel.navigateToResetPassword.collect { shouldNavigate ->
            if (shouldNavigate) {
                // Nếu nhận được tín hiệu Reset, chuyển ngay sang màn hình đổi mật khẩu
                Log.d("IntroScreen", "Nhận tín hiệu Reset Password -> Chuyển màn hình")
                navController.navigate(ScreenRoutes.RESET_PASSWORD) {
                    popUpTo(ScreenRoutes.INTRO) { inclusive = true }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        // 1. Chạy Animation
        launch { alphaAnim.animateTo(1f, animationSpec = tween(800)) }
        launch { scaleAnim.animateTo(1f, animationSpec = tween(800)) }

        val startTime = System.currentTimeMillis()

        // --- LOGIC MỚI: ƯU TIÊN KIỂM TRA RESET PASSWORD ---
        // Kiểm tra xem link mở app có phải là link reset không
        // Link Supabase thường có dạng: .../reset-callback#access_token=...&type=recovery...
        val isResetPasswordFlow = intentData?.toString()?.contains("reset-callback") == true ||
                intentData?.fragment?.contains("type=recovery") == true

        if (isResetPasswordFlow) {
            Log.d("Intro", "Phát hiện link Reset Password -> Chuyển sang màn hình đổi mật khẩu")
            // Đợi 1 chút để Supabase kịp xử lý session từ Deep Link
            delay(1500)

            navController.navigate(ScreenRoutes.RESET_PASSWORD) {
                popUpTo(ScreenRoutes.INTRO) { inclusive = true }
            }
        } else {
            // --- LOGIC CŨ: KIỂM TRA ĐĂNG NHẬP THƯỜNG ---
            // Chỉ chạy vào đây nếu KHÔNG PHẢI là link reset password

            val session = SupabaseClient.client.auth.currentSessionOrNull()
            var nextScreen = ScreenRoutes.LANDING

            if (session != null) {
                try {
                    val userId = session.user?.id
                    if (userId != null) {
                        val result = SupabaseClient.client
                            .from("profiles")
                            .select(columns = Columns.list("role")) {
                                filter { eq("id", userId) }
                            }
                            .decodeSingleOrNull<UserRoleCheck>()

                        nextScreen = ScreenRoutes.HOME
                    }
                } catch (e: Exception) {
                    Log.e("Intro", "Lỗi check role: ${e.message}")
                    nextScreen = ScreenRoutes.HOME
                }
            }

            // Đảm bảo delay tối thiểu 2s
            val elapsedTime = System.currentTimeMillis() - startTime
            if (elapsedTime < 2000) {
                delay(2000 - elapsedTime)
            }

            navController.navigate(nextScreen) {
                popUpTo(ScreenRoutes.INTRO) { inclusive = true }
            }
        }
    }

    // --- GIAO DIỆN ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alphaAnim.value)
                .scale(scaleAnim.value)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_app),
                contentDescription = "Logo",
                modifier = Modifier.size(180.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Flash English",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}