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
    private val supabase = SupabaseClient.client
    val currentUser
        get() = supabase.auth.currentUserOrNull()

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
    suspend fun sendPasswordResetEmail(email: String, redirectUrl: String) {
        supabase.auth.resetPasswordForEmail(
            email = email,
            redirectUrl = redirectUrl // <--- Quan trọng: Gửi kèm link để Supabase biết đường quay về App
        )
    }

    fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
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