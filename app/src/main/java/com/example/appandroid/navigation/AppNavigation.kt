package com.example.appandroid.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appandroid.screen.AdminScreen
import com.example.appandroid.screen.CourseListScreen
import com.example.appandroid.screen.DictionaryScreen
import com.example.appandroid.screen.FlashcardScreen
import com.example.appandroid.screen.ForgotPasswordScreen
import com.example.appandroid.screen.HomeScreen
import com.example.appandroid.screen.IntroScreen
import com.example.appandroid.screen.LandingScreen
import com.example.appandroid.screen.LessonListScreen
import com.example.appandroid.screen.LoginScreen
import com.example.appandroid.screen.ProfileScreen
import com.example.appandroid.screen.RegisterScreenFirst
import com.example.appandroid.screen.RegisterScreenSecond
import com.example.appandroid.screen.ReviewFlashcardScreen
import com.example.appandroid.screen.ReviewScreen
import com.example.appandroid.screen.SplashScreen
import com.example.appandroid.viewmodel.AuthViewModel
import com.example.appandroid.viewmodel.LearnViewModel

// Định nghĩa các đường dẫn (Routes)
object ScreenRoutes {
    const val LOGIN = "login_screen"
    const val REGISTER_FIRST = "register_first_screen"
    const val REGISTER_SECOND = "register_second_screen"
    const val FORGOT_PASSWORD = "forgot_password_screen"
    const val HOME = "home_screen"
    const val COURSE_LIST = "course_list_screen" // Thêm dòng này
    const val LESSON_LIST = "lesson_list_screen/{courseId}"
    const val FLASHCARD = "flashcard_screen" // Màn hình 4
    const val REVIEW = "review_screen"
    const val SPLASH = "splash_screen"
    const val DICTIONARY = "dictionary_screen"
    const val PROFILE = "profile_screen"
    const val RESET_PASSWORD = "reset_password_screen" // Thêm dòng này
    const val INTRO = "intro_screen" // <--- Thêm cái này
    const val LANDING = "landing_screen"
    const val ADMIN = "admin_screen"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(startDestination: String = ScreenRoutes.SPLASH) {
    val navController = rememberNavController()
    // Khởi tạo ViewModel ở cấp độ Navigation để dùng chung hoặc truyền xuống
    val authViewModel: AuthViewModel = viewModel()
    val learnViewModel: LearnViewModel = viewModel()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(ScreenRoutes.SPLASH) {
            SplashScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(ScreenRoutes.LOGIN) {
            LoginScreen(navController = navController, viewModel = authViewModel)
        }

        composable(ScreenRoutes.REGISTER_FIRST) {
            RegisterScreenFirst(navController = navController, viewModel = authViewModel)
        }

        composable(ScreenRoutes.REGISTER_SECOND) {
            RegisterScreenSecond(navController = navController, viewModel = authViewModel)
        }

        composable(ScreenRoutes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(navController = navController, viewModel = authViewModel)
        }

        // Màn hình chính sau khi login thành công
        composable(ScreenRoutes.HOME) {
            // Truyền learnViewModel vào HomeScreen
            HomeScreen(navController = navController, viewModel = learnViewModel)
        }
        composable(ScreenRoutes.COURSE_LIST) {
            // Truyền learnViewModel vào màn hình
            CourseListScreen(navController = navController, viewModel = learnViewModel)
        }
// Tạm thời khai báo placeholder cho màn hình 3 để không lỗi khi click
// ...
        composable(
            route = ScreenRoutes.LESSON_LIST,
            arguments = listOf(navArgument("courseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getLong("courseId") ?: 0L

            // TRUYỀN VIEWMODEL VÀO ĐÂY
            LessonListScreen(
                navController = navController,
                courseId = courseId,
                viewModel = learnViewModel,
                authViewModel = authViewModel // <--- Truyền vào đây
            )
        }
// ...

// ...
        // MÀN HÌNH 4: FLASHCARD
        composable(
            route = "${ScreenRoutes.FLASHCARD}/{lessonId}", // Thêm tham số /{lessonId}
            arguments = listOf(navArgument("lessonId") { type = NavType.LongType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getLong("lessonId") ?: 0L

            FlashcardScreen(
                navController = navController,
                viewModel = learnViewModel, // Truyền ViewModel
                lessonId = lessonId         // Truyền ID bài học
            )
        }
// 2. Màn hình Ôn tập (Review)
        composable(ScreenRoutes.REVIEW) {
            // Gọi màn hình ReviewFlashcardScreen mới tạo
            ReviewFlashcardScreen(navController = navController, viewModel = learnViewModel)
        }
        composable(ScreenRoutes.DICTIONARY) {
            DictionaryScreen(navController = navController, viewModel = learnViewModel)
        }
        composable(ScreenRoutes.PROFILE) {
            ProfileScreen(navController = navController, authViewModel = authViewModel,learnViewModel = learnViewModel) // <--- TRUYỀN THÊM CÁI NÀY)
        }
        composable(ScreenRoutes.RESET_PASSWORD) {
            // Import ResetPasswordScreen nếu chưa có
            com.example.appandroid.screen.ResetPasswordScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }
        composable("intro_screen") {
            IntroScreen(navController = navController)
        }
        composable(ScreenRoutes.LANDING) {
            LandingScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(ScreenRoutes.ADMIN) {
            AdminScreen(navController = navController)
        }
    }
}