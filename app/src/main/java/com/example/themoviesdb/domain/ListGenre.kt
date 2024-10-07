package com.example.themoviesdb.domain

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ListGenre(
    @SerializedName("genres")
    @Expose
    val data: List<GenreDatum>? = null,
)
