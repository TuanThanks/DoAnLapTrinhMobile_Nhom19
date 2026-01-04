package com.example.appandroid.screen


import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.navigation.NavController
import com.example.appandroid.screen.component.FeedbackBottomSheet
import com.example.appandroid.screen.component.MultipleChoiceQuestionView
import com.example.appandroid.screen.component.TypingQuestionView
import com.example.appandroid.utils.SoundManager
import com.example.appandroid.viewmodel.LearnViewModel
import com.example.appandroid.viewmodel.QuestionType
import java.util.Locale
import android.media.AudioAttributes
import android.media.MediaPlayer


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReviewFlashcardScreen(
    navController: NavController,
    viewModel: LearnViewModel
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }

    val reviewList by viewModel.reviewList.collectAsState()
    val currentQuestion by viewModel.currentQuestion.collectAsState()

    // State UI
    var showFeedback by remember { mutableStateOf(false) }
    var isUserCorrect by remember { mutableStateOf(false) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }

    // State Typing (Lưu chữ người dùng gõ)
    var userInput by remember { mutableStateOf("") }

    // --- CẤU HÌNH ÂM THANH ---
    // 1. TextToSpeech (Dự phòng)
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    DisposableEffect(Unit) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        tts = textToSpeech
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    // 2. MediaPlayer (Phát link DB)
    val mediaPlayer = remember { MediaPlayer() }

    // --- HÀM PHÁT ÂM THANH (ĐỊNH NGHĨA Ở ĐÂY ĐỂ DÙNG ĐƯỢC BÊN DƯỚI) ---
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
                mediaPlayer.setOnPreparedListener {
                    it.start()
                    Log.d("Audio", "Playing from URL: $audioUrl")
                }
                mediaPlayer.setOnErrorListener { _, _, _ ->
                    // Lỗi link -> Dùng TTS
                    tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
                    true
                }
            } catch (e: Exception) {
                tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        } else {
            // Không có link -> Dùng TTS
            tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    // Giải phóng MediaPlayer khi thoát
    DisposableEffect(Unit) {
        onDispose { mediaPlayer.release() }
    }

    // --- LOGIC ---
    LaunchedEffect(Unit) {
        // Reset nhắc nhở
        val storage = com.example.appandroid.utils.LocalStorage(context)
        if (storage.isReminderEnabled()) {
            com.example.appandroid.utils.ReminderScheduler.scheduleNextReminder(context)
        }
        // Tạo câu hỏi đầu tiên
        viewModel.generateNextQuestion()
    }

    // Reset ô nhập liệu mỗi khi đổi câu hỏi
    LaunchedEffect(currentQuestion) {
        userInput = ""
        showFeedback = false
        selectedAnswer = null
    }

    // Xử lý trả lời
    fun handleAnswer(answer: String) {
        if (showFeedback) return

        selectedAnswer = answer
        val correct = currentQuestion?.correctAnswer ?: ""

        isUserCorrect = answer.trim().equals(correct.trim(), ignoreCase = true)
        showFeedback = true

        if (isUserCorrect) {
            soundManager.playSuccess()
            playSmartAudio(currentQuestion?.vocab?.word ?: "", currentQuestion?.vocab?.audioUrl)
        } else {
            soundManager.playWrong()
        }
    }

    // Next câu hỏi
    fun onNextQuestion() {
        val q = currentQuestion ?: return
        viewModel.processResult(q.vocab.id, isUserCorrect)
    }

    // --- GIAO DIỆN ---
    Scaffold(containerColor = Color.White) { padding ->
        if (reviewList.isEmpty() && currentQuestion == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉", fontSize = 60.sp)
                    Text("Bạn đã hoàn thành bài ôn!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { navController.popBackStack() }) { Text("Quay về") }
                }
            }
        } else if (currentQuestion != null) {
            val q = currentQuestion!!

            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = "Còn: ${reviewList.size}", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Hiển thị câu hỏi
                    when (q.type) {
                        QuestionType.MC_MEANING_TO_WORD -> {
                            TitleText("Chọn từ tiếng Anh đúng:")
                            BigText(q.vocab.meaning, MochiBlue)
                        }
                        QuestionType.MC_WORD_TO_MEANING -> {
                            TitleText("Chọn nghĩa tiếng Việt đúng:")
                            BigText(q.vocab.word, MochiGreen)
                        }
                        QuestionType.MC_SENTENCE_TO_WORD -> {
                            TitleText("Chọn từ điền vào chỗ trống:")
                            SentenceText(q.vocab.exampleSentence, q.vocab.word)
                        }
                        QuestionType.LISTENING_CHOICE,
                        QuestionType.LISTENING_TYPING,
                        QuestionType.LISTENING_SENTENCE -> {
                            TitleText("Nghe và trả lời:")
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                IconButton(
                                    onClick = { playSmartAudio(q.vocab.word, q.vocab.audioUrl) },
                                    modifier = Modifier.size(80.dp).background(MochiBlue.copy(0.1f), CircleShape)
                                ) {
                                    Icon(Icons.Default.VolumeUp, null, tint = MochiBlue, modifier = Modifier.size(40.dp))
                                }
                            }
                            LaunchedEffect(q) { playSmartAudio(q.vocab.word, q.vocab.audioUrl) }
                        }
                        else -> {
                            // Typing & Fill blank
                            val title = if(q.type == QuestionType.TYPING_MEANING) "Gõ từ tiếng Anh:" else "Điền từ còn thiếu:"
                            TitleText(title)
                            if (q.type == QuestionType.TYPING_MEANING || q.type == QuestionType.TYPING_COLLOCATION) {
                                BigText(q.vocab.meaning)
                            } else {
                                SentenceText(q.vocab.exampleSentence, q.vocab.word)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Khu vực trả lời
                    if (q.options.isNotEmpty()) {
                        MultipleChoiceQuestionView(
                            options = q.options,
                            correctAnswer = q.correctAnswer,
                            selectedAnswer = selectedAnswer,
                            isAnswered = showFeedback,
                            onAnswerSelected = { handleAnswer(it) }
                        )
                    } else {
                        // SỬA: Dùng TypingQuestionView mới (có truyền userInput)
                        val hint = if (q.type == QuestionType.FILL_BLANK_HINT) generateHint(q.vocab.word) else null

                        TypingQuestionView(
                            currentInput = userInput,          // Truyền biến state vào
                            onInputChange = { userInput = it },// Nhận sự kiện gõ phím
                            onAnswerSubmit = { handleAnswer(userInput) },
                            hintText = hint,
                            isAnswered = showFeedback
                        )
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }

                // Bottom Sheet
                if (showFeedback) {
                    Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                        FeedbackBottomSheet(
                            vocab = q.vocab,
                            isCorrect = isUserCorrect,
                            onContinue = { onNextQuestion() },
                            playAudio = { playSmartAudio(q.vocab.word, q.vocab.audioUrl) }
                        )
                    }
                }
            }
        }
    }
}

// Helper Composable
@Composable fun TitleText(text: String) = Text(text, color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
@Composable fun BigText(text: String, color: Color = Color.Black) = Text(text, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = color, modifier = Modifier.fillMaxWidth())
@Composable fun SentenceText(sentence: String?, word: String) {
    val display = sentence?.replace(word, "_______", ignoreCase = true) ?: "_______"
    Text(display, fontSize = 20.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, color = Color.DarkGray, lineHeight = 28.sp)
}

fun generateHint(word: String): String {
    if (word.length <= 2) return "_ ".repeat(word.length)
    val chars = word.toCharArray()
    for (i in chars.indices) {
        if (i % 2 != 0) chars[i] = '_'
    }
    return String(chars).replace("", " ").trim()
}

