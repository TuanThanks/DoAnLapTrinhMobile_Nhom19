package com.example.appandroid.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.appandroid.data.AuthRepository
import com.example.appandroid.data.SupabaseClient
import com.example.appandroid.navigation.ScreenRoutes
import com.example.appandroid.utils.UserRole
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Trạng thái của việc xác thực
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
@kotlinx.serialization.Serializable
data class UserProfileRole(
    val role: String = "user" // Mặc định là user
)
class AuthViewModel : ViewModel() {

    // Biến lưu role hiện tại (Mặc định là Guest)
    private val _userRole = MutableStateFlow(UserRole.GUEST)
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()
    private val repository = AuthRepository() // Khởi tạo Repository

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState
    // Biến trạng thái kiểm tra đăng nhập (null = đang kiểm tra, true = đã login, false = chưa)
    private val _isUserLoggedIn = MutableStateFlow<Boolean?>(null)
    val isUserLoggedIn: StateFlow<Boolean?> = _isUserLoggedIn
    init {
        // Kiểm tra xem máy có lưu phiên đăng nhập cũ không?
        val currentSession = SupabaseClient.client.auth.currentSessionOrNull()
        if (currentSession != null) {
            // Nếu có -> Lấy Role ngay lập tức
            fetchUserRole(currentSession.user?.id ?: "")
        }
    }
    // Hàm set Guest Mode
    fun setGuestMode() {
        _userRole.value = UserRole.GUEST
    }

    // Hàm cập nhật Role sau khi Login thành công (Sẽ dùng ở Phần 2)
    fun fetchUserRole(userId: String) {
        viewModelScope.launch {
            try {
                // 1. Gọi Supabase lấy cột 'role' trong bảng 'profiles'
                val result = SupabaseClient.client
                    .from("profiles")
                    .select(columns = Columns.list("role")) {
                        filter {
                            eq("id", userId)
                        }
                    }
                    .decodeSingle<UserProfileRole>()

                // 2. Chuyển đổi String sang Enum
                _userRole.value = when (result.role) {
                    "admin" -> UserRole.ADMIN
                    "premium" -> UserRole.PREMIUM
                    else -> UserRole.USER
                }
                Log.d("Auth", "User role loaded: ${_userRole.value}")

            } catch (e: Exception) {
                Log.e("Auth", "Error fetching role", e)
                // Nếu lỗi mạng hoặc chưa có profile -> Vẫn cho là User thường
                _userRole.value = UserRole.USER
            }
        }
    }
    // Hàm đăng nhập
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.login(email, pass)

            result.onSuccess {
                // --- SỬA Ở ĐÂY ---

                // 1. Lấy ID của user vừa đăng nhập thành công
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id

                // 2. Nếu lấy được ID thì đi hỏi Role ngay
                if (userId != null) {
                    fetchUserRole(userId)
                }

                _authState.value = AuthState.Success
            }.onFailure { error ->
                // (Giữ nguyên phần xử lý lỗi của bạn)
                val rawMessage = error.message ?: ""
                val userFriendlyMessage = when {
                    rawMessage.contains("Invalid login credentials") -> "Sai tài khoản hoặc mật khẩu!"
                    rawMessage.contains("Unable to resolve host") -> "Lỗi kết nối mạng..."
                    rawMessage.contains("Email not confirmed") -> "Email chưa xác thực..."
                    else -> "Đăng nhập thất bại: $rawMessage"
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
                // SỬA: Truyền thêm redirectUrl khớp với Manifest
                val redirectUrl = "com.example.appandroid://reset-callback"

                repository.sendPasswordResetEmail(email, redirectUrl) // <--- Cần sửa Repository để nhận tham số này

                _authState.value = AuthState.Success
            } catch (e: Exception) {
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