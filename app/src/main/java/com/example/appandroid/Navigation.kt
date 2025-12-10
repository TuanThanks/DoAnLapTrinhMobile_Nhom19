package com.example.appandroid.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appandroid.screen.ForgotPasswordScreen
import com.example.appandroid.screen.HomeScreen
import com.example.appandroid.screen.LoginScreen
import com.example.appandroid.screen.RegisterScreenFirst
import com.example.appandroid.screen.RegisterScreenSecond
import com.example.appandroid.viewmodel.AuthViewModel

// Định nghĩa các đường dẫn (Routes)
object ScreenRoutes {
    const val LOGIN = "login_screen"
    const val REGISTER_FIRST = "register_first_screen"
    const val REGISTER_SECOND = "register_second_screen"
    const val FORGOT_PASSWORD = "forgot_password_screen"
    const val HOME = "home_screen"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Khởi tạo ViewModel ở cấp độ Navigation để dùng chung hoặc truyền xuống
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = ScreenRoutes.LOGIN) {

        composable(ScreenRoutes.LOGIN) {
            LoginScreen(navController = navController, viewModel = authViewModel)
        }

        composable(ScreenRoutes.REGISTER_FIRST) {
            RegisterScreenFirst(navController = navController)
        }

        composable(ScreenRoutes.REGISTER_SECOND) {
            RegisterScreenSecond(navController = navController, viewModel = authViewModel)
        }



        // Màn hình chính sau khi login thành công
        composable(ScreenRoutes.HOME) {
            HomeScreen(onLogout = {
                authViewModel.logout()
                navController.navigate(ScreenRoutes.LOGIN) {
                    // Xóa tất cả các màn hình trước đó khỏi back stack
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                }
            })
        }
    }
}