package com.example.appandroid.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import com.example.appandroid.utils.LearningStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.random.Random

enum class QuizType {
    MULTIPLE_CHOICE_MEANING, // Chọn nghĩa tiếng Việt đúng
    FILL_IN_BLANK,           // Điền từ còn thiếu
    // Bạn có thể mở rộng thêm: LISTENING, TYPING...
}
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
    private val _currentStep = MutableStateFlow(LearningStep.FLASHCARD)
    val currentStep = _currentStep.asStateFlow()
    private val _reviewQueue = mutableListOf<Vocabulary>()

    // Loại câu hỏi hiện tại cho màn hình 3
    private val _currentQuizType = MutableStateFlow(QuizType.MULTIPLE_CHOICE_MEANING)
    val currentQuizType = _currentQuizType.asStateFlow()

    // Danh sách đáp án cho câu trắc nghiệm (1 đúng + 3 sai)
    private val _quizOptions = MutableStateFlow<List<String>>(emptyList())
    val quizOptions = _quizOptions.asStateFlow()
    private val _currentLessonIndex = MutableStateFlow(0)
    val currentLessonIndex: StateFlow<Int> = _currentLessonIndex.asStateFlow()
    private val _currentQuestion = MutableStateFlow<ReviewQuestion?>(null)
    val currentQuestion: StateFlow<ReviewQuestion?> = _currentQuestion
    private val _stats = MutableStateFlow(UserProgressStats())
    val stats: StateFlow<UserProgressStats> = _stats
    private val _reviewList = MutableStateFlow<List<Vocabulary>>(emptyList())
    val reviewList: StateFlow<List<Vocabulary>> = _reviewList
