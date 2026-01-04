package com.example.appandroid.data

import android.R.attr.password
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.appandroid.model.Course
import com.example.appandroid.model.DictionaryEntry
import com.example.appandroid.model.Lesson
import com.example.appandroid.model.UserProfile
import com.example.appandroid.model.UserProgressRequest
import com.example.appandroid.model.Vocabulary
import com.example.appandroid.model.VocabularyRequest
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order // <--- 1. THÊM IMPORT NÀY
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant

class LearnRepository {
    private val supabase = SupabaseClient.client
    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true // Chấp nhận JSON lỏng lẻo hơn tí cho đỡ lỗi
            })
        }

        // Cấu hình timeout (để nếu lâu quá thì báo lỗi chứ đừng treo app)
        engine {
            connectTimeout = 10_000 // 10 giây
            socketTimeout = 10_000
        }
    }
    // 1. Lấy danh sách Khóa học
    suspend fun getCourses(): List<Course> {
        return withContext(Dispatchers.IO) {
            supabase.postgrest["courses"]
                .select {
                    order("id", Order.ASCENDING) // Sắp xếp khóa học theo ID
                }
                .decodeList<Course>()
        }
    }

    // 2. Lấy danh sách Bài học (ĐOẠN BỊ LỖI CỦA BẠN)
    suspend fun getLessons(courseId: Long): List<Lesson> {
        return withContext(Dispatchers.IO) {
            supabase.postgrest["lessons"]
                .select {
                    filter {
                        eq("course_id", courseId)
                    }
                    order("id", Order.ASCENDING)
                }
                .decodeList<Lesson>()
        }
    }

    // 3. Lấy Từ vựng
    suspend fun getVocabularies(lessonId: Long): List<Vocabulary> {
        return withContext(Dispatchers.IO) {
            supabase.postgrest["vocabularies"]
                .select {
                    filter {
                        eq("lesson_id", lessonId)
                    }
                    order("id", Order.ASCENDING) // Nên sắp xếp cả từ vựng cho chuẩn
                }
                .decodeList<Vocabulary>()
        }
    }
    // Cách này chậm hơn xíu nhưng an toàn tuyệt đối, không lo lỗi Duplicate
// Hàm lưu tiến độ (Phiên bản Upsert chuẩn)
    suspend fun saveWordProgress(request: UserProgressRequest) {
        withContext(Dispatchers.IO) {
            // Upsert: Tự động Insert hoặc Update
            // Yêu cầu: Bảng user_progress phải có khóa Unique(user_id, vocab_id)
            supabase.postgrest["user_progress"].upsert(
                value = request,
                onConflict = "user_id, vocab_id"
            )
        }
    }
    // Lấy danh sách từ cần ôn tập (Review)
