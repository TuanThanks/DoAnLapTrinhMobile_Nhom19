package com.example.appandroid.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appandroid.navigation.ScreenRoutes
import com.example.appandroid.viewmodel.AuthState
import com.example.appandroid.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel // Nhận ViewModel vào để xử lý logic
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }

    // Lắng nghe trạng thái từ Firebase trả về
    val authState by viewModel.authState.collectAsState()

    // Xử lý sự kiện khi gửi thành công hoặc thất bại
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                Toast.makeText(context, "Đã gửi email đặt lại mật khẩu, vui lòng kiểm tra hộp thư!", Toast.LENGTH_LONG).show()
                viewModel.resetState() // Reset lại trạng thái để tránh lỗi lặp lại
                navController.navigate(ScreenRoutes.LOGIN) {
                    popUpTo(ScreenRoutes.LOGIN) { inclusive = true }
                }
            }
            is AuthState.Error -> {
                val message = (authState as AuthState.Error).message
                Toast.makeText(context, "Lỗi: $message", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    val isForgotEnable = email.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Nút X (góc trên trái) - Quay lại màn hình Login
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopStart
        ) {
            IconButton(
                onClick = {
                    navController.navigate(ScreenRoutes.LOGIN)
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Đóng",
                    tint = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Tiêu đề
        Text(
            text = "Quên mật khẩu",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Mô tả
        Text(
            text = "Cấp phát email chính xác để nhận mã xác thực",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // TextField Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Nhập email của bạn", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                unfocusedContainerColor = Color(0xFFF5F5F5),
                focusedContainerColor = Color(0xFFF5F5F5)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Nút "Nhận mã ngay" - Gọi ViewModel
        Button(
            onClick = {
                if (isForgotEnable) {
                    viewModel.resetPassword(email)
                }
            },
            enabled = isForgotEnable,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isForgotEnable) {
                    // Nếu bạn có file màu trong res/values/colors.xml thì dùng: colorResource(R.color.blue)
                    // Ở đây mình dùng code cứng màu Blue để tránh lỗi
                    Color.Blue
                } else Color(0xFFE0E0E0),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(25.dp)
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "Nhận mã ngay",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}