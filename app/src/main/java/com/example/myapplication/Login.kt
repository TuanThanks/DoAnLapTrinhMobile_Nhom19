package com.example.myapplication

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Preview(showBackground = true)
@Composable
fun LoginScreen(
    onGoogleSignInClick: () -> Unit = {},
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onForgotPasswordClick: () -> Unit = {},
    onContactClick: () -> Unit = {},
    onCreateAccountClick: () -> Unit = {},
    navController: NavController
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Kiểm tra điều kiện bật nút
    val isLoginEnabled = email.isNotBlank() && password.isNotBlank() && isValidGmail(email = email)

    // Hiển thị Toast khi cần
    LaunchedEffect(showToast) {
        if (showToast) {
            Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
            showToast = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Close button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(onClick = { /* Handle close */ }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = "Đăng nhập để học ngay nhé",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Google Sign In Button
        Button(
            onClick = onGoogleSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.red)
            ),
            shape = RoundedCornerShape(25.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "G+",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Tiếp tục với Gmail",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // OR divider
        Text(
            text = "HOẶC",
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Email Input
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Nhập email tài khoản học", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = Color(0xFFDB4437)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Nhập mật khẩu tài khoản học", color = Color.Gray) },
            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible)
                            Icons.Default.Visibility
                        else
                            Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Color(0xFF4CAF50)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = Color(0xFFDB4437)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // NÚT ĐĂNG NHẬP – ĐỔI MÀU + TOAST
        Button(
            onClick = {
                if (isLoginEnabled) {
                    showToast = true
                    onLoginClick(email, password)
                }
            },
            enabled = isLoginEnabled, // Bật/tắt nút
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLoginEnabled)
                    colorResource(R.color.blue) // Đỏ khi đủ điều kiện
                else
                    Color(0xFFE0E0E0), // Xám khi chưa đủ
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(
                text = "Đăng nhập",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quên mật khẩu
        TextButton(onClick = {
            navController.navigate(Screen.ForgotPasswordScreen.route)
        }) {
            Text("Quên mật khẩu", color = Color(0xFF2196F3), fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Liên hệ Support
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("Không đăng nhập được? ", color = Color.Gray, fontSize = 14.sp)
            TextButton(onClick = onContactClick) {
                Text("Liên hệ Support", color = Color(0xFF2196F3), fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tạo tài khoản
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("Chưa có tài khoản ? ", color = Color.Gray, fontSize = 14.sp)
            TextButton(onClick = {
                navController.navigate(Screen.RegisterFirstScreen.route)
            }) {
                Text("Tạo tài khoản", color = Color(0xFF2196F3), fontSize = 14.sp)
            }
        }
    }
}