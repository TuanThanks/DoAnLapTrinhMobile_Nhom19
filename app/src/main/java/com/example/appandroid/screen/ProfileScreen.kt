package com.example.appandroid.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.appandroid.R
import com.example.appandroid.navigation.ScreenRoutes
import com.example.appandroid.utils.LocalStorage
import com.example.appandroid.utils.ReminderScheduler
import com.example.appandroid.utils.UserRole
import com.example.appandroid.viewmodel.AuthViewModel
import com.example.appandroid.viewmodel.LearnViewModel

// --- MÀU SẮC ---
val MochiGreen = Color(0xFF58CC02) // Màu xanh chuẩn Mochi/Duolingo
val MochiRed = Color(0xFFFF4B4B)
val MochiOrange = Color(0xFFFF9600)
val PremiumGold = Color(0xFFFFD700)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    learnViewModel: LearnViewModel
) {
    val context = LocalContext.current

    // 1. Lấy Role hiện tại
    val userRole by authViewModel.userRole.collectAsState()

    // 2. Lấy thông tin user (Nếu không phải Guest)
    val email = authViewModel.getCurrentUserEmail() ?: "Khách"
    val name = authViewModel.getCurrentUserName() ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }

    // Data thống kê
    val stats by learnViewModel.stats.collectAsState()
    val streak by learnViewModel.streak.collectAsState()

    LaunchedEffect(Unit) {
        if (userRole != UserRole.GUEST) {
            learnViewModel.loadUserStats()
            learnViewModel.loadStreak()
        }
    }

    // --- LOGIC NHẮC NHỞ ---
    var isReminderEnabled by remember {
        mutableStateOf(LocalStorage.isReminderEnabled(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isReminderEnabled = true
            LocalStorage.setReminderEnabled(context, true)
            ReminderScheduler.scheduleNextReminder(context)
            Toast.makeText(context, "Đã bật! App sẽ nhắc sau 24h nếu bạn không học.", Toast.LENGTH_SHORT).show()
        } else {
            isReminderEnabled = false
            Toast.makeText(context, "Cần cấp quyền để nhận thông báo", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .background(Color.Transparent, CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MochiTextDark)
                }
                Text(
                    text = if (userRole == UserRole.GUEST) "Chế độ Khách" else "Hồ sơ cá nhân",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MochiTextDark,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        },
        containerColor = MochiGrayBg
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(30.dp))

                // ============================
                // PHẦN 1: AVATAR & THÔNG TIN
                // ============================
                Box(contentAlignment = Alignment.Center) {
                    // Viền Premium (Nếu có)
                    if (userRole == UserRole.PREMIUM) {
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000))))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .shadow(8.dp, CircleShape)
                            .background(Color.White, CircleShape)
                            .border(4.dp, Color.White, CircleShape)
                    ) {
                        Image(
                            // Dùng icon cute cho Guest, avatar thường cho User
                            painter = painterResource(if (userRole == UserRole.GUEST) R.drawable.ic_avatar else R.drawable.ic_avatar), // Thay ic_avatar bằng ic_launcher hoặc ảnh thật
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    }

                    // Huy hiệu Premium
                    if (userRole == UserRole.PREMIUM) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Premium",
                            tint = PremiumGold,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-4).dp, y = (-4).dp)
                                .background(Color.White, CircleShape)
                                .border(2.dp, MochiGrayBg, CircleShape)
                                .padding(6.dp)
                                .size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (userRole == UserRole.GUEST) {
                    Text(text = "Bạn đang là Khách", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MochiTextDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Đăng ký để lưu tiến độ, học không giới hạn và đồng bộ trên mọi thiết bị!",
                        fontSize = 15.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                } else {
                    Text(text = name, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MochiTextDark)
                    if (userRole == UserRole.PREMIUM) {
                        Text(
                            text = "👑 THÀNH VIÊN PREMIUM",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MochiOrange,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Text(text = email, fontSize = 15.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ============================
                // PHẦN 2: GIAO DIỆN THEO ROLE
                // ============================
// --- KHU VỰC ADMIN (Chỉ hiện nếu là Admin) ---
                if (userRole == UserRole.ADMIN) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)), // Màu xanh nhạt
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Khu vực Quản Trị",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { navController.navigate(ScreenRoutes.ADMIN) },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("VÀO TRANG QUẢN LÝ", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                if (userRole == UserRole.GUEST) {
                    // --- GIAO DIỆN KHÁCH ---

                    // Nút Đăng Ký (Nổi bật)
                    Button(
                        onClick = { navController.navigate(ScreenRoutes.REGISTER_FIRST) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MochiGreen)
                    ) {
                        Text("TẠO TÀI KHOẢN MIỄN PHÍ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nút Đăng Nhập (Viền)
                    OutlinedButton(
                        onClick = { navController.navigate(ScreenRoutes.LOGIN) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE0E0E0)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MochiTextDark)
                    ) {
                        Text("TÔI ĐÃ CÓ TÀI KHOẢN", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                } else {
                    // --- GIAO DIỆN USER / PREMIUM ---

                    // 1. Stats Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 20.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(count = stats.totalWords.toString(), label = "Từ vựng", iconColor = MochiGreen)
                            Divider(color = Color(0xFFEEEEEE), modifier = Modifier.height(40.dp).width(1.dp))
                            StatItem(count = stats.reviewCount.toString(), label = "Cần ôn", iconColor = MochiOrange)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Streak Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White), // Nền trắng sạch sẽ hơn
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFFFF3E0), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.TrendingUp, null, tint = MochiOrange)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Chuỗi ngày học (Streak)", fontSize = 14.sp, color = Color.Gray)
                                Text("$streak ngày liên tiếp 🔥", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MochiTextDark)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Cài đặt nhắc nhở
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFE8EAF6), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.NotificationsActive, null, tint = Color(0xFF5C6BC0))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Nhắc nhở hằng ngày", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MochiTextDark)
                                    Text("Thông báo nếu quên học", fontSize = 13.sp, color = Color.Gray)
                                }
                            }

                            Switch(
                                checked = isReminderEnabled,
                                onCheckedChange = { isChecked ->
                                    // Logic permission cũ của bạn
                                    if (isChecked) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            val hasPerm = ContextCompat.checkSelfPermission(
                                                context, Manifest.permission.POST_NOTIFICATIONS
                                            ) == PackageManager.PERMISSION_GRANTED
                                            if (hasPerm) {
                                                isReminderEnabled = true
                                                LocalStorage.setReminderEnabled(context, true)
                                                ReminderScheduler.scheduleNextReminder(context)
                                                Toast.makeText(context, "Đã bật nhắc nhở", Toast.LENGTH_SHORT).show()
                                            } else {
                                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            }
                                        } else {
                                            isReminderEnabled = true
                                            LocalStorage.setReminderEnabled(context, true)
                                            ReminderScheduler.scheduleNextReminder(context)
                                            Toast.makeText(context, "Đã bật nhắc nhở", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        isReminderEnabled = false
                                        LocalStorage.setReminderEnabled(context, false)
                                        ReminderScheduler.cancelReminder(context)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MochiGreen,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFE0E0E0)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Nút Đăng Xuất (Chỉ hiện cho User/Premium)
                    OutlinedButton(
                        onClick = { authViewModel.logout(navController) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFFFEBEE),
                            contentColor = MochiRed
                        )
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = MochiRed)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("ĐĂNG XUẤT", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun StatItem(count: String, label: String, iconColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = iconColor)
        Text(text = label, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}