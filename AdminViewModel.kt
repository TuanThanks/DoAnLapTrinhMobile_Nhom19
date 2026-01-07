package com.example.appandroid.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appandroid.data.SupabaseClient
import com.example.appandroid.utils.UserRole
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// Model để hứng dữ liệu User từ bảng profiles
@Serializable
data class UserProfile(
    val id: String,
    val email: String? = "No Email",
    val role: String = "user",
    val created_at: String? = null
)

class AdminViewModel : ViewModel() {

    // Danh sách gốc (lấy từ DB)
    private var allUsers = listOf<UserProfile>()

    // Danh sách hiển thị (đã qua lọc/tìm kiếm)
    private val _users = MutableStateFlow<List<UserProfile>>(emptyList())
    val users = _users.asStateFlow()

    // Thống kê
    private val _totalUsers = MutableStateFlow(0)
    val totalUsers = _totalUsers.asStateFlow()

    private val _premiumCount = MutableStateFlow(0)
    val premiumCount = _premiumCount.asStateFlow()

    // Trạng thái loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // 1. LẤY DANH SÁCH USER
    fun fetchUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Gọi Supabase lấy toàn bộ user, sắp xếp theo ngày tạo mới nhất
                val result = SupabaseClient.client
                    .from("profiles")
                    .select()
                    .decodeList<UserProfile>()
                    .sortedByDescending { it.created_at }

                allUsers = result
                _users.value = result

                // Tính toán thống kê
                calculateStats()

            } catch (e: Exception) {
                Log.e("Admin", "Lỗi lấy danh sách user: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 2. TÌM KIẾM EMAIL
    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _users.value = allUsers
        } else {
            _users.value = allUsers.filter {
                it.email?.contains(query, ignoreCase = true) == true
            }
        }
    }

    // 3. NÂNG CẤP / HẠ CẤP USER
    fun updateUserRole(userId: String, newRole: String) {
        viewModelScope.launch {
            try {
                // Update lên Supabase
                SupabaseClient.client
                    .from("profiles")
                    .update(mapOf("role" to newRole)) {
                        filter { eq("id", userId) }
                    }

                // Cập nhật lại danh sách local (để UI tự đổi mà ko cần load lại)
                allUsers = allUsers.map {
                    if (it.id == userId) it.copy(role = newRole) else it
                }
                // Áp dụng lại bộ lọc tìm kiếm hiện tại (nếu có)
                searchUsers("") // Hoặc giữ query cũ nếu muốn phức tạp hơn
                calculateStats()

            } catch (e: Exception) {
                Log.e("Admin", "Lỗi update role: ${e.message}")
            }
        }
    }

    private fun calculateStats() {
        _totalUsers.value = allUsers.size
        _premiumCount.value = allUsers.count { it.role == "premium" }
    }
}