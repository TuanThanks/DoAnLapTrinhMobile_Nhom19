package com.example.appandroid.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appandroid.navigation.ScreenRoutes
import com.example.appandroid.viewmodel.AuthState
import com.example.appandroid.viewmodel.AuthViewModel

@Composable
fun RegisterScreenSecond(
    navController: NavController,
    viewModel: AuthViewModel
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()

    // Xử lý sự kiện sau khi đăng ký
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            // Quay về màn hình Login để đăng nhập
            navController.navigate(ScreenRoutes.LOGIN) {
                popUpTo(ScreenRoutes.LOGIN) { inclusive = true }
            }
        } else if (authState is AuthState.Error) {
            Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            viewModel.resetState()
        }
    }

    val isEnableButton = name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopStart) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.background(Color.LightGray.copy(0.3f), CircleShape)
            ) {
                Icon(Icons.Default.Close, "Close", tint = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Cùng tạo 1 tài khoản\nFlash English nào", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(40.dp))

        // Các trường nhập liệu
        OutlinedTextField(value = name, onValueChange = { name = it }, placeholder = { Text("Tên của bạn") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, placeholder = { Text("Email") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp))
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Mật khẩu") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, "")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Nút Đăng ký gọi ViewModel
        Button(
            onClick = { if (isEnableButton) viewModel.register(name, email, password) },
            enabled = isEnableButton,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if(isEnableButton) Color.Blue else Color(0xFFE0E0E0)),
            shape = RoundedCornerShape(25.dp)
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Tiếp tục", color = Color.White)
            }
        }
    }
}