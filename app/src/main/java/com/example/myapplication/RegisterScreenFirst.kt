package com.example.myapplication

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Preview(showBackground = true)
@Composable
fun RegisterScreenFirst(
    onGoogleRegisterClick: () -> Unit = {},
    onEmailRegisterClick: () -> Unit = {},
    onCloseClick: () -> Unit = {},
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Nút đóng (X)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopStart
        ) {
            IconButton(onClick = {
                navController.navigate(Screen.LoginScreen.route)
            }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Tiêu đề
        Text(
            text = "Bạn muốn tạo tài khoản\nbằng cách nào nhỉ?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Hình minh họa
        Image(
            painter = painterResource(id = R.drawable.icon_register), // Tên file: icon_register
            contentDescription = "Register illustration",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Nút Google (G+)
        Button(
            onClick = onGoogleRegisterClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFDB4437) // Màu đỏ Google
            ),
            shape = RoundedCornerShape(25.dp),
            contentPadding = PaddingValues(start = 28.dp, end = 48.dp) // Dịch trái
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "G+",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tiếp tục với Gmail",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Chữ "HOẶC"
        Text(
            text = "HOẶC",
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Nút tạo bằng email
        OutlinedButton(
            onClick = { navController.navigate(Screen.RegisterSecondScreen.route) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            border = BorderStroke(1.dp, Color.LightGray),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.Black,
                containerColor = Color.White
            )
        ) {
            Text(
                text = "Tạo 1 tài khoản với email",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}