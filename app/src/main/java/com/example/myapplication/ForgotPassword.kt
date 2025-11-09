package com.example.myapplication

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ForgotPasswordScreen(
    onCloseClick: () -> Unit = {},
    onSendClick: (String) -> Unit = {},
    navController: NavController
) {
    var context = LocalContext.current
    var email by remember { mutableStateOf("") }
    val isForgotEnable =email.isNotEmpty() && isValidGmail(email = email)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Nút X (góc trên trái)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopStart
        ) {
            IconButton(
                onClick = {
                    navController.navigate(Screen.LoginScreen.route)
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
                unfocusedContainerColor = Color(0xFFF5F5F5), // Correct parameter
                focusedContainerColor = Color(0xFFF5F5F5)    // Also set for focused state
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Nút "Nhận mã ngay"
        Button(
            onClick = { navController.navigate(Screen.OtpAutheenticationScreen.route)
},
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                if(isForgotEnable) {
                    colorResource(R.color.blue)
                } else Color(0xFFE0E0E0)

            ),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(
                text = "Nhận mã ngay",
                color = Color.Gray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}