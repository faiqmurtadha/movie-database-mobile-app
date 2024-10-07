package com.example.themoviesdb.domain

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ActorDatum(
    @SerializedName("id")
    @Expose
    val id: Int? = null,

    @SerializedName("name")
    @Expose
    val name: String? = null,

    @SerializedName("profile_path")
    @Expose
    val photo: String? = null,

    @SerializedName("character")
    @Expose
    val role: String? = null
)
