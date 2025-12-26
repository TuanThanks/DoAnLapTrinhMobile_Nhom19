package com.example.appandroid.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appandroid.R
import kotlinx.coroutines.delay

@Preview(showBackground = true)
@Composable
fun OtpAuthenticationScreen(
    email: String = "",
    onCloseClick: () -> Unit = {},
    onVerifyClick: (String) -> Unit = {},
    onResendClick: () -> Unit = {},
    onUpdateEmailClick: () -> Unit = {},
    navController: NavController? = null
) {
    var otp by remember { mutableStateOf("") }
    var countdown by remember { mutableStateOf(60) }
    val isButtonEnabled = otp.length == 6 && isValidGmail(email)
    val context = LocalContext.current


    // Đếm ngược 60 giây
    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000L)
            countdown--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Nút X
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopStart
        ) {
            IconButton(
                onClick = {
                    onCloseClick
                    navController?.navigateUp()
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
            text = "Nhập mã xác thực",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Dòng email
        Text(
            text = "Flash English đã gửi mã xác thực đến email:\n$email",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Ô nhập OTP
        OutlinedTextField(
            value = otp,
            onValueChange = { if (it.length <= 6) otp = it },
            placeholder = { Text("Nhập mã xác thực", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                unfocusedContainerColor = Color(0xFFF5F5F5), // Correct parameter
                focusedContainerColor = Color(0xFFF5F5F5)
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Nút Xác thực email
        Button(
            onClick = { onVerifyClick(otp) },
            enabled = isButtonEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isButtonEnabled) colorResource(id = R.color.blue) else Color(0xFFE0E0E0),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(
                text = "Xác thực email",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Không nhận được mã?
        Text(
            text = "Bạn không nhận được mã xác thực?",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Gợi ý 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("1. ", color = Color.Gray, fontSize = 14.sp)
            TextButton(onClick = onUpdateEmailClick) {
                Text(
                    text = "Cập nhật lại email",
                    color = Color(0xFF2196F3),
                    fontSize = 14.sp
                )
            }
        }

        // Gợi ý 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("2. ", color = Color.Gray, fontSize = 14.sp)
            TextButton(
                onClick = onResendClick,
                enabled = countdown == 0
            ) {
                Text(
                    text = if (countdown == 0)
                        "Nhấn 1 mã mới"
                    else
                        "Nhận 1 mã mới (chờ sau ${countdown}s)",
                    color = if (countdown == 0) Color(0xFF2196F3) else Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

fun isValidGmail(email: String): Boolean {
    val gmailPattern = Regex("^[a-zA-Z0-9._%+-]+@gmail\\.com$")
    return gmailPattern.matches(email)
}