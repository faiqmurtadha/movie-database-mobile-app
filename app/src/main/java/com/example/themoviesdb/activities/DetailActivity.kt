package com.example.themoviesdb.activities

import android.content.res.ColorStateList
import android.graphics.text.LineBreaker
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.themoviesdb.R
import com.example.themoviesdb.adapters.ActorListAdapter
import com.example.themoviesdb.adapters.FilmListAdapter
import com.example.themoviesdb.api.client.ApiClient
import com.example.themoviesdb.api.request.SetFavoriteRequest
import com.example.themoviesdb.api.response.MovieAccountStatesResponse
import com.example.themoviesdb.api.service.ApiService
import com.example.themoviesdb.domain.Datum
import com.example.themoviesdb.domain.ListActor
import com.example.themoviesdb.domain.ListFilm
import retrofit2.Call
import retrofit2.Callback
import java.text.DecimalFormat

class DetailActivity : AppCompatActivity() {
    private lateinit var adapterActors: RecyclerView.Adapter<*>
    private lateinit var viewGenres: LinearLayout
    private lateinit var recyclerViewActors: RecyclerView
    private lateinit var adapterRecommendations: RecyclerView.Adapter<*>
    private lateinit var recyclerViewRecommendations: RecyclerView
    private lateinit var loading1: ProgressBar
    private lateinit var btnBack: ImageView
    private lateinit var btnFavorite: ImageView
    private lateinit var moviePoster: ImageView
    private lateinit var movieTitle: TextView
    private lateinit var movieYear: TextView
    private lateinit var movieRating: TextView
    private lateinit var movieDuration: TextView
    private lateinit var movieSummary: TextView

    private var movieId: Int = 0
    private var accountId: Int = 0
    private lateinit var sessionId: String
    private val apiService: ApiService by lazy { ApiClient.apiService }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnBack = findViewById(R.id.btnBackDetail)
        btnFavorite = findViewById(R.id.btnFavoriteDetail)
        btnBack.setOnClickListener{
            super.onBackPressed()
        }

        movieId = intent.getIntExtra("movie_id", -1)
        accountId = getAccountId()
        sessionId = getSessionId().toString()

