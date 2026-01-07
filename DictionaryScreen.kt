package com.example.appandroid.screen

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appandroid.model.DictionaryEntry
import com.example.appandroid.screen.components.MochiBottomBar
import com.example.appandroid.viewmodel.LearnViewModel

// Định nghĩa màu cho dễ quản lý

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DictionaryScreen(
    navController: NavController,
    viewModel: LearnViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResult by viewModel.searchResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val focusManager = LocalFocusManager.current

    // Reset kết quả khi mới vào
    LaunchedEffect(Unit) {
        viewModel.clearSearchResult()
    }

    Scaffold(
        bottomBar = { MochiBottomBar(navController) },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // (1) Thanh Tìm Kiếm
            Text("Tra từ điển", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF8F00))
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Nhập từ tiếng Anh...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF8F00),
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (searchQuery.isNotBlank()) {
                        viewModel.searchDictionary(searchQuery)
                        focusManager.clearFocus() // Ẩn bàn phím
                    }
                })
            )

            Spacer(modifier = Modifier.height(16.dp))

            // (2) Kết quả
            if (isLoading) {
                Box(Modifier.fillMaxWidth().padding(top = 20.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF8F00))
                }
            } else if (searchResult.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(searchResult) { entry ->
                        DictionaryEntryCard(entry, viewModel)
                    }
                }
            } else if (searchQuery.isNotEmpty()) {
                // Chỉ hiện thông báo khi không loading và có từ khóa
                Box(Modifier.fillMaxWidth().padding(top = 20.dp), contentAlignment = Alignment.Center) {
                    Text("Không tìm thấy hoặc lỗi mạng.", color = Color.Gray)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DictionaryEntryCard(
    entry: DictionaryEntry,
    viewModel: LearnViewModel
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Word & Phonetic
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = entry.word, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                    entry.phonetic?.let {
                        Text(text = it, fontSize = 16.sp, color = Color.Gray, fontStyle = FontStyle.Italic)
                    }
                }

                // Nút Save
                IconButton(onClick = {
                    viewModel.saveDictionaryWord(
                        entry = entry,
                        onSuccess = {
                            Toast.makeText(context, "Đã thêm vào bài học!", Toast.LENGTH_SHORT).show()
                        },
                        onError = {
                            Toast.makeText(context, "Lỗi hoặc từ đã tồn tại!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }) {
                    Icon(
                        imageVector = Icons.Default.BookmarkAdd,
                        contentDescription = "Save"
                    )
                }

                // Nút Phát Âm (Đã sửa lỗi lag UI)
                val audioUrl = entry.phonetics?.firstOrNull { !it.audio.isNullOrBlank() }?.audio
                if (audioUrl != null) {
                    IconButton(onClick = {
                        playAudio(context, audioUrl)
                    }) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Play", tint = Color(0xFFFF8F00))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Hiển thị nghĩa (Meanings)
            entry.meanings?.forEach { meaning ->
                Text(
                    text = meaning.partOfSpeech ?: "",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    fontStyle = FontStyle.Italic
                )
                meaning.definitions?.take(3)?.forEachIndexed { _, def ->
                    Text(
                        text = "- ${def.definition}",
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    def.example?.let { ex ->
                        Text(
                            text = "Ex: $ex",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// --- HÀM XỬ LÝ AUDIO AN TOÀN (Không chặn UI) ---
fun playAudio(context: android.content.Context, url: String) {
    try {
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
        // Dùng HTTPS nếu link là HTTP (bắt buộc trên Android mới)
        val secureUrl = if (url.startsWith("//")) "https:$url" else url

        mediaPlayer.setDataSource(secureUrl)

        // QUAN TRỌNG: Dùng prepareAsync thay vì prepare để không đơ màn hình
        mediaPlayer.prepareAsync()

        mediaPlayer.setOnPreparedListener { mp ->
            mp.start()
        }

        // Giải phóng bộ nhớ khi chạy xong
        mediaPlayer.setOnCompletionListener { mp ->
            mp.release()
        }

        mediaPlayer.setOnErrorListener { mp, _, _ ->
            mp.release()
            Toast.makeText(context, "Lỗi phát âm thanh", Toast.LENGTH_SHORT).show()
            true
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Không thể phát audio", Toast.LENGTH_SHORT).show()
    }
}