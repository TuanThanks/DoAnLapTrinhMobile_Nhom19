package com.example.appandroid.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appandroid.data.SupabaseClient
import com.example.appandroid.navigation.ScreenRoutes
import com.example.appandroid.viewmodel.AuthViewModel
import io.github.jan.supabase.gotrue.auth

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    // State lưu giá trị nhập
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // State ẩn/hiện mật khẩu
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmVisible by remember { mutableStateOf(false) }

    // State loading để khóa nút khi đang gửi
    var isLoading by remember { mutableStateOf(false) }

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
        Spacer(modifier = Modifier.height(8.dp))
        Text("Mật khẩu mới cần khác mật khẩu cũ", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        // --- Ô 1: MẬT KHẨU MỚI ---
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("Mật khẩu mới") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Ô 2: XÁC NHẬN MẬT KHẨU ---
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Nhập lại mật khẩu") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            visualTransformation = if (isConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isConfirmVisible = !isConfirmVisible }) {
                    Icon(imageVector = if (isConfirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- NÚT XÁC NHẬN ---
        Button(
            onClick = {
                // --- LOGIC KIỂM TRA LỖI (VALIDATION) ---
                when {
                    newPassword.isBlank() -> {
                        Toast.makeText(context, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show()
                    }
                    newPassword.length < 6 -> {
                        Toast.makeText(context, "Mật khẩu quá ngắn (tối thiểu 6 ký tự)", Toast.LENGTH_SHORT).show()
                    }
                    // (Tùy chọn) Kiểm tra mật khẩu có chứa số không
                    !newPassword.any { it.isDigit() } || !newPassword.any { it.isLetter() } -> {
                        Toast.makeText(context, "Mật khẩu cần chứa cả chữ và số để bảo mật hơn", Toast.LENGTH_SHORT).show()
                    }
                    confirmPassword.isBlank() -> {
                        Toast.makeText(context, "Vui lòng nhập xác nhận mật khẩu", Toast.LENGTH_SHORT).show()
                    }
                    newPassword != confirmPassword -> {
                        Toast.makeText(context, "Hai mật khẩu không khớp nhau!", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // --- HỢP LỆ -> GỌI API ---
                        isLoading = true
                        viewModel.updatePassword(newPassword) { success ->
                            isLoading = false // Tắt loading dù thành công hay thất bại
                            if (success) {
                                Toast.makeText(context, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show()

                                // Cập nhật Role ngay lập tức để không bị lỗi Guest
                                val currentUserId = SupabaseClient.client.auth.currentUserOrNull()?.id
                                if (currentUserId != null) {
                                    viewModel.fetchUserRole(currentUserId)
                                }

                                // Về trang chủ
                                navController.navigate(ScreenRoutes.HOME) {
                                    popUpTo(ScreenRoutes.LOGIN) { inclusive = true }
                                }
                            } else {
                                Toast.makeText(context, "Lỗi hệ thống, vui lòng thử lại sau", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            },
            enabled = !isLoading, // Khóa nút khi đang load
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Xác nhận đổi", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}