package com.example.appandroid.screen.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.appandroid.navigation.ScreenRoutes
import com.example.appandroid.utils.LocalStorage

// Màu vàng Mochi
val MochiYellow = Color(0xFFFFD600)

@Composable
fun MochiBottomBar(navController: NavController) {
    // Lấy tuyến đường (route) hiện tại để biết đang ở đâu
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    val context= LocalContext.current

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        // NÚT 1: HỌC TỪ VỰNG
        // Highlight nếu đang ở: CourseList HOẶC LessonList HOẶC Dictionary
        val isLearnSelected = currentRoute == ScreenRoutes.COURSE_LIST ||
                currentRoute?.startsWith("lesson_list_screen") == true ||
                currentRoute == ScreenRoutes.DICTIONARY

        NavigationBarItem(
            icon = { Icon(painterResource(android.R.drawable.ic_menu_search), contentDescription = "Học") },
            label = { Text("Học từ vựng") },
            selected = isLearnSelected,
// Ví dụ logic trong onClick của BottomBar Item "Học từ vựng"
            onClick = {
                val lastCourseId = LocalStorage(context).getLastCourseId()

                if (lastCourseId != -1L) {
                    // Đã từng chọn -> Vào thẳng bài học
                    navController.navigate("${ScreenRoutes.LESSON_LIST.substringBefore("/{")}/$lastCourseId") {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                } else {
                    // Chưa chọn bao giờ -> Vào danh sách khóa học để chọn
                    navController.navigate(ScreenRoutes.COURSE_LIST) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = MochiYellow)
        )

        // NÚT 2: ÔN TẬP (Màn hình Home chính là Dashboard ôn tập)
        val isReviewSelected = currentRoute == ScreenRoutes.HOME

        NavigationBarItem(
            icon = { Icon(painterResource(android.R.drawable.ic_menu_rotate), contentDescription = "Ôn tập") },
            label = { Text("Ôn tập") },
            selected = isReviewSelected,
            onClick = {
                if (!isReviewSelected) {
                    navController.navigate(ScreenRoutes.HOME) {
                        popUpTo(ScreenRoutes.HOME) { inclusive = true }
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = MochiYellow)
        )
        // --- NÚT 3: TRA TỪ (DICTIONARY) ---
        val isDictSelected = currentRoute == ScreenRoutes.DICTIONARY

        NavigationBarItem(
            // Dùng tạm icon có sẵn hoặc thay bằng R.drawable.ic_dictionary nếu có
            icon = { Icon(painterResource(android.R.drawable.ic_menu_sort_by_size), contentDescription = "Tra từ") },
            label = { Text("Tra từ") },
            selected = isDictSelected,
            onClick = {
                if (!isDictSelected) {
                    navController.navigate(ScreenRoutes.DICTIONARY) {
                        launchSingleTop = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = MochiYellow)
        )
    }
}