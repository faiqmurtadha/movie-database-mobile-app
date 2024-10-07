package com.example.themoviesdb.api.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        var originalUrl = originalRequest.url()

        // Adding the api_key to URL
        var newUrl = originalUrl.newBuilder()
            .addQueryParameter("api_key", apiKey)
            .build()

        // Create new request with the modified URL
        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}