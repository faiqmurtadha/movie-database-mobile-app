package com.example.themoviesdb.api.response

data class MovieAccountStatesResponse(
    val id: Int,
    val favorite: Boolean,
    val rated: Boolean,
    val watchlist: Boolean
)
