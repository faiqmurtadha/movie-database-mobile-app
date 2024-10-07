package com.example.themoviesdb.activities

import android.content.Intent
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
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.themoviesdb.R
import com.example.themoviesdb.adapters.FavoriteListAdapter
import com.example.themoviesdb.api.client.ApiClient
import com.example.themoviesdb.api.request.SetFavoriteRequest
import com.example.themoviesdb.api.response.MovieDetailResponse
import com.example.themoviesdb.api.service.ApiService
import com.example.themoviesdb.domain.Datum
import com.example.themoviesdb.domain.ListFilm
import com.example.themoviesdb.domain.ListGenre
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FavoriteActivity : AppCompatActivity() {
    private lateinit var homeNavBtn: LinearLayout
    private lateinit var favoriteNavBtn: LinearLayout
    private lateinit var accountNavBtn: LinearLayout

    private lateinit var homeNavImg: ImageView
    private lateinit var favoriteNavImg: ImageView
    private lateinit var accountNavImg: ImageView

    private lateinit var homeNavText: TextView
    private lateinit var favoriteNavText: TextView
    private lateinit var accountNavText: TextView

    private lateinit var adapterFavorite: RecyclerView.Adapter<*>
    private lateinit var recyclerViewFavorite: RecyclerView
    private lateinit var emptyView: View
    private lateinit var loading: ProgressBar

    private var accountId: Int = 0
    private lateinit var sessionId: String
    private val apiService: ApiService by lazy { ApiClient.apiService }
    private val genreMap = mutableMapOf<Int, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_favorite)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        homeNavBtn = findViewById(R.id.homeNavLayout)
        favoriteNavBtn = findViewById(R.id.favoriteNavLayout)
        accountNavBtn = findViewById(R.id.accountNavLayout)

        homeNavImg = findViewById(R.id.homeNavImg)
        favoriteNavImg = findViewById(R.id.favoriteNavImg)
        accountNavImg = findViewById(R.id.accountNavImg)

        homeNavText = findViewById(R.id.homeNavText)
        favoriteNavText = findViewById(R.id.favoriteNavText)
        accountNavText = findViewById(R.id.accountNavText)

        setActiveNavItem()

        homeNavBtn.setOnClickListener {
            val intentDestination = Intent(this@FavoriteActivity, HomeActivity::class.java)
            startActivity(intentDestination)
        }

        favoriteNavBtn.setOnClickListener {
            val intentDestination = Intent(this@FavoriteActivity, FavoriteActivity::class.java)
            startActivity(intentDestination)
        }

        accountNavBtn.setOnClickListener {
            val intentDestination = Intent(this@FavoriteActivity, AccountActivity::class.java)
            startActivity(intentDestination)
        }

        initView()
        accountId = getAccountId()
        sessionId = getSessionId().toString()
        fetchGenres()
    }

    private fun setActiveNavItem() {
        resetNavItems()

        // Set active item
        favoriteNavImg.setImageResource(R.drawable.favorites_nav)
        favoriteNavImg.setColorFilter(ContextCompat.getColor(this, R.color.white))
        favoriteNavText.setTextColor(ContextCompat.getColor(this, R.color.white))
        favoriteNavBtn.isClickable = false
        favoriteNavBtn.isEnabled = false
    }

    private fun resetNavItems() {
        homeNavImg.setColorFilter(ContextCompat.getColor(this, R.color.third))
        homeNavText.setTextColor(ContextCompat.getColor(this, R.color.third))

        favoriteNavImg.setColorFilter(ContextCompat.getColor(this, R.color.third))
        favoriteNavText.setTextColor(ContextCompat.getColor(this, R.color.third))

        accountNavImg.setColorFilter(ContextCompat.getColor(this, R.color.third))
        accountNavText.setTextColor(ContextCompat.getColor(this, R.color.third))
    }

    private fun initView() {
        recyclerViewFavorite = findViewById(R.id.favoriteView)
        emptyView = findViewById(R.id.empty_favorite_list)
        recyclerViewFavorite.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        loading = findViewById(R.id.progressBarFavorite)
    }

    private fun fetchGenres() {
        apiService.getGenres().enqueue(object : Callback<ListGenre> {
            override fun onResponse(call: Call<ListGenre>, response: Response<ListGenre>) {
                if (response.isSuccessful) {
                    response.body()?.let { listGenre ->
                        listGenre.data?.let{ genres ->
                            genres.forEach{ genre ->
                                genreMap[genre.id] = genre.name
                            }
                            Log.d("FavoriteActivity", "Genres loaded: $genreMap")
                        }
                        fetchFavoriteMovies()
                    }
                } else {
                    Toast.makeText(this@FavoriteActivity, "Failed to load genres", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ListGenre>, t: Throwable) {
                Toast.makeText(this@FavoriteActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchFavoriteMovies() {
        apiService.getFavoriteMovies(sessionId).enqueue(object : Callback<ListFilm>{
            override fun onResponse(call: Call<ListFilm>, response: Response<ListFilm>) {
                if (response.isSuccessful) {
                    response.body()?.let { listFavorite ->
                        for (favorite in listFavorite.data!!) {
                            fetchMovieRuntime(favorite)
                        }
                        loading.visibility = View.GONE

                        if (response.body()?.data?.isEmpty() == true) {
                            recyclerViewFavorite.visibility = View.GONE
                            emptyView.visibility = View.VISIBLE

                        } else {
                            adapterFavorite = FavoriteListAdapter(listFavorite, genreMap, this@FavoriteActivity)
                            recyclerViewFavorite.adapter = adapterFavorite
                            recyclerViewFavorite.visibility = View.VISIBLE
                            emptyView.visibility = View.GONE
                        }
                    }
                } else {
                    Toast.makeText(this@FavoriteActivity, "Failed to load favorites", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ListFilm>, t: Throwable) {
                Toast.makeText(this@FavoriteActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun fetchMovieRuntime(movie: Datum) {
        apiService.getMovieSmallDetails(movie.id!!).enqueue(object: Callback<MovieDetailResponse> {
            override fun onResponse(call: Call<MovieDetailResponse>, response: Response<MovieDetailResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { movieDetails ->
                        movie.duration = movieDetails.runtime
                        adapterFavorite.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(this@FavoriteActivity, "Failed to load favorites", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MovieDetailResponse>, t: Throwable) {
                Toast.makeText(this@FavoriteActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun removeFavoriteMovie(mediaId: Int) : Response<Void> {
        val requestBody = SetFavoriteRequest(media_type = "movie", media_id = mediaId, favorite = false)
        var responseRemove: Response<Void> = Response.success(null)

        apiService.setFavoriteMovie(accountId, sessionId, requestBody).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                responseRemove = response
                if (response.isSuccessful) {
                    Toast.makeText(this@FavoriteActivity, "Movie removed from favorites", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@FavoriteActivity, "Failed to remove movie", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@FavoriteActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
        return responseRemove
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