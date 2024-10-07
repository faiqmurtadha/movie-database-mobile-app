package com.example.themoviesdb.api.response

data class RequestTokenResponse(
    val success: Boolean,
    val expires_at: String,
    val request_token: String
)
