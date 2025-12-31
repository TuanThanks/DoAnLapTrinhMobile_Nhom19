package com.example.appandroid.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable // <--- Đừng quên cái này
data class Lesson(
    val id: Long,
    val title: String,
    val description: String?,
    @SerialName("course_id")
    val courseId: Long,
    @SerialName("image_url") // Khớp với cột image_url trong Supabase
    val imageUrl: String?,

    @SerialName("total_words") // Khớp với cột total_words
    val totalWords: Int = 0,

    // Những trường này chưa có trong DB, tạm thời để default hoặc @Transient
    // Nếu bạn muốn lấy từ DB thì phải tạo cột tương ứng.
    // Tạm thời ta giả lập logic khóa bài ở ViewModel sau.
    val isLocked: Boolean = false,
    val isCompleted: Boolean = false,
    val isLearned: Boolean = false
)