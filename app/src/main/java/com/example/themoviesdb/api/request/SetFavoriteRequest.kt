package com.example.themoviesdb.api.request

data class SetFavoriteRequest(
    val media_type: String,
    val media_id: Int,
    val favorite: Boolean
)
