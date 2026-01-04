package com.example.appandroid.viewmodel

import com.example.appandroid.model.Vocabulary

// Các loại câu hỏi bạn yêu cầu
enum class QuestionType {
    // 1. Multiple Choice
    MC_MEANING_TO_WORD,     // 1.1: Chọn nghĩa -> từ
    MC_WORD_TO_MEANING,     // 1.2: Chọn từ -> nghĩa
    MC_SENTENCE_TO_WORD,    // 1.3: Chọn từ điền vào câu

    // 2. Fill in the Blank
    FILL_BLANK_HINT,        // 2.1: Điền từ có gợi ý số ký tự
    FILL_BLANK_NO_HINT,     // 2.2: Điền từ không gợi ý (Khó)

    // 3. Typing
    TYPING_MEANING,         // 3.1: Hiện nghĩa -> Gõ từ
    TYPING_SENTENCE,        // 3.2: Hiện câu -> Gõ từ
    TYPING_COLLOCATION,     // 3.3: Gõ cụm từ

    // 4. Listening
    LISTENING_CHOICE,       // 4.1: Nghe -> Chọn đáp án
    LISTENING_TYPING,       // 4.2: Nghe -> Gõ từ (Chính tả)
    LISTENING_SENTENCE      // 4.3: Nghe câu -> Điền từ
}

// Class chứa dữ liệu cho 1 câu hỏi
data class ReviewQuestion(
    val vocab: Vocabulary,           // Từ chính cần ôn
    val type: QuestionType,          // Loại câu hỏi
    val options: List<String>,       // 4 đáp án (cho trắc nghiệm), rỗng nếu là typing
    val correctAnswer: String        // Đáp án đúng (là word hoặc meaning tùy loại)
)