package com.example.appandroid.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appandroid.R
import com.example.appandroid.navigation.ScreenRoutes
import com.example.appandroid.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    // Lắng nghe kết quả kiểm tra
    val isLoggedIn by authViewModel.isUserLoggedIn.collectAsState()

    // 1. Gọi hàm kiểm tra ngay khi màn hình hiện lên
    LaunchedEffect(Unit) {
        // Tạo độ trễ giả 1.5 giây để hiện Logo cho đẹp (tùy chọn)
        delay(1500)
        authViewModel.checkLoginStatus()
    }

    // 2. Điều hướng dựa trên kết quả
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn == true) {
            // Đã đăng nhập -> Vào Home
            navController.navigate(ScreenRoutes.HOME) {
                popUpTo(ScreenRoutes.SPLASH) { inclusive = true } // Xóa Splash khỏi lịch sử back
            }
        } else if (isLoggedIn == false) {
            // Chưa đăng nhập -> Vào Login
            navController.navigate(ScreenRoutes.LOGIN) {
                popUpTo(ScreenRoutes.SPLASH) { inclusive = true }
            }
        }
        // Nếu là null thì nghĩa là đang kiểm tra, chưa làm gì cả
    }

    // 3. Giao diện (Logo app ở giữa màn hình)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            // Thay bằng logo App của bạn
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Logo",
            modifier = Modifier.size(150.dp)
        )
    }
}