        initView()
        fetchMovieDetails(movieId)
        fetchAccountStatesMovie(movieId, sessionId)
        fetchMovieCredits(movieId)
        fetchRecommendationMovies(movieId)
    }

    private fun initView() {
        moviePoster = findViewById(R.id.movieDetailPoster)
        movieTitle = findViewById(R.id.movieDetailTitle)
        movieYear = findViewById(R.id.movieDetailYear)
        movieRating = findViewById(R.id.movieDetailRating)
        movieDuration = findViewById(R.id.movieDetailDuration)
        movieSummary = findViewById(R.id.summaryDetailText)
        movieSummary.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD

        viewGenres = findViewById(R.id.genreDetailList)

        recyclerViewActors = findViewById(R.id.actorsDetailList)
        recyclerViewActors.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapterActors = ActorListAdapter(ListActor(emptyList()))
        recyclerViewActors.adapter = adapterActors

        recyclerViewRecommendations = findViewById(R.id.movieRecommendationsList)
        recyclerViewRecommendations.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapterRecommendations = FilmListAdapter(ListFilm(emptyList(), 1, 1, 1))
        recyclerViewRecommendations.adapter = adapterRecommendations

        loading1 = findViewById(R.id.progressBarDetail)
    }

    private fun fetchMovieDetails(movieId: Int) {
        apiService.getMovieDetails(movieId).enqueue(object : Callback<Datum> {
            override fun onResponse(call: Call<Datum>, response: retrofit2.Response<Datum>) {
                if (response.isSuccessful) {
                    Log.i("LOG RESPONSE", "Log Fetch Detail Movie: " + response.body())
                    loading1.visibility = View.GONE
                    response.body()?.let { movieDetail ->
                        displayMovieDetails(movieDetail)
                    }
                } else {
                    Toast.makeText(this@DetailActivity, "Failed to load movie details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Datum>, t: Throwable) {
                Toast.makeText(this@DetailActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayMovieDetails(movieDetail: Datum) {
        val posterUrl = "https://image.tmdb.org/t/p/w1280" + movieDetail?.backdrop
        movieTitle.text = movieDetail?.title
        movieYear.text = movieDetail?.year?.split("-")!![0]
        movieDetail.duration?.let {
            val hours = it / 60
            val minutes = it % 60
            val durationText = if (hours > 0) {
                "${hours}h ${minutes}m"
            } else {
                "${minutes}m"
            }
            movieDuration.text = durationText
        }
        val ratingString: String? = movieDetail.rating
        val rating: Double = ratingString!!.toDouble()
        val decimalFormat = DecimalFormat("#.#")
        movieRating.text = decimalFormat.format(rating)
        movieSummary.text = movieDetail.summary

        viewGenres.removeAllViews()
        for (genre in movieDetail.movieGenres!!) {
            val genreTextView = TextView(this).apply {
                text = genre.name
                setBackgroundResource(R.drawable.labeltext_background)
                backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.secondary))
                setPadding(16, 8, 16, 8)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 16, 0)
                }
            }
            viewGenres.addView(genreTextView)
        }

        Glide.with(this)
            .load(posterUrl)
            .apply(RequestOptions.centerCropTransform())
            .into(moviePoster)
    }

    private fun fetchMovieCredits(movieId: Int) {
        apiService.getMovieCredits(movieId).enqueue(object : Callback<ListActor> {
            override fun onResponse(call: Call<ListActor>, response: retrofit2.Response<ListActor>) {
                if (response.isSuccessful) {
                    Log.i("LOG RESPONSE", "Log Fetch Movie Credits: " + response.body())
                    response.body()?.let { listCredit ->
                        adapterActors = ActorListAdapter(listCredit)
                        recyclerViewActors.adapter = adapterActors
                    }
                } else {
                    Toast.makeText(this@DetailActivity, "Failed to load Movie Credits", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ListActor>, t: Throwable) {
                Toast.makeText(this@DetailActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchRecommendationMovies(movieId: Int) {
        apiService.getMovieRecommendations(movieId).enqueue(object : Callback<ListFilm> {
            override fun onResponse(call: Call<ListFilm>, response: retrofit2.Response<ListFilm>) {
                if (response.isSuccessful) {
                    response.body()?.let { listRecommendations ->
                        adapterRecommendations = FilmListAdapter(listRecommendations)
                        recyclerViewRecommendations.adapter = adapterRecommendations
                    }
                } else {
                    Toast.makeText(this@DetailActivity, "Failed to load Now Playing Movies", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ListFilm>, t: Throwable) {
                Toast.makeText(this@DetailActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchAccountStatesMovie(movieId: Int, sessionId: String) {
        apiService.getMovieAccountStates(movieId, sessionId).enqueue(object : Callback<MovieAccountStatesResponse> {
            override fun onResponse(call: Call<MovieAccountStatesResponse>, response: retrofit2.Response<MovieAccountStatesResponse>) {
                Log.i("LOG RESPONSE", "Log Response Account States: " + response.body())
                if (response.isSuccessful) {
                    val favStatus = response.body()!!.favorite
                    if (favStatus == true) {
                        btnFavorite.apply {
                            imageTintList = ColorStateList.valueOf(resources.getColor(R.color.red))
                        }
                        updateFavoriteMovie(movieId, !favStatus)
                    } else {
                        btnFavorite.apply {
                            imageTintList = ColorStateList.valueOf(resources.getColor(R.color.secondary))
                        }
                        updateFavoriteMovie(movieId, !favStatus)
                    }
                } else {
                    Toast.makeText(this@DetailActivity, "Failed to load Account States", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MovieAccountStatesResponse>, t: Throwable) {
                Toast.makeText(this@DetailActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun updateFavoriteMovie(mediaId: Int, favStatus: Boolean) {
        btnFavorite.setOnClickListener {
            val requestBody = SetFavoriteRequest(media_type = "movie", media_id = mediaId, favorite = favStatus)

            apiService.setFavoriteMovie(accountId, sessionId, requestBody).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: retrofit2.Response<Void>) {
                    if (response.isSuccessful) {
                        if (favStatus == true) {
                            Toast.makeText(this@DetailActivity, "Movie added to favorites", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@DetailActivity, "Movie removed from favorites", Toast.LENGTH_SHORT).show()
                        }
                        fetchAccountStatesMovie(movieId, sessionId)
                    } else {
                        Toast.makeText(this@DetailActivity, "Failed to update movie", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@DetailActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun getAccountId(): Int {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        return sharedPreferences.getInt("account_id", 0)
    }

    private fun getSessionId(): String? {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        return sharedPreferences.getString("session_id", null)
    }
}