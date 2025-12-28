package com.example.appandroid.screen

import android.os.Build
import android.speech.tts.TextToSpeech
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.appandroid.model.Vocabulary
import com.example.appandroid.screen.components.MochiYellow
import com.example.appandroid.utils.SoundManager
import com.example.appandroid.viewmodel.LearnViewModel
import com.example.appandroid.utils.ReminderScheduler
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.Locale
import java.util.concurrent.TimeUnit
import nl.dionsegijn.konfetti.core.Position



// --- BẢNG MÀU CHUẨN ---
val MochiTextDark = Color(0xFF333333)
val MochiGrayBg = Color(0xFFF2F2F2)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FlashcardScreen(
    navController: NavController,
    viewModel: LearnViewModel,
    lessonId: Long
) {
    val context = LocalContext.current

    // --- 1. DATA & STATE ---
    val vocabList by viewModel.vocabList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var currentIndex by remember { mutableIntStateOf(0) }
    val currentVocab = vocabList.getOrNull(currentIndex)

    // --- 2. KHỞI TẠO CÁC BỘ QUẢN LÝ (MANAGERS) ---

    // SoundManager (Tiếng Flip, Ting, Èo)
    val soundManager = remember { SoundManager(context) }

    // TextToSpeech (Chị Google - Dùng để đọc chậm hoặc dự phòng)
    var tts: TextToSpeech? by remember { mutableStateOf(null) }

    // MediaPlayer (Dùng để phát file MP3 xịn từ URL)
    val mediaPlayer = remember { android.media.MediaPlayer() }

    // --- 3. LIFECYCLE (KHỞI TẠO & DỌN DẸP) ---

    // Load dữ liệu bài học
    LaunchedEffect(lessonId) {
        viewModel.loadVocabularies(lessonId)
    }

    // Khởi tạo TTS
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
    }

    // Dọn dẹp bộ nhớ khi thoát màn hình (Quan trọng!)
    DisposableEffect(Unit) {
        onDispose {
            soundManager.release() // Giải phóng SoundPool
            tts?.stop()
            tts?.shutdown()        // Giải phóng TTS
            mediaPlayer.release()  // Giải phóng MediaPlayer
        }
    }

    // --- 4. LOGIC PHÁT ÂM THANH THÔNG MINH ---
    fun playAudio(url: String?, word: String) {
        // Bước 1: Kiểm tra có link MP3 xịn không?
        if (!url.isNullOrBlank()) {
            try {
                mediaPlayer.reset() // Reset để sẵn sàng phát bài mới
                mediaPlayer.setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                mediaPlayer.setDataSource(url)
                mediaPlayer.prepareAsync() // Chuẩn bị bất đồng bộ (tránh đơ UI)

                mediaPlayer.setOnPreparedListener { it.start() } // Tải xong thì phát

                // Nếu tải MP3 bị lỗi -> Chuyển sang dùng TTS
                mediaPlayer.setOnErrorListener { _, _, _ ->
                    tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
                    true
                }
            } catch (e: Exception) {
                // Lỗi khởi tạo -> Dùng TTS
                tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        } else {
            // Bước 2: Không có link -> Dùng TTS ngay lập tức
            tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    // --- 5. ANIMATION ---
    var isFlipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "flip"
    )

    // Reset lật thẻ khi qua từ mới
    LaunchedEffect(currentIndex) { isFlipped = false }

    val interactionSource = remember { MutableInteractionSource() }

    // --- 6. GIAO DIỆN (UI) ---
    Scaffold(containerColor = MochiGrayBg) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MochiYellow)
            }
        } else if (vocabList.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Chưa có từ vựng!", color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) { Text("Quay lại") }
                }
            }
        } else if (currentIndex >= vocabList.size) {
            // 1. Gọi lệnh lên lịch nhắc nhở (Reset đồng hồ đếm ngược 24h)
            LaunchedEffect(Unit) {
                // Chỉ reset lịch nếu người dùng ĐANG BẬT tính năng này
                if (com.example.appandroid.utils.LocalStorage.isReminderEnabled(context)) {
                    com.example.appandroid.utils.ReminderScheduler.scheduleNextReminder(context)
                }
            }
            FinishScreen(
                onBack = { navController.popBackStack() },
                soundManager = soundManager
            )
        } else {
            currentVocab?.let { vocab ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(bottom = 16.dp)
                ) {
                    FlashcardHeader(
                        progress = (currentIndex + 1).toFloat() / vocabList.size,
                        onClose = { navController.popBackStack() }
                    )

                    // KHU VỰC THẺ TỪ
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    rotationY = rotation
                                    cameraDistance = 12f * density
                                }
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    isFlipped = !isFlipped
                                    soundManager.playFlip() // Âm thanh lật thẻ
                                },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (rotation <= 90f) {
                                    // MẶT TRƯỚC
                                    FrontCardDesign(
                                        vocab = vocab,
                                        // Dùng hàm playAudio xịn
                                        onSpeak = { playAudio(vocab.audioUrl, vocab.word) },
                                        // Loa chậm vẫn dùng TTS cho ổn định
                                        onSlowSpeak = {
                                            tts?.setSpeechRate(0.5f)
                                            tts?.speak(vocab.word, TextToSpeech.QUEUE_FLUSH, null, null)
                                            tts?.setSpeechRate(1.0f)
                                        }
                                    )
                                } else {
                                    // MẶT SAU
                                    Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                                        BackCardDesign(
                                            vocab = vocab,
                                            // Dùng hàm playAudio xịn
                                            onSpeak = { playAudio(vocab.audioUrl, vocab.word) },
                                            onSlowSpeak = {
                                                tts?.setSpeechRate(0.5f)
                                                tts?.speak(vocab.word, TextToSpeech.QUEUE_FLUSH, null, null)
                                                tts?.setSpeechRate(1.0f)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // FOOTER
                    FlashcardFooter(
                        onNext = {
                            viewModel.markAsLearned(vocab.id, false)
                            currentIndex++
                        },
                        onKnown = {
                            viewModel.markAsLearned(vocab.id, true)
                            currentIndex++
                        }
                    )
                }
            }
        }
    }
}
// ================= UI COMPONENTS =================

