package com.example.appandroid.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appandroid.R
import com.example.appandroid.model.UserProgressStats
import com.example.appandroid.navigation.ScreenRoutes
import com.example.appandroid.screen.components.MochiBottomBar
import com.example.appandroid.viewmodel.LearnViewModel

// --- BẢNG MÀU CHUẨN ---
private val MochiBlueLight = Color(0xFF4FC3F7)
val MochiBlue = Color(0xFF2196F3)
private val MochiNavy = Color(0xFF1A237E)

private val BackgroundColor = Color(0xFFF7F9FC)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: LearnViewModel
) {
    val stats by viewModel.stats.collectAsState()

    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.loadUserStats()
        startAnimation = true
    }

    Scaffold(
        bottomBar = { MochiBottomBar(navController) },
        containerColor = BackgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            // (1) Header
            HeaderSection(
                onAvatarClick = { navController.navigate(ScreenRoutes.PROFILE) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // (2) Statistics Section
            StatsCard(totalWords = stats.totalWords)

            Spacer(modifier = Modifier.height(20.dp))

            // (3) Biểu đồ SRS
            SRSGraphSection(stats = stats, startAnimation = startAnimation)

            Spacer(modifier = Modifier.height(8.dp))

            // (4) Review CTA
            ReviewCTASection(
                wordsToReview = stats.reviewCount,
                onClick = {
                    if (stats.reviewCount > 0) {
                        navController.navigate(ScreenRoutes.REVIEW)
                    }
                }
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun HeaderSection(onAvatarClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "FlashEnglish",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFF8F00)
            )
            Text(
                text = "Học từ vựng mỗi ngày",
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }

        Box(
            modifier = Modifier
                .size(52.dp)
                .bounceClick(onClick = onAvatarClick)
                .shadow(6.dp, CircleShape)
                .background(Color.White, CircleShape)
                .border(2.dp, Color.White, CircleShape)
                .padding(2.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_avatar),
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
fun StatsCard(totalWords: Int) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MochiRed.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_my_calendar),
                    contentDescription = null,
                    tint = MochiRed,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text("Sổ tay đã có", fontSize = 14.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$totalWords", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MochiTextDark)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("từ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 2.dp))
                }
            }
        }
    }
}

@Composable
fun SRSGraphSection(stats: UserProgressStats, startAnimation: Boolean) {
    val levels = listOf(stats.level1Count, stats.level2Count, stats.level3Count, stats.level4Count, stats.level5Count)
    val maxVal = levels.maxOrNull()?.takeIf { it > 0 } ?: 1
    val barColors = listOf(MochiRed, MochiOrange, MochiBlueLight, MochiBlue, MochiNavy)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                "Biểu đồ ghi nhớ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MochiTextDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                levels.forEachIndexed { index, count ->
                    val heightRatio = count.toFloat() / maxVal.toFloat()
                    val displayRatio = if (count > 0 && heightRatio < 0.12f) 0.12f else heightRatio

                    val animatedHeight by animateFloatAsState(
                        targetValue = if (startAnimation) displayRatio else 0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                        label = "barHeight"
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (startAnimation) {
                            Text(
                                text = "$count",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .fillMaxHeight(0.85f * animatedHeight)
                                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(barColors[index].copy(alpha = 0.7f), barColors[index])
                                    )
                                )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "${index + 1}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MochiTextDark)
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewCTASection(wordsToReview: Int, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Chuẩn bị ôn tập: ",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Text(
                text = "$wordsToReview từ",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MochiTextDark
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .bounceClick(onClick = onClick)
                .fillMaxWidth()
                .height(50.dp)
                .shadow(8.dp, RoundedCornerShape(30.dp))
                .background(MochiGreen, RoundedCornerShape(30.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_media_play),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Ôn tập ngay",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun Modifier.bounceClick(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "pressScale"
    )

    return this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}
