package com.example.appandroid.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appandroid.navigation.ScreenRoutes
import com.example.appandroid.viewmodel.AuthViewModel

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Tạo mật khẩu mới", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        // Ô nhập mật khẩu mới
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("Mật khẩu mới") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Nút Xác nhận
        Button(
            onClick = {
                if (newPassword.length < 6) {
                    Toast.makeText(context, "Mật khẩu phải trên 6 ký tự", Toast.LENGTH_SHORT).show()
                } else {
                    // Gọi hàm đổi mật khẩu trong ViewModel
                    viewModel.updatePassword(newPassword) { success ->
                        if (success) {
                            Toast.makeText(context, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show()
                            // Đổi xong thì về trang Home hoặc Login
                            navController.navigate(ScreenRoutes.HOME) {
                                popUpTo(ScreenRoutes.LOGIN) { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "Lỗi đổi mật khẩu", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
        ) {
            Text("Đổi mật khẩu", color = Color.White)
        }
    }
}