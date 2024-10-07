package com.example.themoviesdb.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.themoviesdb.R
import com.example.themoviesdb.adapters.FilmListAdapter
import com.example.themoviesdb.adapters.SearchListAdapter
import com.example.themoviesdb.api.client.ApiClient
import com.example.themoviesdb.api.service.ApiService
import com.example.themoviesdb.domain.Datum
import com.example.themoviesdb.domain.ListFilm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {
    private lateinit var searchResultText: TextView
    private val apiService: ApiService by lazy { ApiClient.apiService }
    private lateinit var adapterResultMovies: RecyclerView.Adapter<*>
    private lateinit var recyclerResultMovies: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initView()
        val query = intent.getStringExtra("query")
        searchResultText.text = "Search results for: ${query}"
        fetchSearch(query.toString())
    }

    private fun initView() {
        searchResultText = findViewById(R.id.searchQuery)
        recyclerResultMovies = findViewById(R.id.searchResultsView)
        recyclerResultMovies.layoutManager = GridLayoutManager(this, 4)
        adapterResultMovies = SearchListAdapter(ListFilm(emptyList(), 1, 1, 1))
        recyclerResultMovies.adapter = adapterResultMovies
    }

    private fun fetchSearch(query: String) {
        apiService.searchMovies(query).enqueue(object : Callback<ListFilm> {
            override fun onResponse(call: Call<ListFilm>, response: Response<ListFilm>) {
                if (response.isSuccessful) {
                    Log.i("LOG RESPONSE", "Log Response Search Movie: " + response.body())
                    response.body()?.let { listSearchResults ->
                        adapterResultMovies = SearchListAdapter(listSearchResults)
                        recyclerResultMovies.adapter = adapterResultMovies
                    }
                } else {
                    Toast.makeText(this@SearchActivity, "Failed to search movies", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ListFilm>, t: Throwable) {
                Toast.makeText(this@SearchActivity, "Error fetching suggestions: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val intentDestination = Intent(this@SearchActivity, HomeActivity::class.java)
        startActivity(intentDestination)
    }
}