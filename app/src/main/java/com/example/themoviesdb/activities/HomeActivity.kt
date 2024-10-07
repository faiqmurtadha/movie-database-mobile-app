package com.example.themoviesdb.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.transition.Slide
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
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
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.themoviesdb.adapters.SliderAdapters
import com.example.themoviesdb.domain.SliderItems
import com.example.themoviesdb.R
import com.example.themoviesdb.adapters.FilmListAdapter
import com.example.themoviesdb.adapters.TopRatedListAdapter
import com.example.themoviesdb.api.client.ApiClient
import com.example.themoviesdb.api.response.AccountResponse
import com.example.themoviesdb.api.response.MovieDetailResponse
import com.example.themoviesdb.api.service.ApiService
import com.example.themoviesdb.domain.Datum
import com.example.themoviesdb.domain.ListFilm
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class HomeActivity : AppCompatActivity() {
    private lateinit var homeNavBtn: LinearLayout
    private lateinit var favoriteNavBtn: LinearLayout
    private lateinit var accountNavBtn: LinearLayout

    private lateinit var homeNavImg: ImageView
    private lateinit var favoriteNavImg: ImageView
    private lateinit var accountNavImg: ImageView

    private lateinit var homeNavText: TextView
    private lateinit var favoriteNavText: TextView
    private lateinit var accountNavText: TextView
    private lateinit var userGreetText: TextView
    private lateinit var searchMovie: EditText

    private lateinit var adapterNowPlaying: RecyclerView.Adapter<*>
    private lateinit var adapterPopular: RecyclerView.Adapter<*>
    private lateinit var adapterTopRated: RecyclerView.Adapter<*>
    private lateinit var recyclerViewNowPlaying: RecyclerView
    private lateinit var recyclerViewPopular: RecyclerView
    private lateinit var recyclerViewTopRated: RecyclerView
    private lateinit var loading1: ProgressBar
    private lateinit var loading2: ProgressBar
    private lateinit var loading3: ProgressBar
    private lateinit var viewPager2: ViewPager2
    private lateinit var sliderAdapters: SliderAdapters
    private val slideHandler: Handler = Handler()

    private lateinit var sessionId: String
    private val apiService: ApiService by lazy { ApiClient.apiService }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userGreetText = findViewById(R.id.userGreet)

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
            val intentDestination = Intent(this@HomeActivity, HomeActivity::class.java)
            startActivity(intentDestination)
        }

        favoriteNavBtn.setOnClickListener {
            val intentDestination = Intent(this@HomeActivity, FavoriteActivity::class.java)
            startActivity(intentDestination)
        }

        accountNavBtn.setOnClickListener {
            val intentDestination = Intent(this@HomeActivity, AccountActivity::class.java)
            startActivity(intentDestination)
        }

        initView()
        sessionId = getSessionId().toString()
        searchMovie.setOnEditorActionListener {v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                val query = searchMovie.text.toString().trim()
                hideKeyboard()
                if (query.isNotEmpty()) {
                    val intent = Intent(this, SearchActivity::class.java)
                    intent.putExtra("query", query)
                    startActivity(intent)
                }

                true
            } else {
                false
            }
        }
        fetchUpcomingMovies()
        fetchAccountDetails()
        fetchNowPlayingMovies()
        fetchPopularMovies()
        fetchTopRatedMovies()
    }

    private fun setActiveNavItem() {
        resetNavItems()

        // Set active item
        homeNavImg.setImageResource(R.drawable.home_nav)
        homeNavImg.setColorFilter(ContextCompat.getColor(this, R.color.white))
        homeNavText.setTextColor(ContextCompat.getColor(this, R.color.white))
        homeNavBtn.isClickable = false
        homeNavBtn.isEnabled = false
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
        searchMovie = findViewById(R.id.searchBar)

        viewPager2 = findViewById(R.id.viewPagerSlider);
        recyclerViewNowPlaying = findViewById(R.id.nowPlayingView)
        recyclerViewNowPlaying.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapterNowPlaying = FilmListAdapter(ListFilm(emptyList(), 1, 1, 1))
        recyclerViewNowPlaying.adapter = adapterNowPlaying

        recyclerViewPopular = findViewById(R.id.popularView)
        recyclerViewPopular.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapterPopular = FilmListAdapter(ListFilm(emptyList(), 1, 1, 1))
        recyclerViewPopular.adapter = adapterPopular

        recyclerViewTopRated = findViewById(R.id.topRatedView)
        recyclerViewTopRated.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapterTopRated = TopRatedListAdapter(ListFilm(emptyList(), 1, 1, 1))
        recyclerViewTopRated.adapter = adapterTopRated

        loading1 = findViewById(R.id.progressBarNowPlaying)
        loading2 = findViewById(R.id.progressBarPopular)
        loading3 = findViewById(R.id.progressBartopRated)
    }

    private val sliderRunnable = Runnable {
        viewPager2.currentItem = viewPager2.currentItem + 1
    }

    override fun onPause() {
        super.onPause()
        slideHandler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        slideHandler.postDelayed(sliderRunnable, 2000)
    }

    private fun fetchAccountDetails() {
        apiService.getAccountDetails(sessionId).enqueue(object : Callback<AccountResponse> {
            override fun onResponse(call: Call<AccountResponse>, response: Response<AccountResponse>) {
                Log.i("LOD RESPONSE", "Log Response Account Detail: " + response.body())
                if (response.isSuccessful) {
                    val name = response.body()?.name
                    val formattedName = getFormattedName(name.toString())
                    val accountId = response.body()!!.id
                    saveAccountId(accountId)
                    if (name != null) {
                        userGreetText.text = "Hello, $formattedName"
                    } else {
                        Toast.makeText(this@HomeActivity, "Name is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@HomeActivity, "Failed to load Account Detail", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AccountResponse>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchUpcomingMovies() {
        apiService.getUpcomingMovies().enqueue(object : Callback<ListFilm> {
            override fun onResponse(call: Call<ListFilm>, response: Response<ListFilm>) {
                if (response.isSuccessful) {
                    response.body()?.let { listUpcoming ->
                        val sliderItems = listUpcoming.data?.map { movie ->
                                SliderItems(
                                image = "https://image.tmdb.org/t/p/w780${movie.backdrop}",
                                title = movie.title.toString()
                            )
                        }

                        if (sliderItems != null && sliderItems.isNotEmpty()) {
                            sliderAdapters = SliderAdapters(sliderItems, viewPager2)
                            viewPager2.adapter = sliderAdapters
                            viewPager2.clipToPadding = false
                            viewPager2.clipChildren = false
                            viewPager2.offscreenPageLimit = 3
                            viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

                            val compositePageTransformer = CompositePageTransformer().apply {
                                addTransformer(MarginPageTransformer(40))
                                addTransformer{ page, position ->
                                    val r = 1 - Math.abs(position)
                                    page.scaleY = 0.85f + r * 0.15f
                                }
                            }

                            viewPager2.setPageTransformer(compositePageTransformer)
                            viewPager2.currentItem = 1
                            viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                                override fun onPageSelected(position : Int) {
                                    super.onPageSelected(position)
                                    slideHandler.removeCallbacks(sliderRunnable)
                                }
                            })
                        }
                    }
                } else {
                    Toast.makeText(this@HomeActivity, "Failed to load Upcoming Movies", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ListFilm>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchNowPlayingMovies() {
        apiService.getNowPlayingMovies().enqueue(object : Callback<ListFilm> {
            override fun onResponse(call: Call<ListFilm>, response: Response<ListFilm>) {
                if (response.isSuccessful) {
                    Log.i("LOG RESPONSE", "Log Response Now Playing Movies: " + response.body())
                    response.body()?.let { listNowPlaying ->
                        adapterNowPlaying = FilmListAdapter(listNowPlaying)
                        recyclerViewNowPlaying.adapter = adapterNowPlaying
                        loading1.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(this@HomeActivity, "Failed to load Now Playing Movies", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ListFilm>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchPopularMovies() {
        apiService.getPopularMovies().enqueue(object : Callback<ListFilm> {
            override fun onResponse(call: Call<ListFilm>, response: Response<ListFilm>) {
                if (response.isSuccessful) {
                    response.body()?.let { listPopular ->
                        adapterPopular = FilmListAdapter(listPopular)
                        recyclerViewPopular.adapter = adapterPopular
                        loading2.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(this@HomeActivity, "Failed to load Popular Movies", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ListFilm>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchTopRatedMovies() {
        apiService.getTopRatedMovies().enqueue(object : Callback<ListFilm> {
            override fun onResponse(call: Call<ListFilm>, response: Response<ListFilm>) {
                if (response.isSuccessful) {
                    response.body()?.let { listTopRated ->
                        for (movie in listTopRated.data!!) {
                            fetchMovieRuntime(movie)
                        }
                        adapterTopRated = TopRatedListAdapter(listTopRated)
                        recyclerViewTopRated.adapter = adapterTopRated
                        loading3.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(this@HomeActivity, "Failed to load Popular Movies", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ListFilm>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchMovieRuntime(movie: Datum) {
        apiService.getMovieSmallDetails(movie.id!!).enqueue(object : Callback<MovieDetailResponse> {
            override fun onResponse(call: Call<MovieDetailResponse>, response: Response<MovieDetailResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { movieDetails ->
                        movie.duration = movieDetails.runtime
                        adapterTopRated.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(this@HomeActivity, "Failed to load Popular Movies", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MovieDetailResponse>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {

    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    private fun getSessionId(): String? {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        return sharedPreferences.getString("session_id", null)
    }

    private fun getFormattedName(name: String): String {
        val words = name.trim().split("\\s+".toRegex())
        return if (words.size > 2) {
            "${words[0]} ${words[1]}"
        } else {
            name
        }
    }

    private fun saveAccountId(accountId: Int) {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("account_id", accountId)
        editor.apply()
    }
}