package com.example.appandroid.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.appandroid.R
import com.example.appandroid.model.Lesson
import com.example.appandroid.navigation.ScreenRoutes
import com.example.appandroid.screen.components.MochiBottomBar
import com.example.appandroid.viewmodel.LearnViewModel

// --- BẢNG MÀU ---
val MochiGreenLight = Color(0xFF81C784)
val MochiGreenMain = Color(0xFF4CAF50)
val MochiGreenDark = Color(0xFF388E3C)
val MochiLockedBg = Color(0xFFF0F0F0)
val MochiLockedText = Color(0xFF9E9E9E)

@Composable
fun LessonListScreen(
    navController: NavController,
    courseId: Long,
    viewModel: LearnViewModel
) {
    val lessons by viewModel.lessons.collectAsState()

    // 1. LẤY TÊN KHÓA HỌC TỪ VIEWMODEL (Động)
    val courseTitle by viewModel.currentCourseTitle.collectAsState()

    LaunchedEffect(courseId) {
        viewModel.loadLessons(courseId)
    }

    Scaffold(
        topBar = {
            // HEADER CHUẨN MOCHI
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Quay lại màn hình chọn khóa học
                        navController.navigate(ScreenRoutes.COURSE_LIST) {
                            popUpTo(ScreenRoutes.COURSE_LIST) { inclusive = true }
                        }
                    }
                    .padding(vertical = 14.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Icon Tủ Sách
                    Image(
                        painter = painterResource(R.drawable.ic_bookcase),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Tên Khóa Học (DYNAMIC)
                    Text(
                        text = courseTitle, // Sử dụng biến lấy từ API
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black, // In đậm
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Mũi tên trỏ xuống
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        bottomBar = { MochiBottomBar(navController) },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 20.dp)
        ) {
            itemsIndexed(lessons) { index, lesson ->
                LessonCardBouncy(
                    index = index + 1,
                    lesson = lesson,
                    onClick = {
                        if (!lesson.isLocked) {
                            navController.navigate("${ScreenRoutes.FLASHCARD}/${lesson.id}")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LessonCardBouncy(index: Int, lesson: Lesson, onClick: () -> Unit) {
    val isLocked = lesson.isLocked
    val isLearned = lesson.isLearned

    // Màu sắc
    val backgroundColor = when {
        isLocked -> MochiLockedBg
        isLearned -> MochiGreenMain
        else -> Color.White
    }

    val textColor = if (isLearned) Color.White else if (isLocked) MochiLockedText else Color.Black
    val descColor = if (isLearned) Color(0xFFE8F5E9) else Color.Gray

    // Viền: Bài chưa học có viền xanh
    val borderStroke = if (!isLocked && !isLearned) {
        BorderStroke(2.dp, MochiGreenMain)
    } else null

    val shadowElevation = if (!isLocked) 4.dp else 0.dp

    // Hiệu ứng Lún
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val targetScale = if (isPressed && !isLocked) 0.96f else 1f

    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 100),
        label = "bouncy"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = shadowElevation,
                shape = RoundedCornerShape(20.dp),
                spotColor = MochiGreenDark
            )
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .then(if (borderStroke != null) Modifier.border(borderStroke, RoundedCornerShape(20.dp)) else Modifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isLocked
            ) { onClick() }
    ) {
        // Nội dung Gradient cho bài đã học
        val contentModifier = if (isLearned) {
            Modifier.background(Brush.verticalGradient(listOf(MochiGreenLight, MochiGreenMain)))
        } else Modifier

        Row(
            modifier = contentModifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ảnh
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(70.dp)
                    .border(3.dp, Color.White, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(lesson.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                    error = painterResource(android.R.drawable.ic_menu_report_image),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isLocked) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Chữ
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$index. ${lesson.title}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = lesson.description ?: "Không có mô tả",
                    fontSize = 14.sp,
                    color = descColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }

            // Lock Icon
            if (isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MochiLockedText,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}