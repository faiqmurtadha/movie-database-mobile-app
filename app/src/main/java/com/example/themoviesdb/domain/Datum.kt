package com.example.themoviesdb.domain

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Datum(
    @SerializedName("id")
    @Expose
    val id: Int? = null,

    @SerializedName("title")
    @Expose
    val title: String? = null,

    @SerializedName("poster_path")
    @Expose
    val poster: String? = null,

    @SerializedName("release_date")
    @Expose
    val year: String? = null,

    @SerializedName("runtime")
    @Expose
    var duration: Int? = null,

    @SerializedName("origin_country")
    @Expose
    val country: List<String>? = null,

    @SerializedName("vote_average")
    @Expose
    val rating: String? = null,

    @SerializedName("genre_ids")
    @Expose
    val genres: List<Int>? = null,

    @SerializedName("genres")
    @Expose
    val movieGenres: List<GenreDatum>? = null,

    @SerializedName("overview")
    @Expose
    val summary: String? = null,

    @SerializedName("backdrop_path")
    @Expose
    val backdrop: String? = null
)
