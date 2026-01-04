package com.example.appandroid.screen.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appandroid.R
import com.example.appandroid.model.Vocabulary
import com.example.appandroid.screen.MochiGreen
import com.example.appandroid.screen.MochiRed
import com.example.appandroid.screen.MochiTextDark
// Import ML Kit
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.delay

@Composable
fun FeedbackBottomSheet(
    vocab: Vocabulary,
    isCorrect: Boolean,
    onContinue: () -> Unit,
    playAudio: () -> Unit
) {
    // 1. STATE
    var showTranslation by remember { mutableStateOf(false) }
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // 2. LOGIC TỰ ĐỘNG PHÁT ÂM
    LaunchedEffect(Unit) {
        delay(300)
        playAudio()
    }

    // 3. LOGIC XỬ LÝ DỊCH (Đưa logic vào đây cho gọn)
    // Khi showTranslation = true, code trong này sẽ chạy
    LaunchedEffect(showTranslation) {
        if (showTranslation && translatedText.isEmpty()) {
            val sentence = vocab.exampleSentence
            if (sentence.isNullOrBlank()) {
                translatedText = "Không có câu ví dụ để dịch."
                return@LaunchedEffect
            }

            isTranslating = true

            // Cấu hình ML Kit
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.VIETNAMESE)
                .build()
            val translator = Translation.getClient(options)
            val conditions = DownloadConditions.Builder().requireWifi().build()

            // Bắt đầu tải và dịch
            translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    translator.translate(sentence)
                        .addOnSuccessListener { result ->
                            translatedText = result
                            isTranslating = false
                        }
                        .addOnFailureListener {
                            translatedText = "Lỗi khi dịch câu."
                            isTranslating = false
                        }
                }
                .addOnFailureListener {
                    translatedText = "Cần kết nối mạng để tải gói ngôn ngữ."
                    isTranslating = false
                }
        }
    }

    // 4. BIẾN UI
    val themeColor = if (isCorrect) MochiGreen else MochiRed
    val iconRes = if (isCorrect) R.drawable.ic_happy else R.drawable.ic_boring
    val titleText = if (isCorrect) "Chính xác!" else "Chưa chính xác"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        // --- HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(themeColor)
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = titleText,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Collapse",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // --- NỘI DUNG ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = playAudio,
                    modifier = Modifier
                        .size(40.dp)
                        .background(MochiGreen.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Speak", tint = MochiGreen)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = vocab.word, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MochiTextDark)
                    Text(text = "(n) ${vocab.phonetic ?: ""}", fontSize = 16.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Nghĩa: ${vocab.meaning}", fontSize = 18.sp, color = MochiTextDark)

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(16.dp))

            // --- VÍ DỤ & DỊCH ---
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Ví dụ:", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(
                        text = vocab.exampleSentence ?: "Chưa có câu ví dụ",
                        fontSize = 16.sp,
                        color = MochiTextDark,
                        lineHeight = 24.sp
                    )

                    // Hiển thị phần dịch
                    if (showTranslation) {
                        Spacer(modifier = Modifier.height(8.dp))
                        if (isTranslating) {
                            Text(text = "Đang tải bản dịch...", fontSize = 14.sp, color = Color.Gray, fontStyle = FontStyle.Italic)
                        } else {
                            Text(
                                text = "Dịch: $translatedText",
                                fontSize = 15.sp,
                                color = Color(0xFF1976D2),
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { showTranslation = !showTranslation },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "Translate",
                        tint = if (showTranslation) MochiGreen else Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- BUTTON ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("TIẾP TỤC", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}