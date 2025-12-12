package com.example.appandroid.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VocabularyRequest(
    @SerialName("lesson_id") val lessonId: Long, // Mặc định gán vào Bài 1 (hoặc tạo bài riêng ID 999)
    val word: String,
    val meaning: String, // Lưu định nghĩa tiếng Anh (vì API trả về tiếng Anh)
    val phonetic: String?,
    @SerialName("example_sentence") val exampleSentence: String?,
    @SerialName("audio_url") val audioUrl: String?
)