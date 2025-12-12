package com.example.appandroid.utils

import android.content.Context

class LocalStorage(context: Context) {
    private val prefs = context.getSharedPreferences("mochi_prefs", Context.MODE_PRIVATE)

    // Lưu ID khóa học vừa chọn
    fun saveLastCourseId(courseId: Long) {
        prefs.edit().putLong("LAST_COURSE_ID", courseId).apply()
    }

    // Lấy ID khóa học cũ (Trả về -1 nếu chưa có)
    fun getLastCourseId(): Long {
        return prefs.getLong("LAST_COURSE_ID", -1L)
    }
}