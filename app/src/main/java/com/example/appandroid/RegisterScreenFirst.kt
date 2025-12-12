package com.example.appandroid.screen

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appandroid.R
import com.example.appandroid.navigation.ScreenRoutes
import com.example.appandroid.viewmodel.AuthState
import com.example.appandroid.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun RegisterScreenFirst(
    navController: NavController,
    viewModel: AuthViewModel // 1. Thêm tham số ViewModel vào đây
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()

    // --- CẤU HÌNH GOOGLE SIGN IN (Giống hệt LoginScreen) ---
    val webClientId = "1044564910746-1898f7mk3sa8lboj7fcg28scte0qv3uu.apps.googleusercontent.com"

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    // --- XỬ LÝ KẾT QUẢ GOOGLE TRẢ VỀ ---
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    // Gửi token sang ViewModel để Supabase xử lý
                    viewModel.loginWithGoogle(idToken)
                } else {
                    Toast.makeText(context, "Không lấy được ID Token", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(context, "Lỗi Google: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- LẮNG NGHE KẾT QUẢ TỪ SUPABASE ---
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                // Chuyển thẳng vào trang chủ
                navController.navigate(ScreenRoutes.HOME) {
                    popUpTo(ScreenRoutes.LOGIN) { inclusive = true }
                }
            }
            is AuthState.Error -> {
                val msg = (authState as AuthState.Error).message
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

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

        // --- NÚT GOOGLE ĐÃ SỬA ---
        Button(
            onClick = {
                // Gọi hàm mở cửa sổ Google
                launcher.launch(googleSignInClient.signInIntent)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB4437)),
            shape = RoundedCornerShape(25.dp)
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Tiếp tục với Gmail", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("HOẶC", color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        // Nút Email -> Chuyển sang màn hình 2 (Giữ nguyên)
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