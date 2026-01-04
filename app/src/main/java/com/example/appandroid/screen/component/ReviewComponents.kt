package com.example.appandroid.screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appandroid.screen.MochiBlue
import com.example.appandroid.screen.MochiGreen
import com.example.appandroid.screen.MochiRed

// --- 1. GIAO DIỆN TRẮC NGHIỆM (4 Nút) ---
@Composable
fun MultipleChoiceQuestionView(
    options: List<String>,
    onAnswerSelected: (String) -> Unit,
    isAnswered: Boolean,       // Đã chọn chưa?
    selectedAnswer: String?,   // Đáp án người dùng chọn
    correctAnswer: String      // Đáp án đúng
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        options.forEach { option ->
            // Xác định màu sắc nút dựa trên kết quả
            val backgroundColor = when {
                !isAnswered -> Color.White
                option == correctAnswer -> MochiGreen.copy(alpha = 0.2f) // Đáp án đúng luôn hiện xanh nhạt
                option == selectedAnswer -> MochiRed.copy(alpha = 0.2f)  // Nếu chọn sai thì hiện đỏ
                else -> Color.White
            }

            val borderColor = when {
                !isAnswered -> Color(0xFFE0E0E0)
                option == correctAnswer -> MochiGreen
                option == selectedAnswer -> MochiRed
                else -> Color(0xFFE0E0E0)
            }

            // UI của 1 nút đáp án
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor)
                    .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                    .clickable(enabled = !isAnswered) {
                        onAnswerSelected(option)
                    }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = option,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun TypingQuestionView(
    currentInput: String,           // <-- Nhận dữ liệu từ màn hình cha
    onInputChange: (String) -> Unit,// <-- Báo cho cha biết khi gõ
    onAnswerSubmit: () -> Unit,
    hintText: String? = null,
    isAnswered: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (hintText != null) {
            Text("Gợi ý: $hintText", color = Color.Gray, fontSize = 18.sp, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = currentInput,
            onValueChange = { if (!isAnswered) onInputChange(it) }, // Chỉ cho nhập khi chưa trả lời
            label = { Text("Nhập từ vựng") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isAnswered, // Khóa ô nhập khi đã hiện đáp án
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (currentInput.isNotBlank()) onAnswerSubmit()
            })
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { if (currentInput.isNotBlank()) onAnswerSubmit() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MochiBlue),
            enabled = !isAnswered
        ) {
            Text("Kiểm tra")
        }
    }
}