// ... imports giữ nguyên

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getReviewList(userId: String): List<Vocabulary> {
        return withContext(Dispatchers.IO) {
            val now = Instant.now().toString()

            // BƯỚC 1: Lấy TOÀN BỘ danh sách tiến độ đến hạn (ĐÃ BỎ LIMIT)
            val progressList = supabase.postgrest["user_progress"]
                .select {
                    filter {
                        eq("user_id", userId)
                        lte("next_review_at", now) // Nhỏ hơn hoặc bằng hiện tại
                    }
                    order("next_review_at", Order.ASCENDING)
                }.decodeList<UserProgressRequest>()

            if (progressList.isEmpty()) return@withContext emptyList()

            // BƯỚC 2: Lấy chi tiết từ vựng (Dùng kỹ thuật Chunking an toàn)
            val vocabIds = progressList.map { it.vocabId }
            val allVocabularies = mutableListOf<Vocabulary>()

            // Chia danh sách ID thành các nhóm nhỏ, mỗi nhóm 50 ID
            // Ví dụ: 258 từ sẽ chia thành 6 lần gọi (50, 50, 50, 50, 50, 8)
            // Cách này đảm bảo không bao giờ bị lỗi quá tải đường truyền
            vocabIds.chunked(50).forEach { batchIds ->
                val batchResult = supabase.postgrest["vocabularies"]
                    .select {
                        filter {
                            isIn("id", batchIds)
                        }
                    }.decodeList<Vocabulary>()

                allVocabularies.addAll(batchResult)
            }

            // BƯỚC 3: Map Level từ progress sang vocabulary
            allVocabularies.forEach { vocab ->
                val matchingProgress = progressList.find { it.vocabId == vocab.id }
                if (matchingProgress != null) {
                    vocab.currentLevel = matchingProgress.memoryLevel
                }
            }

            // Trả về toàn bộ danh sách (258 từ...)
            allVocabularies
        }
    }

    // Hàm tra từ điển
    suspend fun searchWord(word: String): List<DictionaryEntry> {
        return withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis() // Bấm giờ
            Log.d("DictionarySpeed", "Bắt đầu gọi API lúc: $startTime")

            try {
                val url = "https://api.dictionaryapi.dev/api/v2/entries/en/$word"
                val result = httpClient.get(url).body<List<DictionaryEntry>>()

                val endTime = System.currentTimeMillis() // Kết thúc
                Log.d("DictionarySpeed", "Đã tải xong! Mất tổng cộng: ${endTime - startTime} ms")

                result
            } catch (e: Exception) {
                val endTime = System.currentTimeMillis()
                Log.e("DictionarySpeed", "Lỗi rồi! Mất: ${endTime - startTime} ms. Lỗi: ${e.message}")
                emptyList()
            }
        }
    }
    // ĐỊNH NGHĨA ID CỦA "KHO CHỨA" (Thay số này bằng ID thật bạn vừa tạo ở bước 1)
    private val SAVED_WORDS_LESSON_ID = 9999L

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addDictionaryWordToNotebook(userId: String, entry: DictionaryEntry): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Kiểm tra xem từ này đã có chưa (Ở bất kỳ bài nào)
                val existingVocab = supabase.postgrest["vocabularies"]
                    .select {
                        filter { eq("word", entry.word) }
                        limit(1)
                    }.decodeList<Vocabulary>().firstOrNull()

                var vocabId = existingVocab?.id

                // 2. Nếu chưa có -> Tạo mới và nhét vào "KHO CHỨA" (9999)
                if (vocabId == null) {
                    val def = entry.meanings?.firstOrNull()?.definitions?.firstOrNull()
                    val meaning = def?.definition ?: "Định nghĩa tiếng Anh"
                    val example = def?.example
                    val audio = entry.phonetics?.firstOrNull { !it.audio.isNullOrBlank() }?.audio

                    val newVocab = VocabularyRequest(
                        word = entry.word,
                        meaning = meaning,
                        phonetic = entry.phonetic,
                        exampleSentence = example,
                        audioUrl = audio,

                        // --- SỬA ĐOẠN NÀY ---
                        // Gán cứng vào bài học "Từ vựng đã lưu"
                        lessonId = SAVED_WORDS_LESSON_ID
                        // --------------------
                    )

                    val inserted = supabase.postgrest["vocabularies"]
                        .insert(newVocab) { select() }
                        .decodeList<Vocabulary>()
                        .first()

                    vocabId = inserted.id
                }

                // 3. Thêm vào Sổ tay ôn tập (Level 1)
                val progress = UserProgressRequest(
                    userId = userId,
                    vocabId = vocabId,
                    memoryLevel = 1, // Mặc định Level 1 (Mới toanh)
                    isLearned = true,
                    // Ôn tập ngay lập tức (hoặc +1 ngày tùy bạn)
                    nextReviewAt = java.time.Instant.now().toString()
                )

                saveWordProgress(progress)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    suspend fun getAllUserProgress(userId: String): List<UserProgressRequest> {
        return withContext(Dispatchers.IO) {
            try {
                // Lấy toàn bộ danh sách đã học của user này
                supabase.postgrest["user_progress"]
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }.decodeList<UserProgressRequest>()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
    // ...
    // Hàm lấy danh sách ID của các bài học mà User đã bắt đầu học
// 2. Cập nhật lại hàm getLearnedLessonIds để lấy từ bảng mới này
    suspend fun getLearnedLessonIds(userId: String): List<Long> {
        return try {
            val result = SupabaseClient.client
                .from("user_lessons")
                // Chỉ lấy cột lesson_id
                .select(columns = Columns.list("lesson_id")) {
                    filter { eq("user_id", userId) }
                }
                // SỬA Ở ĐÂY: Dùng class nhỏ LessonIdResult thay vì UserLessonProgress
                .decodeList<LessonIdResult>()

            // Map ra list Long
            result.map { it.lesson_id }

        } catch (e: Exception) {
            // QUAN TRỌNG: In lỗi ra để biết nếu có gì sai
            e.printStackTrace()
            emptyList()
        }
    }

// ... các hàm cũ ...

    // Hàm lấy chi tiết 1 khóa học theo ID
    suspend fun getCourseDetails(courseId: Long): Course? {
        return withContext(Dispatchers.IO) {
            try {
                supabase.postgrest["courses"]
                    .select {
                        filter { eq("id", courseId) }
                    }.decodeSingleOrNull<Course>() // Chỉ lấy 1 dòng
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    suspend fun getUserProfile(userId: String): UserProfile? {
        return withContext(Dispatchers.IO) {
            supabase.postgrest["profiles"]
                .select { filter { eq("id", userId) } }
                .decodeList<UserProfile>()
                .firstOrNull()
        }
    }

    // Hàm cập nhật Streak (Logic thông minh)
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateStreak(userId: String) {
        withContext(Dispatchers.IO) {
            val today = LocalDate.now().toString()
            val profile = getUserProfile(userId)

            if (profile == null) {
                // Tạo mới nếu chưa có
                val newProfile = UserProfile(id = userId, streakCount = 1, lastStudyDate = today)
                supabase.postgrest["profiles"].insert(newProfile)
            } else {
                val lastDate = profile.lastStudyDate
                // Nếu đã học hôm nay rồi thì thôi
                if (lastDate == today) return@withContext

                // Kiểm tra xem hôm qua có học không
                val yesterday = LocalDate.now().minusDays(1).toString()

                val newStreak = if (lastDate == yesterday) {
                    profile.streakCount + 1 // Học liên tiếp -> Tăng streak
                } else {
                    1 // Mất chuỗi -> Reset về 1
                }

                // Cập nhật lên Server
                supabase.postgrest["profiles"].update(
                    {
                        set("streak_count", newStreak)
                        set("last_study_date", today)
                    }
                ) {
                    filter { eq("id", userId) }
                }
            }
        }
    }
    // 1. Hàm lưu bài học đã xong
// Sửa hàm completeLesson
    suspend fun completeLesson(userId: String, lessonId: Long) {
        val progress = UserLessonProgress(
            user_id = userId,
            lesson_id = lessonId
        )

        try {
            SupabaseClient.client
                .from("user_lessons")
                .insert(progress)
        } catch (e: Exception) {
            // Duplicate key → bỏ qua
        }
    }



}

@Serializable
data class UserLessonProgress(
    val user_id: String,
    val lesson_id: Long,
    val is_completed: Boolean = true
)
@kotlinx.serialization.Serializable
data class LessonIdResult(
    val lesson_id: Long
)