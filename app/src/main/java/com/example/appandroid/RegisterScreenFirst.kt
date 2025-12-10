package com.example.appandroid.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appandroid.R
import com.example.appandroid.navigation.ScreenRoutes

@Composable
fun RegisterScreenFirst(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopStart) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Bạn muốn tạo tài khoản\nbằng cách nào nhỉ?", fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(40.dp))
        // Nút Google giả lập
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB4437)),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text("Tiếp tục với Gmail", color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("HOẶC", color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        // Nút Email -> Chuyển sang màn hình 2
        OutlinedButton(
            onClick = { navController.navigate(ScreenRoutes.REGISTER_SECOND) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(25.dp),
            border = BorderStroke(1.dp, Color.LightGray)
        ) {
            Text("Tạo 1 tài khoản với email", color = Color.Black)
        }
    }
}