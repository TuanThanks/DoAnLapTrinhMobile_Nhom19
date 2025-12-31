package com.example.appandroid.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,

    @SerialName("streak_count")
    val streakCount: Int = 0,

    @SerialName("last_study_date")
    val lastStudyDate: String? = null
)