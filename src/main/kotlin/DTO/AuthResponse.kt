package com.example.DTO
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val message: String,
    val userId: Int
)
