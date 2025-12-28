package com.example.appandroid.screen

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Star // Thêm icon Star cho bài đã học
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.example.appandroid.utils.UserRole // Đảm bảo import đúng đường dẫn UserRole
import com.example.appandroid.viewmodel.AuthViewModel
import com.example.appandroid.viewmodel.LearnViewModel

// --- BẢNG MÀU ---
val MochiGreenLight = Color(0xFF81C784)
val MochiGreenMain = Color(0xFF4CAF50)
val MochiGreenDark = Color(0xFF388E3C)
val MochiLockedBg = Color(0xFFEEEEEE) // Màu nền xám nhạt hơn chút cho sạch
val MochiLockedText = Color(0xFF9E9E9E)

@Composable
fun LessonListScreen(
    navController: NavController,
    courseId: Long,
    viewModel: LearnViewModel,
    authViewModel: AuthViewModel // <--- 1. TRUYỀN AUTH VIEWMODEL
) {
    val lessons by viewModel.lessons.collectAsState()
    val courseTitle by viewModel.currentCourseTitle.collectAsState()
    val context = LocalContext.current

    // 2. LẤY ROLE CỦA USER
    val userRole by authViewModel.userRole.collectAsState()

    LaunchedEffect(courseId) {
        viewModel.loadLessons(courseId)
    }

    Scaffold(
        topBar = {
            // HEADER (Giữ nguyên)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        navController.navigate(ScreenRoutes.COURSE_LIST) {
                            popUpTo(ScreenRoutes.COURSE_LIST) { inclusive = true }
                        }
                    }
                    .padding(vertical = 14.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ic_bookcase), // Đảm bảo bạn có icon này
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = courseTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
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

                // 3. LOGIC KHÓA BÀI HỌC (QUAN TRỌNG NHẤT)
                // Bài bị khóa nếu là bài thứ 4 trở đi (index >= 3) VÀ User không phải VIP/Admin
                val isLockedByRole = (index >= 3) && (userRole != UserRole.PREMIUM && userRole != UserRole.ADMIN)

                LessonCardBouncy(
                    index = index + 1,
                    lesson = lesson,
                    isLocked = isLockedByRole, // Truyền trạng thái khóa vào Card
                    onClick = {
                        if (!isLockedByRole) {
                            navController.navigate("${ScreenRoutes.FLASHCARD}/${lesson.id}")
                        } else {
                            // Thông báo nhẹ khi bấm vào bài khóa
                            Toast.makeText(context, "Nâng cấp Premium để mở khóa toàn bộ!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LessonCardBouncy(
    index: Int,
    lesson: Lesson,
    isLocked: Boolean, // Nhận tham số khóa từ bên ngoài
    onClick: () -> Unit
) {
    val isLearned = lesson.isLearned

    // Màu sắc & Style
    val backgroundColor = when {
        isLocked -> MochiLockedBg
        isLearned -> MochiGreenMain
        else -> Color.White
    }

    val textColor = if (isLearned) Color.White else if (isLocked) MochiLockedText else Color.Black
    val descColor = if (isLearned) Color(0xFFE8F5E9) else Color.Gray

    // Border: Chỉ hiện viền xanh cho bài ĐANG HỌC (Chưa xong, chưa khóa)
    val borderStroke = if (!isLocked && !isLearned) {
        BorderStroke(2.dp, MochiGreenMain)
    } else null

    // Đổ bóng: Bài khóa không có bóng (phẳng lì)
    val shadowElevation = if (!isLocked) 6.dp else 0.dp

    // Hiệu ứng Lún khi bấm
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
                // Nếu bị khóa, làm mờ nhẹ cả card đi một chút
                alpha = if (isLocked) 0.8f else 1f
            }
            .shadow(
                elevation = shadowElevation,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (isLearned) MochiGreenMain else Color.Black.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .then(if (borderStroke != null) Modifier.border(borderStroke, RoundedCornerShape(20.dp)) else Modifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isLocked // Vô hiệu hóa click nếu bị khóa
            ) { onClick() }
    ) {
        // Gradient nền cho bài đã học (Làm đẹp hơn)
        val contentModifier = if (isLearned) {
            Modifier.background(
                Brush.linearGradient(
                    colors = listOf(MochiGreenLight, MochiGreenMain)
                )
            )
        } else Modifier

        Row(
            modifier = contentModifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- ẢNH BÀI HỌC ---
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(75.dp) // Tăng kích thước chút cho đẹp
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

                // Lớp phủ đen mờ lên ảnh nếu bị khóa
                if (isLocked) {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // --- NỘI DUNG CHỮ ---
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bài $index: ${lesson.title}", // Thêm prefix "Bài X"
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = lesson.description ?: "Chưa có mô tả",
                    fontSize = 13.sp,
                    color = descColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )
            }

            // --- ICON TRẠNG THÁI (Cuối dòng) ---
            Box(modifier = Modifier.padding(start = 8.dp)) {
                if (isLocked) {
                    // Icon Ổ khóa
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = MochiLockedText,
                        modifier = Modifier.size(26.dp)
                    )
                } else if (isLearned) {
                    // Icon Hoàn thành (Ngôi sao hoặc Check)
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Learned",
                        tint = Color.Yellow, // Ngôi sao vàng trên nền xanh
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}