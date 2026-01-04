package com.example.appandroid.screen

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appandroid.R
import com.example.appandroid.model.Vocabulary
import com.example.appandroid.screen.component.FeedbackBottomSheet
import com.example.appandroid.screen.component.MochiProgressBar
import com.example.appandroid.utils.SoundManager
import com.example.appandroid.viewmodel.LearnViewModel
import java.util.Locale

// Màu sắc
val MochiYellowDark = Color(0xFFFFC107)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DictationScreen(
    vocab: Vocabulary,
    onCorrect: () -> Unit,
    onWrong: () -> Unit,
    playAudio: () -> Unit, // Giữ lại tham số để code cũ không lỗi
    onClose: () -> Unit,
    viewModel: LearnViewModel
) {
    var userInput by remember { mutableStateOf("") }
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

    // 2. Hàm phát âm thanh thông minh (Nội bộ)
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

    // Tự động phát khi vào màn hình
    LaunchedEffect(Unit) {
        // Delay xíu cho mượt
        kotlinx.coroutines.delay(300)
        playSmartAudio(vocab.word, vocab.audioUrl)
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Gray
                    )
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
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Nghe và viết lại",
                    fontSize = 20.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(30.dp))

                // Hình ảnh minh họa (Mèo hoặc Logo)
                Image(
                    painter = painterResource(id = R.drawable.ic_app), // Thay bằng ảnh con mèo nếu có
                    contentDescription = null,
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Nút Loa lớn
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { playSmartAudio(vocab.word, vocab.audioUrl) },
                        modifier = Modifier
                            .size(80.dp)
                            .shadow(6.dp, CircleShape)
                            .background(Color.White, CircleShape)
                            .border(1.dp, MochiYellowDark, CircleShape),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Listen",
                            tint = MochiYellowDark,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Ô nhập liệu
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    placeholder = {
                        Text("Gõ lại từ bạn nghe được...", color = Color.LightGray)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9F9F9), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF9F9F9),
                        unfocusedContainerColor = Color(0xFFF9F9F9),
                        focusedIndicatorColor = MochiGreen,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MochiTextDark
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                // Nút Kiểm tra
                val isButtonEnabled = userInput.isNotBlank()

                Button(
                    onClick = {
                        val correct = userInput.trim().equals(vocab.word.trim(), ignoreCase = true)

                        // PHÁT ÂM THANH NGAY LẬP TỨC
                        if (correct) soundManager.playSuccess() else soundManager.playWrong()

                        isAnswerCorrect = correct
                    },
                    enabled = isButtonEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(bottom = 24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MochiGreen,
                        disabledContainerColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        "KIỂM TRA",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isButtonEnabled) Color.White else Color.Gray
                    )
                }
            }

            // Bottom Sheet
            if (isAnswerCorrect != null) {
                ModalBottomSheet(
                    onDismissRequest = {},
                    sheetState = bottomSheetState,
                    containerColor = Color.Transparent,
                    dragHandle = null
                ) {
                    FeedbackBottomSheet(
                        vocab = vocab,
                        isCorrect = isAnswerCorrect!!,
                        // Dùng hàm playSmartAudio nội bộ
                        playAudio = { playSmartAudio(vocab.word, vocab.audioUrl) },
                        onContinue = {
                            val wasCorrect = isAnswerCorrect!!
                            isAnswerCorrect = null
                            userInput = ""

                            if (wasCorrect) {
                                onCorrect()
                            } else {
                                onWrong()
                                onCorrect()
                            }
                        }
                    )
                }
            }
        }
    }
}