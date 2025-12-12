package com.example.appandroid.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.appandroid.viewmodel.AuthViewModel
import com.example.appandroid.viewmodel.LearnViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    learnViewModel: LearnViewModel
) {
    val email = authViewModel.getCurrentUserEmail() ?: "User"
    val name = authViewModel.getCurrentUserName() ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }

    // Lấy thống kê
    val stats by learnViewModel.stats.collectAsState()

    val streak by learnViewModel.streak.collectAsState()

    LaunchedEffect(Unit) {
        learnViewModel.loadUserStats()
        learnViewModel.loadStreak() // Sẽ làm sau
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .background(Color.White, CircleShape)
                        .shadow(2.dp, CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MochiTextDark)
                }
                Text(
                    text = "Hồ sơ cá nhân",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MochiTextDark,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        },
        containerColor = MochiGrayBg
    ) { padding ->
        // Dùng BOX để xếp chồng lớp (Layer)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // LAYER 1: Nội dung cuộn được
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()) // <--- CHO PHÉP CUỘN
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // 1. Avatar
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(10.dp, CircleShape)
                        .background(Color.White, CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_avatar),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Info
                Text(text = name, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = MochiTextDark)
                Text(text = email, fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.Medium)

                Spacer(modifier = Modifier.height(40.dp))

                // 3. Stats Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 24.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(count = stats.totalWords.toString(), label = "Từ đã học", iconColor = MochiGreen)
                        Divider(color = Color(0xFFEEEEEE), modifier = Modifier.height(50.dp).width(1.dp))
                        StatItem(count = stats.reviewCount.toString(), label = "Cần ôn tập", iconColor = MochiRed)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Streak Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.TrendingUp, null, tint = MochiOrange, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Chuỗi ngày học (Streak)", fontSize = 14.sp, color = Color.Gray)
                            // Tạm thời hiển thị số 1, sẽ sửa sau
                            Text("🔥 $streak ngày", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MochiTextDark)                        }
                    }
                }

                // QUAN TRỌNG: Spacer cuối cùng để đẩy nội dung lên, không bị nút đè lên
                Spacer(modifier = Modifier.height(100.dp))
            }

            // LAYER 2: Nút Đăng xuất NỔI (Sticky Bottom)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter) // Luôn nằm dưới đáy
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, MochiGrayBg, MochiGrayBg)
                        )
                    ) // Hiệu ứng mờ nền
                    .padding(24.dp)
            ) {
                Button(
                    onClick = { authViewModel.logout(navController) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFE53935))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Đăng xuất", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                }
            }
        }
    }
}

// Giữ nguyên hàm StatItem cũ
@Composable
fun StatItem(count: String, label: String, iconColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = iconColor)
        Text(text = label, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}