package com.example.appandroid.screen.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.appandroid.R
import com.example.appandroid.screen.MochiGreen

@Composable
fun MochiProgressBar(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    height: Dp = 12.dp
) {
    // Tính toán phần trăm (0.0 -> 1.0)
    // Đảm bảo không chia cho 0
    val targetProgress = if (totalSteps > 0) currentStep.toFloat() / totalSteps else 0f

    // Animation mượt mà
    val progress by animateFloatAsState(
        targetValue = targetProgress.coerceIn(0f, 1f), // Giới hạn 0-1
        label = "progress"
    )

    BoxWithConstraints(
        modifier = modifier
            .height(30.dp) // Chiều cao tổng thể (bao gồm cả icon lòi ra)
    ) {
        val width = maxWidth

        // 1. Thanh Background (Màu xám)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .background(Color(0xFFE0E0E0))
        )

        // 2. Thanh Progress (Màu xanh)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(width * progress) // Chiều dài thay đổi theo progress
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .background(MochiGreen)
        )

        // 3. Icon Mochi (Chạy theo đầu thanh)
        // Dùng offset x để dịch chuyển
        Image(
            painter = painterResource(id = R.drawable.ic_app), // Icon con sâu/mochi
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(30.dp) // Kích thước icon
                .align(Alignment.CenterStart)
                .offset(x = (width * progress) - 15.dp) // Trừ 1 nửa size để tâm icon nằm đúng đầu thanh
        )
    }
}