val AUDIO_BUTTON_OFFSET = 25.dp // Nửa chiều cao nút loa (50dp / 2)

@Composable
fun FrontCardDesign(vocab: Vocabulary, onSpeak: () -> Unit, onSlowSpeak: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. CARD NỀN TRẮNG
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = AUDIO_BUTTON_OFFSET),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Spacer để chừa chỗ cho nút loa
                Spacer(modifier = Modifier.height(AUDIO_BUTTON_OFFSET + 24.dp))

                // 2. ẢNH MINH HỌA (Dùng weight để linh hoạt chiều cao)
                // Ảnh sẽ co giãn để nhường chỗ cho text bên dưới
                Box(
                    modifier = Modifier
                        .weight(1f) // Chiếm phần lớn không gian, nhưng không đè text
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(0.dp),
                        modifier = Modifier
                            .fillMaxHeight(0.9f) // Cao tối đa 90% vùng chứa
                            .aspectRatio(1f)     // Giữ tỉ lệ vuông
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(vocab.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                            error = painterResource(android.R.drawable.ic_menu_report_image),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. CÂU VÍ DỤ (Luôn hiển thị)
                // Đặt trong Column fixed ở dưới để đảm bảo không bị che
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Câu ví dụ
                    HighlightedSentence(
                        sentence = vocab.exampleSentence ?: "",
                        targetWord = vocab.word
                    )

                    Spacer(modifier = Modifier.height(30.dp)) // Khoảng cách tới đáy card
                }
            }
        }

        // Icon Hint (Góc phải dưới - đè lên Card)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 24.dp) // Padding phải trùng với Card padding
        ) {
            Icon(
                painter = painterResource(android.R.drawable.ic_menu_help),
                contentDescription = null,
                tint = MochiYellow.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }

        // 4. CỤM NÚT LOA (Đè lên trên cùng)
        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            AudioButtons(onSpeak, onSlowSpeak)
        }
    }
}

@Composable
fun BackCardDesign(vocab: Vocabulary, onSpeak: () -> Unit, onSlowSpeak: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = AUDIO_BUTTON_OFFSET),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(AUDIO_BUTTON_OFFSET + 40.dp))

                // Từ vựng
                Text(
                    text = vocab.word,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MochiTextDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Phiên âm
                Text(
                    text = vocab.phonetic ?: "",
                    fontSize = 20.sp,
                    color = Color.Gray,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(30.dp))
                Divider(color = Color(0xFFEEEEEE), thickness = 1.dp, modifier = Modifier.fillMaxWidth(0.6f))
                Spacer(modifier = Modifier.height(30.dp))

                // Nghĩa
                Text(
                    text = vocab.meaning,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2),
                    textAlign = TextAlign.Center
                )
            }
        }

        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            AudioButtons(onSpeak, onSlowSpeak)
        }
    }
}