// ... Trong LearnViewModel
// Hàm tạo câu hỏi mới từ list review
// ... import giữ nguyên

    // 2. Hàm Tạo Câu Hỏi Đa Dạng (Random 11 loại)
    fun generateNextQuestion() {
        val list = reviewList.value
        if (list.isEmpty()) {
            _currentQuestion.value = null
            return
        }

        val targetVocab = list.first()
        val otherVocabs = list.filter { it.id != targetVocab.id }

        // Kiểm tra dữ liệu có đủ để tạo câu hỏi khó không
        val hasSentence = !targetVocab.exampleSentence.isNullOrBlank()
        val isPhrase = targetVocab.word.contains(" ") // Check cụm từ

        // Lọc ra các loại câu hỏi khả thi cho từ này
        val feasibleTypes = mutableListOf<QuestionType>()

        // Nhóm Trắc nghiệm (Cần ít nhất 3 từ khác để làm đáp án nhiễu)
        if (otherVocabs.size >= 3) {
            feasibleTypes.add(QuestionType.MC_MEANING_TO_WORD)
            feasibleTypes.add(QuestionType.MC_WORD_TO_MEANING)
            feasibleTypes.add(QuestionType.LISTENING_CHOICE)
            if (hasSentence) feasibleTypes.add(QuestionType.MC_SENTENCE_TO_WORD)
        }

        // Nhóm Gõ phím / Điền từ (Luôn khả thi)
        feasibleTypes.add(QuestionType.TYPING_MEANING)
        feasibleTypes.add(QuestionType.LISTENING_TYPING)

        if (hasSentence) {
            feasibleTypes.add(QuestionType.FILL_BLANK_HINT)
            feasibleTypes.add(QuestionType.FILL_BLANK_NO_HINT)
            feasibleTypes.add(QuestionType.TYPING_SENTENCE)
            feasibleTypes.add(QuestionType.LISTENING_SENTENCE)
        }
        if (isPhrase) feasibleTypes.add(QuestionType.TYPING_COLLOCATION)

        // Random chọn 1 loại
        val type = feasibleTypes.randomOrNull() ?: QuestionType.TYPING_MEANING

        // Tạo đáp án nhiễu (cho trắc nghiệm)
        val options = if (type.name.startsWith("MC_") || type == QuestionType.LISTENING_CHOICE) {
            val isShowWordAnswer = (type == QuestionType.MC_MEANING_TO_WORD || type == QuestionType.MC_SENTENCE_TO_WORD || type == QuestionType.LISTENING_CHOICE)

            val distractors = otherVocabs.shuffled().take(3).map {
                if (isShowWordAnswer) it.word else it.meaning
            }
            val correctParams = if (isShowWordAnswer) targetVocab.word else targetVocab.meaning
            (distractors + correctParams).shuffled()
        } else emptyList()

        // Đáp án đúng để so khớp
        val correctString = when(type) {
            QuestionType.MC_WORD_TO_MEANING -> targetVocab.meaning
            else -> targetVocab.word // Các dạng còn lại đều yêu cầu nhập/chọn từ tiếng Anh
        }

        _currentQuestion.value = ReviewQuestion(targetVocab, type, options, correctString)
    }

    // Hàm xử lý khi người dùng trả lời xong (giống hàm cũ của bạn nhưng gọi generateNextQuestion)
    @RequiresApi(Build.VERSION_CODES.O)
    fun processResult(vocabId: Long, isCorrect: Boolean) {
        val currentList = _reviewList.value.toMutableList()
        if (currentList.isEmpty()) return

        val currentVocab = currentList.first() // Từ đang học

        if (isCorrect) {
            // --- ĐÚNG ---
            // Tăng level (Max 5)
            val nextLevel = if (currentVocab.currentLevel < 5) currentVocab.currentLevel + 1 else 5

            // Lưu DB và xóa khỏi hàng đợi
            submitReviewResult(vocabId, nextLevel)
            currentList.removeAt(0)

        } else {
            // --- SAI / QUÊN ---

            // Logic: Giảm 1 Level (Min 1)
            val newLevel = if (currentVocab.currentLevel > 1) currentVocab.currentLevel - 1 else 1

            // Cập nhật ngay vào object hiện tại để lần sau gặp lại nó biết nó đang level thấp
            currentVocab.currentLevel = newLevel

            // Lưu xuống DB ngay lập tức (để SRS ghi nhận việc bị tụt hạng)
            submitReviewResult(vocabId, newLevel)

            // Logic Hàng Đợi: Đẩy lùi ra sau 5 vị trí
            currentList.removeAt(0)
            // Nếu list còn ít hơn 5 từ thì nhét xuống cuối, nếu nhiều hơn thì nhét vào vị trí số 5
            val insertIndex = minOf(5, currentList.size)
            currentList.add(insertIndex, currentVocab)

            Log.d("ReviewLogic", "Sai từ '${currentVocab.word}'. Giảm về Level $newLevel. Lặp lại sau $insertIndex từ.")
        }

        _reviewList.value = currentList
        generateNextQuestion()
    }
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
    fun getTotalProgressSteps(): Int {
        return _vocabList.value.size * 3
    }
    fun getCurrentProgressStep(): Int {
        val wordIndex = _currentLessonIndex.value

        // Tính điểm dựa trên bước nhỏ đang đứng
        val stepScore = when (_currentStep.value) {
            LearningStep.FLASHCARD -> 0 // Vừa vào Flashcard tính là bắt đầu từ đó
            LearningStep.DICTATION -> 1 // Xong Flashcard -> được 1 điểm
            LearningStep.QUIZ -> 2      // Xong Dictation -> được 2 điểm
            else -> 3                   // Xong hết -> 3 điểm
        }

        // Công thức: (Số từ đã qua * 3) + Điểm của từ hiện tại
        // Ví dụ: Đang học từ thứ 2 (index=1), ở màn hình Quiz (2 điểm)
        // -> (1 * 3) + 2 = 5. Tiến độ là 5/15.
        // Khi bấm "Đã biết" ở từ 1 -> index nhảy lên 1 -> (1*3) + 0 = 3. Tiến độ nhảy từ 0 lên 3.
        return (wordIndex * 3) + stepScore + 1
    }
    fun loadVocabularies(lessonId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _currentLessonIndex.value = 0          // Reset biến đếm về 0 (Bài đầu tiên)
            _currentStep.value = LearningStep.FLASHCARD // Reset về bước 1 (Màn hình Flashcard)
            _reviewQueue.clear()                   // Xóa hàng chờ ôn tập cũ (nếu có)
            // ----------------------
            try {
                _vocabList.value = repository.getVocabularies(lessonId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    // Danh sách từ cần ôn tập

    // Hàm xử lý Ôn tập (Review)
    @RequiresApi(Build.VERSION_CODES.O)
    fun submitReviewResult(vocabId: Long, newLevel: Int) {
        viewModelScope.launch {
            val userId = authRepo.getCurrentUserId() ?: return@launch
            val now = Instant.now()

            // Công thức Spaced Repetition của bạn
            val nextReview = when (newLevel) {
                1 -> now.plus(1, ChronoUnit.MINUTES) // Level 1 học lại ngay
                2 -> now.plus(3, ChronoUnit.DAYS)
                3 -> now.plus(7, ChronoUnit.DAYS)
                4 -> now.plus(14, ChronoUnit.DAYS)
                5 -> now.plus(30, ChronoUnit.DAYS)
                else -> now.plus(1, ChronoUnit.DAYS)
            }

            val request = UserProgressRequest(
                userId = userId, vocabId = vocabId,
                memoryLevel = newLevel, isLearned = true,
                nextReviewAt = nextReview.toString()
            )
            repository.saveWordProgress(request)
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
    // Hàm chuyển sang bước tiếp theo
    fun moveToNextStep() {
        when (_currentStep.value) {
            LearningStep.FLASHCARD -> {
                _currentStep.value = LearningStep.DICTATION
            }
            LearningStep.DICTATION -> {
                // Trước khi sang Quiz, hãy chuẩn bị dữ liệu cho Quiz
                prepareQuiz()
                _currentStep.value = LearningStep.QUIZ
            }
            LearningStep.QUIZ -> {
                // Xong từ hiện tại -> Chuyển từ tiếp theo
                nextLesson()
            }
            else -> {}
        }
    }
    private fun prepareQuiz() {
        val currentVocab = _vocabList.value.getOrNull(_currentLessonIndex.value) ?: return

        // Random loại câu hỏi (50% trắc nghiệm, 50% điền từ)
        // Hoặc bạn có thể fix cứng logic: Sai Dictation -> Fill Blank, Đúng -> Multiple Choice
        val isRandomChoice = Random.nextBoolean()

        if (isRandomChoice) {
            _currentQuizType.value = QuizType.MULTIPLE_CHOICE_MEANING
            generateMultipleChoiceOptions(currentVocab)
        } else {
            _currentQuizType.value = QuizType.FILL_IN_BLANK
        }
    }
    private fun generateMultipleChoiceOptions(correctVocab: Vocabulary) {
        val allMeanings = _vocabList.value.map { it.meaning }.toMutableList()
        allMeanings.remove(correctVocab.meaning) // Bỏ đáp án đúng ra khỏi list sai

        // Lấy 3 đáp án sai ngẫu nhiên
        val wrongOptions = allMeanings.shuffled().take(3)

        // Trộn đáp án đúng vào và shuffle lần nữa
        val options = (wrongOptions + correctVocab.meaning).shuffled()
        _quizOptions.value = options
    }

    // Hàm chuyển từ mới (Logic cũ của bạn nhưng cập nhật thêm reset step)
    fun nextLesson() {
        val currentList = _vocabList.value
        val currentIndex = _currentLessonIndex.value

        if (currentIndex < currentList.size - 1) {
            // Vẫn còn từ mới -> Tăng Index -> Reset về Flashcard
            _currentLessonIndex.value += 1
            _currentStep.value = LearningStep.FLASHCARD
        } else {
            // Hết từ mới -> Kiểm tra hàng chờ Review (Từ làm sai)
            if (_reviewQueue.isNotEmpty()) {
                // Lấy từ đầu tiên trong hàng chờ ra học lại
                val nextReviewVocab = _reviewQueue.removeAt(0)

                // Mẹo: Thêm tạm vào list hiển thị để học tiếp
                val newList = currentList.toMutableList()
                newList.add(nextReviewVocab)
                _vocabList.value = newList

                _currentLessonIndex.value += 1
                _currentStep.value = LearningStep.FLASHCARD
            } else {
                // --- HOÀN THÀNH BÀI HỌC ---
                // Chỉ khi hết sạch sành sanh (cả từ mới lẫn từ ôn lại) mới vào đây

                // 1. Trigger màn hình Finish
                _currentLessonIndex.value += 1

                // 2. Đổi màu xanh cho Lesson (Chỉ làm ở đây)
                // Lưu ý: Bạn cần lấy lessonId từ đâu đó, hoặc truyền vào hàm này
                // Ở đây mình lấy ID của từ cuối cùng để truy ngược ra Lesson (hoặc bạn lưu currentLessonId trong ViewModel)
                val lessonId = currentList.firstOrNull()?.lessonId ?: 0L
                if (lessonId != 0L) {
                    markLessonAsCompleted(lessonId)
                }
            }
        }
    }
    // Hàm đổi màu xanh Lesson (Chỉ cập nhật UI List bên ngoài)
// Sửa lại hàm này
    fun markLessonAsCompleted(lessonId: Long) {
        viewModelScope.launch {
            // 1. Cập nhật UI ngay lập tức cho mượt (Optimistic Update)
            val updatedLessons = _lessons.value.map { lesson ->
                if (lesson.id == lessonId) lesson.copy(isLearned = true) else lesson
            }
            _lessons.value = updatedLessons

            // 2. GỌI API LƯU LÊN SUPABASE
            val userId = authRepo.getCurrentUserId()
            if (userId != null) {
                try {
                    // Gọi repository lưu vào bảng user_lessons
                    repository.completeLesson(userId, lessonId)
                    Log.d("LearnViewModel", "Saved lesson completion to DB")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Hàm "Tôi đã biết từ này" -> Bỏ qua Dictation/Quiz -> Sang từ mới luôn
    fun onKnowWord() {
        // Logic đánh dấu đã thuộc (nếu cần)
        // ...
        nextLesson()
    }

    // Hàm đánh dấu làm sai (để ôn lại sau)
    fun markAsWrong(vocab: Vocabulary) {
        // Nếu từ này chưa có trong hàng đợi thì thêm vào
        if (!_reviewQueue.any { it.id == vocab.id }) {
            _reviewQueue.add(vocab)
            // Có thể thêm logic: Toast báo "Từ này sẽ được ôn lại vào cuối giờ"
        }
    }
}