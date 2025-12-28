package com.example.appandroid.utils

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.appandroid.worker.ReminderWorker
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    // Hàm này gọi khi học xong (hoặc khi mới mở màn hình review)
    fun scheduleNextReminder(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // --- 1. QUAN TRỌNG: Hủy cái tên cũ (Cái Periodic lúc đầu bạn làm) ---
        // Dòng này giúp xóa sạch các "bóng ma" cũ gây lỗi báo sai giờ
        workManager.cancelUniqueWork("my_daily_study_work")
        // -------------------------------------------------------------------

        // 2. Tạo yêu cầu: Chờ 24h rồi mới chạy Worker
        val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            // ĐANG TEST: Để 60 giây cho thoải mái (24s nhanh quá dễ bị báo đè)
            // KHI CHẠY THẬT: Đổi 60 -> 24 và SECONDS -> HOURS
            .setInitialDelay(24, TimeUnit.SECONDS)
            .addTag("dynamic_study_reminder")
            .build()

        // 3. REPLACE: Hủy cái lịch hiện tại, đặt cái mới (Reset đồng hồ)
        workManager.enqueueUniqueWork(
            "study_reminder_work", // Tên định danh mới
            ExistingWorkPolicy.REPLACE,
            reminderRequest
        )
    }

    // Hàm gọi khi tắt Switch
    fun cancelReminder(context: Context) {
        val workManager = WorkManager.getInstance(context)
        // Hủy cả mới lẫn cũ cho chắc ăn
        workManager.cancelUniqueWork("study_reminder_work")
        workManager.cancelUniqueWork("my_daily_study_work")
    }
}