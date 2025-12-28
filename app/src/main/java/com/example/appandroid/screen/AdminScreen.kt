package com.example.appandroid.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appandroid.viewmodel.AdminViewModel
import com.example.appandroid.viewmodel.UserProfile

// Màu cho Admin
val AdminBlue = Color(0xFF2196F3)
val AdminBg = Color(0xFFF0F4C3) // Màu nền hơi vàng nhạt cho khác biệt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel() // Tự khởi tạo ViewModel
) {
    val users by viewModel.users.collectAsState()
    val totalUsers by viewModel.totalUsers.collectAsState()
    val premiumCount by viewModel.premiumCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    // Load dữ liệu khi vào màn hình
    LaunchedEffect(Unit) {
        viewModel.fetchUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản Trị Viên (Admin)", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminBlue)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // 1. THỐNG KÊ
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminStatCard(title = "Tổng User", count = totalUsers.toString(), color = Color(0xFF4CAF50), modifier = Modifier.weight(1f))
                AdminStatCard(title = "Premium", count = premiumCount.toString(), color = Color(0xFFFF9800), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. THANH TÌM KIẾM
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchUsers(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tìm kiếm theo email...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. DANH SÁCH USER
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users) { user ->
                        UserItemCard(
                            user = user,
                            onUpgrade = { viewModel.updateUserRole(user.id, "premium") },
                            onDowngrade = { viewModel.updateUserRole(user.id, "user") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(title: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(title, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun UserItemCard(
    user: UserProfile,
    onUpgrade: () -> Unit,
    onDowngrade: () -> Unit
) {
    val isPremium = user.role == "premium"
    val isAdmin = user.role == "admin"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Role
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        when {
                            isAdmin -> AdminBlue
                            isPremium -> Color(0xFFFFD700) // Vàng
                            else -> Color.LightGray
                        },
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPremium || isAdmin) Icons.Default.Star else Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Email & Role
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.email ?: "No Email",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = "Role: ${user.role.uppercase()}",
                    fontSize = 12.sp,
                    color = if (isPremium) Color(0xFFFF9800) else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }

            // Nút Thao tác
            if (!isAdmin) { // Không cho sửa Admin khác
                if (isPremium) {
                    // Nút Hạ cấp (Chỉ hiện text nhỏ hoặc icon để đỡ bấm nhầm)
                    TextButton(onClick = onDowngrade) {
                        Text("Hạ cấp", color = Color.Red, fontSize = 12.sp)
                    }
                } else {
                    // Nút Nâng cấp
                    Button(
                        onClick = onUpgrade,
                        colors = ButtonDefaults.buttonColors(containerColor = AdminBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Up VIP", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}