// --- Cụm nút Audio (Đã cân chỉnh đều) ---
@Composable
fun AudioButtons(onSpeak: () -> Unit, onSlowSpeak: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Loa thường
        IconButton(
            onClick = onSpeak,
            modifier = Modifier
                .size(50.dp) // Size chuẩn
                .shadow(4.dp, CircleShape)
                .background(Color.White, CircleShape)
                .border(1.dp, MochiYellow, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "Speak",
                tint = Color(0xFFFF8F00), // Cam đậm
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        // Loa chậm
        IconButton(
            onClick = onSlowSpeak,
            modifier = Modifier
                .size(50.dp) // Size bằng loa thường
                .background(Color(0xFFF5F5F5), CircleShape) // Nền xám
        ) {
            // Dùng icon VolumeUp nhưng màu khác
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "Slow Speak",
                tint = Color.Gray, // Màu xám
                modifier = Modifier.size(20.dp) // Icon nhỏ hơn chút xíu
            )
        }
    }
}

@Composable
fun FlashcardFooter(onNext: () -> Unit, onKnown: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .shadow(6.dp, RoundedCornerShape(27.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = MochiGreen),
            shape = RoundedCornerShape(27.dp)
        ) {
            Text("Tiếp tục", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // TextButton căn giữa
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TextButton(onClick = onKnown) {
                Text(
                    "Mình đã biết từ này",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    style = androidx.compose.ui.text.TextStyle(textDecoration = TextDecoration.Underline)
                )
            }
        }
    }
}

// (Các hàm HighlightedSentence, Header, FinishScreen giữ nguyên)
@Composable
fun HighlightedSentence(sentence: String, targetWord: String) {
    val annotatedString = buildAnnotatedString {
        val lowerSentence = sentence.lowercase()
        val lowerTarget = targetWord.lowercase()
        val startIndex = lowerSentence.indexOf(lowerTarget)
        if (startIndex >= 0) {
            val endIndex = startIndex + targetWord.length
            append(sentence.substring(0, startIndex))
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MochiTextDark, textDecoration = TextDecoration.Underline)) {
                append(sentence.substring(startIndex, endIndex))
            }
            append(sentence.substring(endIndex))
        } else { append(sentence) }
    }
    Text(text = annotatedString, fontSize = 18.sp, textAlign = TextAlign.Center, color = MochiTextDark, lineHeight = 28.sp)
}

@Composable
fun FlashcardHeader(progress: Float, onClose: () -> Unit) {
    Row(Modifier
        .fillMaxWidth()
        .padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Close", tint = Color.Gray) }
        Box(Modifier
            .weight(1f)
            .height(8.dp)) {
            Box(Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE0E0E0)))
            Box(Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(4.dp))
                .background(MochiYellow))
        }
        Spacer(Modifier.width(48.dp))
    }
}

@Composable
fun FinishScreen(
    onBack: () -> Unit,
    soundManager: SoundManager? = null // Truyền SoundManager vào để phát nhạc
) {
    // Kích hoạt nhạc chiến thắng khi màn hình hiện ra
    LaunchedEffect(Unit) {
        soundManager?.playSuccess()
    }

    // Cấu hình pháo hoa: Bắn từ trên xuống (Rain)
    val party = Party(
        speed = 0f,        maxSpeed = 30f,
        damping = 0.9f,
        spread = 360,
        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
        emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
        // ĐOẠN MÃ ĐÃ SỬA
// Dòng code mới đã sửa
        position = Position.Relative(0.5, 0.3)

    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Nội dung chính (Giữ nguyên code cũ)
        Column(
            Modifier
                .fillMaxSize()
                .background(Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(android.R.drawable.star_big_on),
                contentDescription = null,
                tint = MochiYellow,
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("Xuất sắc!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MochiTextDark)
            Text("Bạn đã hoàn thành bài học.", fontSize = 18.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = MochiGreen),
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(28.dp))
            ) {
                Text("Về trang chủ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 2. Pháo hoa (Đè lên trên cùng)
        KonfettiView(
            modifier = Modifier.fillMaxSize(),
            parties = listOf(party)
        )
    }
}
