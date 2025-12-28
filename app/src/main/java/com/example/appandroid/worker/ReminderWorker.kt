package com.example.appandroid.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.appandroid.MainActivity
import com.example.appandroid.R

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val context = applicationContext

        // 1. Chỉ hiện thông báo nếu User vẫn đang BẬT tính năng này
        if (com.example.appandroid.utils.LocalStorage.isReminderEnabled(context)) {
            showNotification()

            // 2. QUAN TRỌNG: Tự động lên lịch cho ngày mai (Vòng lặp)
            // Nếu user không vào học để reset lịch, thì 24h sau App sẽ nhắc tiếp
            com.example.appandroid.utils.ReminderScheduler.scheduleNextReminder(context)
        }

        return Result.success()
    }

    private fun showNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "daily_study_reminder"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Nhắc nhở học tập",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // --- SỬA NỘI DUNG Ở ĐÂY ---
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Bạn đã vắng mặt 24h rồi! \uD83D\uDE22") // <--- Sửa tiêu đề
            .setContentText("Duy trì chuỗi (Streak) rất quan trọng. Vào học ngay nhé!") // <--- Sửa nội dung
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}