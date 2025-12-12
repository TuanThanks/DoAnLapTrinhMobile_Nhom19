package com.example.appandroid.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProgressRequest(
    @SerialName("user_id")
    val userId: String,

    @SerialName("vocab_id")
    val vocabId: Long,

    @SerialName("memory_level")
    val memoryLevel: Int,

    // --- QUAN TRỌNG NHẤT: Phải có dòng @SerialName này ---
    @SerialName("is_learned")
    val isLearned: Boolean = true, // Gán mặc định là true
    // -----------------------------------------------------

    @SerialName("next_review_at")
    val nextReviewAt: String
)