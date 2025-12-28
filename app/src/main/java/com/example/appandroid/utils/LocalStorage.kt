package com.example.appandroid.utils

import android.content.Context
import android.content.SharedPreferences

class LocalStorage(context: Context) {

    // Biến này chỉ dùng được khi bạn khởi tạo class: val storage = LocalStorage(context)
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "mochi_settings"
        private const val KEY_LAST_COURSE_ID = "LAST_COURSE_ID"
        private const val KEY_REMINDER = "is_reminder_enabled"
        private const val KEY_IS_FIRST_LAUNCH = "is_first_launch"
        private const val KEY_AVATAR_INDEX = "key_avatar_index"

        // Helper để lấy SharedPreferences nhanh cho các hàm tĩnh
        private fun getPrefs(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }

        // --- CÁC HÀM TĨNH (STATIC) - Gọi bằng: LocalStorage.tenHam(context) ---

        fun isFirstLaunch(context: Context): Boolean {
            return getPrefs(context).getBoolean(KEY_IS_FIRST_LAUNCH, true)
        }

        fun setFirstLaunchComplete(context: Context) {
            getPrefs(context).edit().putBoolean(KEY_IS_FIRST_LAUNCH, false).apply()
        }

        fun saveLastCourseId(context: Context, courseId: Long) {
            getPrefs(context).edit().putLong(KEY_LAST_COURSE_ID, courseId).apply()
        }

        fun getLastCourseId(context: Context): Long {
            return getPrefs(context).getLong(KEY_LAST_COURSE_ID, -1L)
        }

        fun isReminderEnabled(context: Context): Boolean {
            return getPrefs(context).getBoolean(KEY_REMINDER, false)
        }

        fun setReminderEnabled(context: Context, isEnabled: Boolean) {
            getPrefs(context).edit().putBoolean(KEY_REMINDER, isEnabled).apply()
        }

        // Avatar tĩnh
        fun getSelectedAvatarIndex(context: Context): Int {
            return getPrefs(context).getInt(KEY_AVATAR_INDEX, 0)
        }

        fun saveSelectedAvatarIndex(context: Context, index: Int) {
            getPrefs(context).edit().putInt(KEY_AVATAR_INDEX, index).apply()
        }
    }

    // --- CÁC HÀM INSTANCE - Gọi bằng: val storage = LocalStorage(context); storage.tenHam() ---

    // 1. INTRO (Sửa lỗi của bạn ở đây)
    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(KEY_IS_FIRST_LAUNCH, true)
    }

    fun setFirstLaunchComplete() {
        prefs.edit().putBoolean(KEY_IS_FIRST_LAUNCH, false).apply()
    }

    // 2. COURSE
    fun saveLastCourseId(courseId: Long) {
        prefs.edit().putLong(KEY_LAST_COURSE_ID, courseId).apply()
    }

    fun getLastCourseId(): Long {
        return prefs.getLong(KEY_LAST_COURSE_ID, -1L)
    }

    // 3. REMINDER
    fun isReminderEnabled(): Boolean {
        return prefs.getBoolean(KEY_REMINDER, false)
    }

    fun setReminderEnabled(isEnabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMINDER, isEnabled).apply()
    }

    // 4. AVATAR
    fun getSelectedAvatarIndex(): Int {
        return prefs.getInt(KEY_AVATAR_INDEX, 0)
    }

    fun saveSelectedAvatarIndex(index: Int) {
        prefs.edit().putInt(KEY_AVATAR_INDEX, index).apply()
    }
}