package com.example.appandroid.screen

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appandroid.model.Vocabulary
import com.example.appandroid.screen.component.FeedbackBottomSheet
import com.example.appandroid.screen.component.MochiProgressBar
import com.example.appandroid.utils.SoundManager
import com.example.appandroid.viewmodel.LearnViewModel
import com.example.appandroid.viewmodel.QuizType
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun QuizScreen(
    viewModel: LearnViewModel,
    vocab: Vocabulary,
    playAudio: () -> Unit, // Giữ tham số này để không lỗi code gọi, nhưng ta sẽ dùng logic xịn bên trong
    onClose: () -> Unit
) {
    val quizType by viewModel.currentQuizType.collectAsState()
    val options by viewModel.quizOptions.collectAsState()

    // Trạng thái đúng sai
    var isAnswerCorrect by remember { mutableStateOf<Boolean?>(null) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Cấu hình Âm thanh
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    val mediaPlayer = remember { MediaPlayer() }
    var tts: TextToSpeech? by remember { mutableStateOf(null) }

    // 1. Khởi tạo TTS & MediaPlayer
    DisposableEffect(Unit) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) tts?.language = Locale.US
        }
        tts = textToSpeech
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
            mediaPlayer.release()
        }
    }

    // 2. Hàm phát âm thanh thông minh (Copy từ ReviewFlashcardScreen)
    fun playSmartAudio(word: String, audioUrl: String?) {
        if (!audioUrl.isNullOrBlank()) {
            try {
                mediaPlayer.reset()
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                mediaPlayer.setDataSource(audioUrl)
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener { it.start() }
                mediaPlayer.setOnErrorListener { _, _, _ ->
                    tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
                    true
                }
            } catch (e: Exception) {
                tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        } else {
            tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                MochiProgressBar(
                    currentStep = viewModel.getCurrentProgressStep(),
                    totalSteps = viewModel.getTotalProgressSteps(),
                    modifier = Modifier.weight(1f)
                )
            }
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            when (quizType) {
                // --- DẠNG 1: TRẮC NGHIỆM ---
                QuizType.MULTIPLE_CHOICE_MEANING -> {
                    Text("Chọn nghĩa đúng của từ:", color = Color.Gray, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = vocab.word,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MochiTextDark
                    )
                    Spacer(modifier = Modifier.height(40.dp))

                    options.forEach { option ->
                        QuizOptionButton(
                            text = option,
                            onClick = {
                                val isCorrect = option == vocab.meaning
                                // PHÁT ÂM THANH NGAY KHI BẤM
                                if (isCorrect) soundManager.playSuccess() else soundManager.playWrong()
                                isAnswerCorrect = isCorrect
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // --- DẠNG 2: ĐIỀN TỪ ---
                QuizType.FILL_IN_BLANK -> {
                    Text("Điền từ còn thiếu vào câu:", color = Color.Gray, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(32.dp))
                    val maskedSentence = vocab.exampleSentence?.replace(vocab.word, "_______", ignoreCase = true) ?: "_______"
                    Text(
                        text = maskedSentence,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = MochiTextDark,
                        lineHeight = 30.sp
                    )
                    Spacer(modifier = Modifier.height(40.dp))

                    var input by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nhập từ còn thiếu") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val isCorrect = input.trim().equals(vocab.word, ignoreCase = true)
                            // PHÁT ÂM THANH NGAY KHI BẤM
                            if (isCorrect) soundManager.playSuccess() else soundManager.playWrong()
                            isAnswerCorrect = isCorrect
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MochiGreen),
                        enabled = input.isNotBlank(),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("Kiểm tra", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- BOTTOM SHEET KẾT QUẢ ---
        if (isAnswerCorrect != null) {
            ModalBottomSheet(
                onDismissRequest = { },
                sheetState = bottomSheetState,
                containerColor = Color.Transparent,
                dragHandle = null
            ) {
                FeedbackBottomSheet(
                    vocab = vocab,
                    isCorrect = isAnswerCorrect!!,
                    // Dùng hàm playSmartAudio nội bộ thay vì hàm được truyền vào
                    playAudio = { playSmartAudio(vocab.word, vocab.audioUrl) },
                    onContinue = {
                        val wasCorrect = isAnswerCorrect!!
                        isAnswerCorrect = null
                        if (wasCorrect) {
                            viewModel.moveToNextStep()
                        } else {
                            viewModel.markAsWrong(vocab)
                            viewModel.moveToNextStep()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun QuizOptionButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = MochiTextDark
        ),
        elevation = ButtonDefaults.buttonElevation(2.dp)
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}