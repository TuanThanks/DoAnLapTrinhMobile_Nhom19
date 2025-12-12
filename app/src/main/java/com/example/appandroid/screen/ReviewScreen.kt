package com.example.appandroid.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appandroid.viewmodel.LearnViewModel

// Màu Mochi (Khai báo lại nếu file này chưa có)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReviewScreen(
    navController: NavController,
    viewModel: LearnViewModel
) {
    // 1. Lấy danh sách cần ôn
    val reviewList by viewModel.reviewList.collectAsState()

    // 2. LOGIC SỬA ĐỔI: Luôn lấy phần tử đầu tiên của danh sách (Vì từ đã ôn sẽ bị xóa khỏi list)
    val currentVocab = reviewList.firstOrNull()

    // Trạng thái lật thẻ
    var isFlipped by remember { mutableStateOf(false) }

    // Animation xoay
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "flip"
    )

    // 3. QUAN TRỌNG: Reset mặt trước khi từ vựng thay đổi (Không dùng currentIndex nữa)
    LaunchedEffect(currentVocab) {
        isFlipped = false
    }

    // Tắt hiệu ứng ripple khi click
    val interactionSource = remember { MutableInteractionSource() }

    Scaffold(containerColor = Color(0xFFF9F9F9)) { padding ->
        if (reviewList.isEmpty()) {
            // Hết bài ôn -> Quay về Home
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉 Tuyệt vời! Hết bài ôn rồi.", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) { Text("Về trang chủ") }
                }
            }
        } else if (currentVocab != null) {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {

                // Header: Hiển thị số lượng còn lại
                Text(
                    text = "Còn lại: ${reviewList.size} từ",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                // CARD AREA
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationY = rotation
                                cameraDistance = 12f * density
                            }
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                // Chỉ cho phép lật xem đáp án, không cho lật ngược lại (UX tốt hơn)
                                if (!isFlipped) isFlipped = true
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (rotation <= 90f) {
                                // --- MẶT TRƯỚC (CÂU HỎI - NGHĨA VIỆT) ---
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Nghĩa là gì?", color = Color.Gray)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    // Chỉ hiện Nghĩa tiếng Việt
                                    Text(
                                        text = currentVocab.meaning,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MochiBlue,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(40.dp))
                                    Text("(Chạm để xem đáp án)", fontSize = 14.sp, color = Color.LightGray)
                                }
                            } else {
                                // --- MẶT SAU (ĐÁP ÁN - TIẾNG ANH) ---
                                Column(
                                    modifier = Modifier.graphicsLayer { rotationY = 180f },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = currentVocab.word,
                                        fontSize = 40.sp, // To hơn chút
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MochiGreen
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = currentVocab.phonetic ?: "", fontSize = 20.sp, color = Color.Gray)

                                    Spacer(modifier = Modifier.height(32.dp))
                                    Divider(modifier = Modifier.width(100.dp), color = Color(0xFFEEEEEE))
                                    Spacer(modifier = Modifier.height(32.dp))

                                    Text(
                                        text = currentVocab.meaning,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }

                // FOOTER BUTTONS (Chỉ hiện khi đã lật thẻ để xem đáp án)
                // Dùng Box để giữ chỗ, tránh giao diện bị nhảy khi nút hiện ra
                Box(modifier = Modifier.height(80.dp).fillMaxWidth()) {
                    if (isFlipped) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Nút QUÊN (Đỏ)
                            Button(
                                onClick = {
                                    // Gửi kết quả: Quên -> ViewModel sẽ xóa từ này khỏi list hiển thị
                                    viewModel.submitReviewResult(currentVocab.id, currentLevel = 1, isRemembered = false)
                                    // Không cần chỉnh index vì list tự trôi
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MochiRed),
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Text("Quên", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Nút NHỚ (Xanh)
                            Button(
                                onClick = {
                                    // Gửi kết quả: Nhớ
                                    viewModel.submitReviewResult(currentVocab.id, currentLevel = 1, isRemembered = true)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MochiGreen),
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Text("Nhớ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    } else {
                        // Trạng thái chờ lật thẻ (Có thể để trống hoặc text hướng dẫn)
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Hãy nhớ lại nghĩa của từ vựng", color = Color.LightGray)
                        }
                    }
                }
            }
        }
    }
}