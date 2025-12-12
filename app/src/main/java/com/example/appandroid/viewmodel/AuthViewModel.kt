package com.example.appandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.appandroid.data.AuthRepository
import com.example.appandroid.navigation.ScreenRoutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Trạng thái của việc xác thực
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository() // Khởi tạo Repository

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState
    // Biến trạng thái kiểm tra đăng nhập (null = đang kiểm tra, true = đã login, false = chưa)
    private val _isUserLoggedIn = MutableStateFlow<Boolean?>(null)
    val isUserLoggedIn: StateFlow<Boolean?> = _isUserLoggedIn
    // Hàm đăng nhập
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.login(email, pass)

            result.onSuccess {
                _authState.value = AuthState.Success
            }.onFailure { error ->
                // --- SỬA ĐOẠN NÀY ---
                // Lấy thông báo lỗi gốc
                val rawMessage = error.message ?: ""

                // "Dịch" lỗi sang tiếng Việt thân thiện
                val userFriendlyMessage = when {
                    rawMessage.contains("Invalid login credentials") -> "Sai tài khoản hoặc mật khẩu!"
                    rawMessage.contains("Unable to resolve host") -> "Lỗi kết nối mạng, vui lòng kiểm tra Wifi/4G."
                    rawMessage.contains("Email not confirmed") -> "Email chưa được xác thực. Vui lòng kiểm tra hộp thư."
                    else -> "Đăng nhập thất bại: $rawMessage" // Hiện lỗi gốc nếu là lỗi lạ
                }

                _authState.value = AuthState.Error(userFriendlyMessage)
            }
        }
    }

    // Bạn cũng nên áp dụng tương tự cho hàm register
    fun register(name: String,email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.register(name, email, pass)

            result.onSuccess {
                _authState.value = AuthState.Success
            }.onFailure { error ->
                val rawMessage = error.message ?: ""

                // Xử lý lỗi đăng ký
                val userFriendlyMessage = when {
                    rawMessage.contains("User already registered") -> "Email này đã được đăng ký rồi."
                    rawMessage.contains("Unable to resolve host") -> "Lỗi kết nối mạng."
                    rawMessage.contains("Password should be") -> "Mật khẩu quá yếu (cần ít nhất 6 ký tự)."
                    else -> "Lỗi đăng ký: $rawMessage"
                }

                _authState.value = AuthState.Error(userFriendlyMessage)
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Gọi repository (Hàm này nên là suspend và throw exception nếu lỗi)
                repository.sendPasswordResetEmail(email)

                // Nếu chạy đến đây nghĩa là thành công
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                // Nếu có lỗi -> Nhảy vào đây
                _authState.value = AuthState.Error(e.message ?: "Lỗi gửi mail")
            }
        }
    }
    fun updatePassword(password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.updateUserPassword(password)
            onResult(success)
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    // Hàm reset trạng thái (để tránh lỗi hiển thị Toast nhiều lần)
    fun resetState() {
        _authState.value = AuthState.Idle
    }
    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.loginWithGoogle(idToken)
            result.onSuccess {
                _authState.value = AuthState.Success
            }.onFailure {
                _authState.value = AuthState.Error("Lỗi Google: ${it.message}")
            }
        }
    }
    fun checkLoginStatus() {
        viewModelScope.launch {
            val isLoggedIn = repository.retrieveUserSession()
            _isUserLoggedIn.value = isLoggedIn
        }
    }
    fun getCurrentUserEmail(): String? {
        return repository.currentUser?.email
    }

    fun getCurrentUserName(): String? {
        // Lấy tên từ User Metadata (nếu có lưu lúc đăng ký), hoặc lấy phần đầu email
        val metadata = repository.currentUser?.userMetadata
        return metadata?.get("full_name")?.toString()?.replace("\"", "")
            ?: getCurrentUserEmail()?.substringBefore("@")
    }

    // Xử lý Đăng xuất
    fun logout(navController: NavController) {
        viewModelScope.launch {
            repository.logout()
            // Reset trạng thái
            _isUserLoggedIn.value = false
            _authState.value = AuthState.Idle

            // Chuyển về màn hình Login và XÓA SẠCH lịch sử màn hình cũ
            navController.navigate(ScreenRoutes.LOGIN) {
                popUpTo(0) { inclusive = true } // Xóa hết stack
                launchSingleTop = true
            }
        }
    }
}