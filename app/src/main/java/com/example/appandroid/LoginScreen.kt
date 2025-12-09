package com.example.appandroid.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appandroid.R
import com.example.appandroid.navigation.ScreenRoutes
import com.example.appandroid.viewmodel.AuthState
import com.example.appandroid.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()

    // Lắng nghe trạng thái từ ViewModel
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                navController.navigate(ScreenRoutes.HOME) {
                    popUpTo(ScreenRoutes.LOGIN) { inclusive = true }
                }
            }
            is AuthState.Error -> {
                val msg = (authState as AuthState.Error).message
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    val isLoginEnabled = email.isNotBlank() && password.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ... (Giữ nguyên UI Box close button cũ của bạn) ...
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
            IconButton(onClick = { /* Handle close */ }) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Đăng nhập để học ngay nhé", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(24.dp))

        // ... (Nút Google giữ nguyên hoặc xử lý sau) ...
        Button(
            onClick = { /* TODO: Google Sign In logic */ },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB4437)), // Hardcode màu đỏ google
            shape = RoundedCornerShape(25.dp)
        ) {
            Text("Tiếp tục với Gmail", color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("HOẶC", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        // Email Input
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Nhập email tài khoản học", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Nhập mật khẩu", color = Color.Gray) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, "Show/Hide")
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // NÚT ĐĂNG NHẬP GỌI VIEWMODEL
        Button(
            onClick = {
                if (isLoginEnabled) {
                    viewModel.login(email, password)
                }
            },
            enabled = isLoginEnabled,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                // Lưu ý: Nếu không có R.color.blue thì thay bằng Color.Blue
                containerColor = if (isLoginEnabled) Color.Blue else Color(0xFFE0E0E0)
            ),
            shape = RoundedCornerShape(25.dp)
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Đăng nhập", color = Color.White, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { navController.navigate(ScreenRoutes.FORGOT_PASSWORD) }) {
            Text("Quên mật khẩu", color = Color(0xFF2196F3))
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Chưa có tài khoản? ", color = Color.Gray)
            TextButton(onClick = { navController.navigate(ScreenRoutes.REGISTER_FIRST) }) {
                Text("Tạo tài khoản", color = Color(0xFF2196F3))
            }
        }
    }
}