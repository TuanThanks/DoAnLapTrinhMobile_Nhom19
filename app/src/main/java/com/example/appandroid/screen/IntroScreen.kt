package com.example.appandroid.screen

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appandroid.R
import com.example.appandroid.data.SupabaseClient
import com.example.appandroid.navigation.ScreenRoutes
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
fun IntroScreen(navController: NavController) {

    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        // 1. Hiệu ứng Logo
        launch {
            alphaAnim.animateTo(1f, animationSpec = tween(800))
        }
        launch {
            scaleAnim.animateTo(1f, animationSpec = tween(800))
        }

        // 2. Vừa chờ hiệu ứng, vừa âm thầm kiểm tra Role
        val startTime = System.currentTimeMillis()

        // Kiểm tra session hiện tại
        val session = SupabaseClient.client.auth.currentSessionOrNull()
        var nextScreen = ScreenRoutes.LANDING // Mặc định là về Landing (Guest)

        if (session != null) {
            try {
                // Nếu đã đăng nhập, hỏi Supabase xem Role là gì?
                val userId = session.user?.id
                if (userId != null) {
                    val result = SupabaseClient.client
                        .from("profiles")
                        .select(columns = Columns.list("role")) {
                            filter { eq("id", userId) }
                        }
                        .decodeSingleOrNull<UserRoleCheck>()

                    // QUYẾT ĐỊNH ĐI ĐÂU?
                    nextScreen = if (result?.role == "admin") {
                        ScreenRoutes.ADMIN // Nếu là Admin -> Vào Admin Screen
                    } else {
                        ScreenRoutes.HOME  // Nếu là User/Premium -> Vào Home
                    }
                }
            } catch (e: Exception) {
                Log.e("Intro", "Lỗi check role: ${e.message}")
                // Lỗi mạng thì cứ cho vào Home (chế độ user thường) để đỡ kẹt
                nextScreen = ScreenRoutes.HOME
            }
        }

        // 3. Đảm bảo Intro hiện ít nhất 2 giây cho đẹp
        val elapsedTime = System.currentTimeMillis() - startTime
        if (elapsedTime < 2000) {
            delay(2000 - elapsedTime)
        }

        // 4. Chuyển màn hình
        navController.navigate(nextScreen) {
            popUpTo(ScreenRoutes.INTRO) { inclusive = true }
        }
    }

    // --- GIAO DIỆN (Giữ nguyên) ---
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