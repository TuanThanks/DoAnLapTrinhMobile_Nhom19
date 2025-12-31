package com.example.appandroid.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Course(
    val id: Long,
    val title: String,
    val description: String?,

    // Bắt buộc phải có dòng này để "dịch" tên từ Supabase
    @SerialName("image_url")
    val imageUrl: String?,

    @SerialName("bg_color")
    val bgColor: String?,
    @SerialName("description_vocab") // Tên cột trên Supabase
    val descriptionVocab: String? = "Từ vựng nền tảng" //
)