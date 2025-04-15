package com.example.DTO

import com.example.models.FlashcardType
import kotlinx.serialization.Serializable

@Serializable
data class FlashcardDTO(
    val question: String,
    val answer: String,
    val userId: Int,
    val type: FlashcardType,
    val options: List<String>? = null,
    val locations: List<String>? =null,
    val isCorrect: Boolean? = null
)

@Serializable
data class FlashcardResponseDTO(
    val id: Int,
    val question: String,
    val answer: String,
    val type: String,
    val options: List<String>? = null,
    val locations: List<String>? =null,
    val isCorrect: Boolean? = null
)