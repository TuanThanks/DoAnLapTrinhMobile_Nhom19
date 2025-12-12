package com.example.appandroid.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Vocabulary(
    val id: Long,

    @SerialName("lesson_id") // Thêm dòng này để khớp với DB
    val lessonId: Long,

    val word: String,
    val meaning: String,
    val phonetic: String?,

    @SerialName("example_sentence")
    val exampleSentence: String?,

    // Nếu bạn có cột example_highlight trong DB, hãy thêm dòng này
    @SerialName("example_highlight")
    val exampleHighlight: String? = null,

    @SerialName("image_url")
    val imageUrl: String?,

    @SerialName("audio_url")
    val audioUrl: String?
)