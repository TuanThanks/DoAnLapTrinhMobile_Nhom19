package com.example.appandroid.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.appandroid.R

class SoundManager(context: Context) {
    private val soundPool: SoundPool

    // ID của các âm thanh sau khi load
    private val soundFlip: Int
    private val soundCorrect: Int
    private val soundWrong: Int
    private val soundSuccess: Int

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5) // Chơi tối đa 5 âm thanh cùng lúc
            .setAudioAttributes(audioAttributes)
            .build()

        // Load âm thanh từ thư mục raw
        // Lưu ý: Đảm bảo bạn đã có file trong res/raw
        soundFlip = soundPool.load(context, R.raw.flip, 1)
        soundCorrect = soundPool.load(context, R.raw.correct, 1)
        soundWrong = soundPool.load(context, R.raw.wrong, 1)
        soundSuccess = soundPool.load(context, R.raw.success, 1)
    }

    fun playFlip() {
        soundPool.play(soundFlip, 1f, 1f, 0, 0, 1f)
    }

    fun playCorrect() {
        soundPool.play(soundCorrect, 1f, 1f, 0, 0, 1f)
    }

    fun playWrong() {
        soundPool.play(soundWrong, 1f, 1f, 0, 0, 1f)
    }

    fun playSuccess() {
        soundPool.play(soundSuccess, 1f, 1f, 0, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}