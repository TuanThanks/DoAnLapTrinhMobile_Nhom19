package com.example.appandroid.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.ui.unit.plus
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appandroid.data.AuthRepository
import com.example.appandroid.data.LearnRepository
import com.example.appandroid.model.Course
import com.example.appandroid.model.DictionaryEntry
import com.example.appandroid.model.Lesson
import com.example.appandroid.model.UserProgressRequest
import com.example.appandroid.model.UserProgressStats
import com.example.appandroid.model.Vocabulary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import java.time.Instant
import java.time.temporal.ChronoUnit

class LearnViewModel : ViewModel() {
    private val authRepo = AuthRepository()
    private val repository = LearnRepository()

    // Danh sách Khóa học (Cho màn hình 2)
    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses

    // Danh sách Bài học (Cho màn hình 3)
    private val _lessons = MutableStateFlow<List<Lesson>>(emptyList())
    val lessons: StateFlow<List<Lesson>> = _lessons

    // Danh sách Từ vựng đang học (Cho màn hình 4)
    private val _vocabList = MutableStateFlow<List<Vocabulary>>(emptyList())
    val vocabList: StateFlow<List<Vocabulary>> = _vocabList

    // Trạng thái loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // --- CÁC HÀM GỌI DỮ LIỆU ---

// ... Trong LearnViewModel

    fun loadCourses() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // 1. Lấy danh sách gốc từ Supabase
                val fetchedCourses = repository.getCourses()

                // 2. LỌC DỮ LIỆU (Thêm đoạn này)
                // Loại bỏ những khóa học có tên chứa chữ "Sổ tay" hoặc ID đặc biệt nào đó
                _courses.value = fetchedCourses.filter { course ->
                    // Chỉ giữ lại những khóa học KHÔNG phải là Sổ tay cá nhân
                    !course.title.contains("Sổ tay", ignoreCase = true)
                    // Hoặc lọc theo ID nếu bạn nhớ ID của nó (ví dụ ID 9999)
                    // && course.id != 9999L
                }

