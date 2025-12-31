package com.example.appandroid.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DictionaryEntry(
    val word: String,
    val phonetic: String? = null,
    val phonetics: List<PhoneticDto>? = null,
    val meanings: List<MeaningDto>? = null
)

@Serializable
data class PhoneticDto(
    val text: String? = null,
    val audio: String? = null
)

@Serializable
data class MeaningDto(
    val partOfSpeech: String? = null, // Danh từ, động từ...
    val definitions: List<DefinitionDto>? = null
)

@Serializable
data class DefinitionDto(
    val definition: String? = null,
    val example: String? = null
)