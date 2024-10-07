package com.example.themoviesdb.api.response

data class AccountResponse(
    val id: Int,
    val username: String,
    val name: String,
    val avatar: AccountAvatarDto
)