                _isLoading.value = false
            } catch (e: Exception) {
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }

    // ...
    private val _currentCourseTitle = MutableStateFlow("Khóa học")
    val currentCourseTitle = _currentCourseTitle.asStateFlow()

    // 2. Cập nhật hàm loadLessons để lấy luôn tên khóa học
    fun loadLessons(courseId: Long) {
        viewModelScope.launch {
            _isLoading.value = true

            // A. Lấy tên khóa học (Chạy song song hoặc tuần tự đều được)
            val course = repository.getCourseDetails(courseId)
            if (course != null) {
                _currentCourseTitle.value = course.title.uppercase() // Viết hoa cho đẹp
            }

            // B. Lấy danh sách bài học (Logic cũ của bạn)
            val userId = authRepo.getCurrentUserId()
            val rawLessons = repository.getLessons(courseId)
            val learnedLessonIds = if (userId != null) repository.getLearnedLessonIds(userId) else emptyList()

            val mappedLessons = rawLessons.map { lesson ->
                lesson.copy(isLearned = learnedLessonIds.contains(lesson.id))
            }
            _lessons.value = mappedLessons

            _isLoading.value = false
        }
    }

    fun loadVocabularies(lessonId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _vocabList.value = repository.getVocabularies(lessonId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun markAsLearned(vocabId: Long, isKnown: Boolean) {
        viewModelScope.launch {
            val userId = authRepo.getCurrentUserId() ?: return@launch

            // Logic Mochi:
            // - Nếu bấm "Tiếp tục" (Mới học) -> Level 1 -> Ôn lại sau 1 ngày (hoặc 1 phút để test)
            // - Nếu bấm "Đã biết" -> Level 5 (hoặc 4) -> Ôn lại sau 1 tháng

            val level = if (isKnown) 5 else 1
            // Tính thời gian ôn tập tiếp theo (Next Review)
            val now = Instant.now() // Lấy giờ hiện tại (UTC)

            val nextReview = if (isKnown) {
                // Đã biết -> Cộng 30 ngày
                now.plus(30, ChronoUnit.DAYS)
            } else {
                // Mới học -> Cộng 1 ngày (hoặc 1 phút để test)
                now.plus(1, ChronoUnit.DAYS)
            }

            // Chuyển sang String chuẩn ISO-8601 để gửi lên Supabase
            val request = UserProgressRequest(
                userId = userId,
                vocabId = vocabId,
                memoryLevel = level,
                isLearned = true,
                nextReviewAt = nextReview.toString() // VD: "2023-10-25T10:00:00Z"
            )

            try {
                repository.saveWordProgress(request)
                repository.updateStreak(userId)
                // Log để kiểm tra
                println("Đã lưu tiến độ cho từ $vocabId: Level $level")
                println("Đã học xong từ $vocabId, đang update streak...")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            repository.updateStreak(userId)
        }
    }
    // Danh sách từ cần ôn tập
    private val _reviewList = MutableStateFlow<List<Vocabulary>>(emptyList())
    val reviewList: StateFlow<List<Vocabulary>> = _reviewList

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadReviewWords() {
        viewModelScope.launch {
            val userId = authRepo.getCurrentUserId() ?: return@launch
            _isLoading.value = true
            try {
                // Gọi Repository lấy danh sách
                _reviewList.value = repository.getReviewList(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    // Hàm xử lý Ôn tập (Review)
    @RequiresApi(Build.VERSION_CODES.O)
    fun submitReviewResult(vocabId: Long, currentLevel: Int, isRemembered: Boolean) {
        viewModelScope.launch {
            val userId = authRepo.getCurrentUserId() ?: return@launch

            // LOGIC QUAN TRỌNG NHẤT CỦA APP MOCHI
            val newLevel = if (isRemembered) {
                // Nếu nhớ: Tăng 1 cấp (Max là 5)
                if (currentLevel < 5) currentLevel + 1 else 5
            } else {
                // Nếu quên: Về lại mức 1
                1
            }

            // Tính thời gian ôn tiếp theo dựa trên Level mới
            val now = Instant.now()
            val nextReview = when (newLevel) {
                1 -> now.plus(1, ChronoUnit.DAYS)   // Level 1: 1 ngày
                2 -> now.plus(3, ChronoUnit.DAYS)   // Level 2: 3 ngày
                3 -> now.plus(7, ChronoUnit.DAYS)   // Level 3: 7 ngày
                4 -> now.plus(14, ChronoUnit.DAYS)  // Level 4: 2 tuần
                5 -> now.plus(30, ChronoUnit.DAYS)  // Level 5: 1 tháng
                else -> now.plus(1, ChronoUnit.DAYS)
            }

            // Gửi lên Server
            val request = UserProgressRequest(
                userId = userId,
                vocabId = vocabId,
                memoryLevel = newLevel,
                isLearned = true,
                nextReviewAt = nextReview.toString()
            )

            try {
                repository.saveWordProgress(request)
                repository.updateStreak(userId)
                // Sau khi lưu xong, xóa từ này khỏi danh sách ôn tập hiện tại trên UI
                _reviewList.value = _reviewList.value.filter { it.id != vocabId }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private val _searchResult = MutableStateFlow<List<DictionaryEntry>>(emptyList())
    val searchResult: StateFlow<List<DictionaryEntry>> = _searchResult

    // Hàm tìm kiếm
    fun searchDictionary(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                _searchResult.value = repository.searchWord(query.trim())
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Hàm xóa kết quả khi thoát màn hình
    fun clearSearchResult() {
        _searchResult.value = emptyList()
    }
    // ...

    // Hàm lưu từ điển
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveDictionaryWord(entry: DictionaryEntry, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val userId = authRepo.getCurrentUserId()
            if (userId == null) {
                onError()
                return@launch
            }

            val success = repository.addDictionaryWordToNotebook(userId, entry)
            if (success) {
                onSuccess()
            } else {
                onError()
            }
        }
    }
    // Biến lưu thống kê (Để vẽ biểu đồ)
    private val _stats = MutableStateFlow(UserProgressStats())
    val stats: StateFlow<UserProgressStats> = _stats

    // Hàm tính toán thống kê
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadUserStats() {
        viewModelScope.launch {
            val userId = authRepo.getCurrentUserId() ?: return@launch

            // 1. Lấy danh sách từ cần ôn (Giữ nguyên logic cũ)
            val reviews = repository.getReviewList(userId)

            // 2. Lấy TOÀN BỘ tiến độ để tính biểu đồ
            val allProgress = repository.getAllUserProgress(userId)

            // 3. Tính toán các con số
            val total = allProgress.size
            val lv1 = allProgress.count { it.memoryLevel == 1 }
            val lv2 = allProgress.count { it.memoryLevel == 2 }
            val lv3 = allProgress.count { it.memoryLevel == 3 }
            val lv4 = allProgress.count { it.memoryLevel == 4 }
            val lv5 = allProgress.count { it.memoryLevel == 5 }

            // 4. Cập nhật vào State
            _stats.value = UserProgressStats(
                totalWords = total,
                reviewCount = reviews.size,
                level1Count = lv1,
                level2Count = lv2,
                level3Count = lv3,
                level4Count = lv4,
                level5Count = lv5
            )

            // Cập nhật luôn danh sách review cho chắc
            _reviewList.value = reviews
        }
    }
    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak

    // Hàm load streak hiển thị lên Profile
    fun loadStreak() {
        viewModelScope.launch {
            val userId = authRepo.getCurrentUserId() ?: return@launch
            val profile = repository.getUserProfile(userId)
            _streak.value = profile?.streakCount ?: 0
        }
    }

}