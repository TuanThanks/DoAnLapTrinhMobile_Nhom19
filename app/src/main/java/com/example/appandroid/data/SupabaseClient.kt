package com.example.appandroid.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.FlowType // <--- 1. NHỚ THÊM IMPORT NÀY
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

object SupabaseClient {
    // URL và KEY của bạn (Giữ nguyên)
    private const val SUPABASE_URL = "https://fauhxkxzycyudbcpjoro.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZhdWh4a3h6eWN5dWRiY3Bqb3JvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQ4OTk1NjksImV4cCI6MjA4MDQ3NTU2OX0.2T4IamksG_Q-NL955Oe5mDnSWmBWoXDp8AjhDjY9uKY"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        // --- 2. CẤU HÌNH AUTH CHI TIẾT (SỬA ĐOẠN NÀY) ---
        install(Auth) {
            // Sử dụng chuẩn PKCE (Chuẩn mới nhất, bảo mật hơn và ít lỗi session hơn)
            flowType = FlowType.PKCE

            // Hai dòng này BẮT BUỘC phải khớp với AndroidManifest.xml
            scheme = "com.example.appandroid"
            host = "reset-callback"
        }
        // ------------------------------------------------

        install(Postgrest) {
            serializer = KotlinXSerializer(
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                }
            )
        }
    }
}