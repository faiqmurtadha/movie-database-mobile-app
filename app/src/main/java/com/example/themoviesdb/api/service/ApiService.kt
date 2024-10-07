package com.example.themoviesdb.api.service

import com.example.themoviesdb.api.request.SetFavoriteRequest
import com.example.themoviesdb.api.response.AccountResponse
import com.example.themoviesdb.api.response.LoginResponse
import com.example.themoviesdb.api.response.MovieAccountStatesResponse
import com.example.themoviesdb.api.response.MovieDetailResponse
import com.example.themoviesdb.api.response.RequestTokenResponse
import com.example.themoviesdb.api.response.SessionResponse
import com.example.themoviesdb.domain.Datum
import com.example.themoviesdb.domain.ListActor
import com.example.themoviesdb.domain.ListFilm
import com.example.themoviesdb.domain.ListGenre
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("authentication/token/new")
    fun createRequestToken(): Call<RequestTokenResponse>

    @POST("authentication/token/validate_with_login")
    fun validateTokenWithLogin(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("request_token") requestToken: String
    ): Call<LoginResponse>

    @POST("authentication/session/new")
    fun createSession(
        @Query("request_token") requestToken: String
    ): Call<SessionResponse>

    @GET("account")
    fun getAccountDetails(
        @Query("session_id") sessionId: String
    ): Call<AccountResponse>

    @GET("search/movie")
    fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): Call<ListFilm>

    @GET("movie/upcoming")
    fun getUpcomingMovies(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): Call<ListFilm>

    @GET("movie/now_playing")
    fun getNowPlayingMovies(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): Call<ListFilm>

    @GET("movie/popular")
    fun getPopularMovies(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): Call<ListFilm>

    @GET("movie/top_rated")
    fun getTopRatedMovies(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): Call<ListFilm>

    @GET("movie/{movie_id}")
    fun getMovieSmallDetails(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String = "en-US"
    ): Call<MovieDetailResponse>

    @GET("account/{account_id}/favorite/movies")
    fun getFavoriteMovies(
        @Query("session_id") sessionId: String
    ): Call<ListFilm>

    @GET("genre/movie/list")
    fun getGenres(
        @Query("language") language: String = "en-US"
    ): Call<ListGenre>

    @POST("account/{account_id}/favorite")
    fun setFavoriteMovie(
        @Path("account_id") accountId: Int,
        @Query("session_id") sessionId: String,
        @Body request: SetFavoriteRequest
    ): Call<Void>

    @GET("movie/{movie_id}")
    fun getMovieDetails(
        @Path("movie_id") movieId: Int
    ): Call<Datum>

    @GET("movie/{movie_id}/credits")
    fun getMovieCredits(
        @Path("movie_id") movieId: Int
    ): Call<ListActor>

    @GET("movie/{movie_id}/recommendations")
    fun getMovieRecommendations(
        @Path("movie_id") movieId: Int
    ): Call <ListFilm>

    @GET("movie/{movie_id}/account_states")
    fun getMovieAccountStates(
        @Path("movie_id") movieId: Int,
        @Query("session_id") sessionId: String
    ): Call<MovieAccountStatesResponse>

    @DELETE("authentication/session")
    fun logout(
        @Query("session_id") sessionId: String
    ): Call<ResponseBody>
}