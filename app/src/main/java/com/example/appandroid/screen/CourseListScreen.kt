package com.example.appandroid.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appandroid.R
import com.example.appandroid.model.Course
import com.example.appandroid.navigation.ScreenRoutes
import com.example.appandroid.utils.LocalStorage
import com.example.appandroid.viewmodel.LearnViewModel

// --- BẢNG MÀU ---
val MochiYellowCard = Color(0xFFFFD600)
val MochiStartBadge = Color(0xFFFFC107)

@Composable
fun CourseListScreen(
    navController: NavController,
    viewModel: LearnViewModel
) {
    val context= LocalContext.current
    val courses by viewModel.courses.collectAsState()
    val localStorage = remember { LocalStorage(context) }
    LaunchedEffect(Unit) {
        viewModel.loadCourses()
    }

    Scaffold(
        topBar = {
            // HEADER CHUẨN MOCHI
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 14.dp), // Tăng padding dọc chút cho thoáng
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Icon Tủ sách (Giữ nguyên màu gốc của ảnh)
                Image(
                    painter = painterResource(R.drawable.ic_bookcase),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 2. Tiêu đề
                Text(
                    text = "DANH SÁCH KHÓA HỌC FLASH",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black, // In rất đậm
                    color = Color.Black
                )
            }
        },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(24.dp), // Khoảng cách rộng để thẻ START không bị dính
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                itemsIndexed(courses) { index, course ->
                    CourseCard(
                        course = course,
                        isFirst = index == 0,
                        onClick = {
                            // 1. LƯU LẠI ID KHÓA HỌC
                            localStorage.saveLastCourseId(course.id)

                            // 2. Chuyển màn hình
                            navController.navigate("${ScreenRoutes.LESSON_LIST.substringBefore("/{")}/${course.id}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CourseCard(course: Course, isFirst: Boolean, onClick: () -> Unit) {
    val cardColor = if (isFirst) MochiYellowCard else Color.White

    // --- HIỆU ỨNG LÚN (BOUNCY) ---
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Scale từ 1.0 xuống 0.96 khi ấn
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "bounce"
    )

    // Box tổng chịu ảnh hưởng animation scale
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .graphicsLayer { // Áp dụng scale
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null // Tắt hiệu ứng sóng nước để nhìn rõ hiệu ứng lún
            ) { onClick() }
    ) {
        // 1. CARD CHÍNH
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp) // Cố định chiều cao
                .padding(top = if (isFirst) 12.dp else 0.dp) // Đẩy xuống nếu có badge Start
                .shadow(
                    elevation = if (isFirst) 0.dp else 3.dp, // Khóa vàng thì phẳng, khóa trắng thì nổi
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color.LightGray
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Center // Căn giữa nội dung dọc
            ) {
                // Tên khóa học
                Text(
                    text = course.title.uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dòng 1: Icon Target
                InfoRow(
                    iconRes = R.drawable.ic_target,
                    text = course.descriptionVocab ?: "Mục tiêu khóa học"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Dòng 2: Icon Study
                InfoRow(
                    iconRes = R.drawable.ic_study,
                    text = course.description ?: "Từ vựng nền tảng"
                )
            }
        }

        // 2. BADGE START! (Chỉ hiện cho khóa đầu)
        if (isFirst) {
            Surface(
                color = MochiStartBadge,
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .padding(start = 24.dp) // Căn lề trái
                    .align(Alignment.TopStart) // Neo lên góc trên trái
            ) {
                Text(
                    text = "START!",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun InfoRow(iconRes: Int, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // SỬA LỖI ICON BỊ CHE MÀU:
        // Dùng tint = Color.Unspecified để hiện màu gốc của ảnh
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.Unspecified, // <--- QUAN TRỌNG: Giữ nguyên màu gốc của icon
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text,
            fontSize = 15.sp,
            color = Color.DarkGray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}