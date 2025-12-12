package com.example.appandroid.data

import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


class AuthRepository {
    val currentUser
        get() = supabase.auth.currentUserOrNull()

    // Gọi client Supabase đã tạo ở bước 2
    private val supabase = SupabaseClient.client

    // Kiểm tra xem có user đang đăng nhập không
    // Lưu ý: Supabase lưu session ở cache, hàm này load user từ session đó
    suspend fun getCurrentUser(): Boolean {
        // Load session từ bộ nhớ máy (nếu có)
        supabase.auth.loadFromStorage()
        return supabase.auth.currentUserOrNull() != null
    }

    // Đăng nhập
    suspend fun login(email: String, pass: String): Result<Unit> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = pass
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e) // Trả về lỗi để ViewModel hiển thị
        }
    }

    // Đăng ký
    suspend fun register(name: String, email: String, pass: String): Result<Unit> {
        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = pass

                // Lưu tên người dùng vào metadata của user
                this.data = buildJsonObject {
                    put("full_name", name)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun loginWithGoogle(idToken: String): Result<Unit> {
        return try {
            // Thay vì dùng 'Google', ta dùng 'IDToken' để báo Supabase biết ta đang gửi mã token
            supabase.auth.signInWith(IDToken) {
                this.idToken = idToken
                this.provider = Google // Báo rằng token này là của Google
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Quên mật khẩu
// Sửa lại hàm gửi mail quên mật khẩu
    suspend fun sendPasswordResetEmail(email: String) {
        withContext(Dispatchers.IO) {
            supabase.auth.resetPasswordForEmail(
                email = email,
                redirectUrl = "com.example.appandroid://reset-callback" // Thêm dòng này
            )
        }
    }

    fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }
    suspend fun retrieveUserSession(): Boolean {
        return try {
            // Quan trọng: Lệnh này khôi phục phiên đăng nhập từ bộ nhớ máy
            supabase.auth.loadFromStorage()

            // Kiểm tra xem có session hợp lệ không
            supabase.auth.currentSessionOrNull() != null
        } catch (e: Exception) {
            false
        }
    }
    suspend fun logout() {
        try {
            // Xóa session trên Supabase và xóa cache trong máy
            supabase.auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    suspend fun updateUserPassword(newPassword: String): Boolean {
        return try {
            // ❌ XÓA DÒNG NÀY: supabase.auth.refreshCurrentSession()
            // Vì ta đang dùng session phục hồi (vừa lấy từ link), đừng refresh nó.

            // Chỉ gọi lệnh đổi pass
            supabase.auth.modifyUser {
                password = newPassword
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            println("Lỗi đổi mật khẩu: ${e.message}")
            false
        }
    }
}