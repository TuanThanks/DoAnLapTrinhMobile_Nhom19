package com.example.appandroid.screen

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appandroid.utils.SoundManager
import com.example.appandroid.viewmodel.LearnViewModel

// --- KHAI BÁO MÀU (Thêm MochiGreen/Dark nếu file khác chưa có) ---
val ReviewGray = Color(0xFFE0E0E0)
val CorrectGreen = Color(0xFF4CAF50)
val WrongRed = Color(0xFFF44336)
// Nếu project bạn đã có file UiTheme chứa 2 màu này thì xóa 2 dòng dưới đi


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReviewFlashcardScreen(
    navController: NavController,
    viewModel: LearnViewModel
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    val focusManager = LocalFocusManager.current

    // --- LOGIC 1: VỪA VÀO MÀN HÌNH LÀ RESET LỊCH NGAY (HOÃN BINH) ---
    LaunchedEffect(Unit) {
        val storage = com.example.appandroid.utils.LocalStorage(context)
        if (storage.isReminderEnabled()) {
            com.example.appandroid.utils.ReminderScheduler.scheduleNextReminder(context)
        }
    }

    // 1. Lấy danh sách review
    val reviewList by viewModel.reviewList.collectAsState()

    // 2. Luôn lấy từ đầu tiên
    val currentVocab = reviewList.firstOrNull()

    // State nhập liệu
    var userInput by remember { mutableStateOf("") }
    var isChecked by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }

    // 3. Reset form khi từ vựng thay đổi
    LaunchedEffect(currentVocab) {
        userInput = ""
        isChecked = false
        isCorrect = false
    }

    Scaffold(containerColor = Color.White) { padding ->
        if (reviewList.isEmpty()) {
            // --- LOGIC 2: HỌC XONG THÌ RESET LỊCH (CHỐT HẠ) ---
            // Dùng luôn biến 'context' ở trên, không cần tạo 'ctx' mới
            LaunchedEffect(Unit) {
                val storage = com.example.appandroid.utils.LocalStorage(context)
                if (storage.isReminderEnabled()) {
                    com.example.appandroid.utils.ReminderScheduler.scheduleNextReminder(context)
                }
            }

            FinishScreen(onBack = { navController.popBackStack() })
        } else {
            currentVocab?.let { vocab ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
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

                    Spacer(modifier = Modifier.height(40.dp))

                    // Câu hỏi
                    Text(text = "Điền từ tiếng Anh", fontSize = 16.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = vocab.meaning,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // Ô nhập liệu
                    val wordLength = vocab.word.length

                    BasicTextField(
                        value = userInput,
                        onValueChange = { if (!isChecked) userInput = it },
                        textStyle = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = if (isChecked) (if (isCorrect) CorrectGreen else WrongRed) else Color.Black,
                            letterSpacing = 4.sp
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            if (userInput.isNotBlank() && !isChecked) {
                                val result = userInput.trim().equals(vocab.word, ignoreCase = true)
                                isCorrect = result
                                isChecked = true
                                if (result) soundManager.playSuccess() else soundManager.playWrong()
                            }
                        }),
                        cursorBrush = SolidColor(MochiGreen),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (userInput.isEmpty()) {
                                    Text(
                                        text = "_ ".repeat(wordLength),
                                        fontSize = 28.sp,
                                        color = Color.LightGray,
                                        letterSpacing = 4.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    if (isChecked && !isCorrect) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Đáp án: ${vocab.word}",
                            color = CorrectGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Footer Buttons
                    if (!isChecked) {
                        Button(
                            onClick = {
                                if (userInput.isNotBlank()) {
                                    val result = userInput.trim().equals(vocab.word, ignoreCase = true)
                                    isCorrect = result
                                    isChecked = true
                                    if (result) soundManager.playSuccess() else soundManager.playWrong()
                                } else {
                                    Toast.makeText(context, "Hãy nhập từ vựng!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp).shadow(4.dp, RoundedCornerShape(28.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = ReviewGray),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text("Kiểm tra", fontSize = 18.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(onClick = {
                            isCorrect = false
                            isChecked = true
                            userInput = vocab.word
                            soundManager.playWrong()
                        }) {
                            Text(
                                "Mình không thuộc từ này",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                style = TextStyle(textDecoration = TextDecoration.Underline)
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.submitReviewResult(
                                    vocabId = vocab.id,
                                    currentLevel = 1,
                                    isRemembered = isCorrect
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp).shadow(4.dp, RoundedCornerShape(28.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCorrect) CorrectGreen else MochiTextDark
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text("Tiếp tục", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}