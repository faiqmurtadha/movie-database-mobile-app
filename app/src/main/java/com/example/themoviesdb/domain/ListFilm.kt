package com.example.themoviesdb.domain

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ListFilm (
    @SerializedName("results")
    @Expose
    val data: List<Datum>? = null,

    @SerializedName("page")
    @Expose
    val page: Int? = null,

    @SerializedName("total_pages")
    @Expose
    val totalPages: Int? = null,

    @SerializedName("total_results")
    @Expose
    val totalResults: Int? = null
)