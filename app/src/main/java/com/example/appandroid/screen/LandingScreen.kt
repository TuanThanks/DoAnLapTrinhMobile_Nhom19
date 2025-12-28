package com.example.appandroid.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appandroid.R
import com.example.appandroid.navigation.ScreenRoutes
import com.example.appandroid.viewmodel.AuthViewModel

// Màu xanh lá chủ đạo (Bạn có thể thay bằng MochiGreen của bạn)

@Composable
fun LandingScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Logo chữ (Nếu có)
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground), // Thay bằng logo chữ
            contentDescription = "Logo Text",
            modifier = Modifier.height(40.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 2. Ảnh Mascot Cute (ic_cute)
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground), // Thay bằng R.drawable.ic_cute của bạn
            contentDescription = "Mascot",
            modifier = Modifier.size(280.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Slogan
        Text(
            text = "Ghi nhớ 1000 từ vựng\nmột cách dễ dàng",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        // 4. Nút BẮT ĐẦU NGAY (Chế độ Guest)
        Button(
            onClick = {
                // Set role là GUEST trong ViewModel (Ta sẽ làm hàm này sau)
                authViewModel.setGuestMode()
                // Vào thẳng Home
                navController.navigate(ScreenRoutes.HOME)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MochiGreen)
        ) {
            Text("Bắt đầu ngay", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Nút ĐĂNG NHẬP
        OutlinedButton(
            onClick = { navController.navigate(ScreenRoutes.LOGIN) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MochiGreen),
            border = androidx.compose.foundation.BorderStroke(2.dp, MochiGreen)
        ) {
            Text("Đăng nhập", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}