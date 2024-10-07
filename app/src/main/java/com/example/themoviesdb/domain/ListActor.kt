package com.example.themoviesdb.domain

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ListActor(
    @SerializedName("cast")
    @Expose
    val data: List<ActorDatum>? = null,
)
