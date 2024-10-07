package com.example.themoviesdb.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.themoviesdb.R
import com.example.themoviesdb.api.client.ApiClient
import com.example.themoviesdb.api.response.AccountResponse
import com.example.themoviesdb.api.service.ApiService
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountActivity : AppCompatActivity() {
    private lateinit var homeNavBtn: LinearLayout
    private lateinit var favoriteNavBtn: LinearLayout
    private lateinit var accountNavBtn: LinearLayout

    private lateinit var homeNavImg: ImageView
    private lateinit var favoriteNavImg: ImageView
    private lateinit var accountNavImg: ImageView

    private lateinit var homeNavText: TextView
    private lateinit var favoriteNavText: TextView
    private lateinit var accountNavText: TextView

    private lateinit var accountPhoto: ImageView
    private lateinit var accountName: TextView
    private lateinit var accountUsername: TextView
    private lateinit var signOutText: TextView
    private lateinit var signOutBtn: LinearLayout

    private lateinit var sessionId: String
    private val apiService: ApiService by lazy { ApiClient.apiService }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account)
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

        accountPhoto = findViewById(R.id.account_photo)
        accountName = findViewById(R.id.account_name_text)
        accountUsername = findViewById(R.id.account_username_text)
        signOutText = findViewById(R.id.signOutText)
        signOutBtn = findViewById(R.id.signOutBtn)

        setActiveNavItem()

        homeNavBtn.setOnClickListener {
            val intentDestination = Intent(this@AccountActivity, HomeActivity::class.java)
            startActivity(intentDestination)
        }

        favoriteNavBtn.setOnClickListener {
            val intentDestination = Intent(this@AccountActivity, FavoriteActivity::class.java)
            startActivity(intentDestination)
        }

        accountNavBtn.setOnClickListener {
            val intentDestination = Intent(this@AccountActivity, AccountActivity::class.java)
            startActivity(intentDestination)
        }

        signOutBtn.setOnClickListener {
            logout()
        }

        sessionId = getSessionId().toString()
        signOutText.setTextColor(resources.getColor(R.color.main))
        fetchAccountDetails()
    }

    private fun setActiveNavItem() {
        resetNavItems()

        // Set active item
        accountNavImg.setImageResource(R.drawable.account_nav)
        accountNavImg.setColorFilter(ContextCompat.getColor(this, R.color.white))
        accountNavText.setTextColor(ContextCompat.getColor(this, R.color.white))
        accountNavBtn.isClickable = false
        accountNavBtn.isEnabled = false
    }

    private fun resetNavItems() {
        homeNavImg.setColorFilter(ContextCompat.getColor(this, R.color.third))
        homeNavText.setTextColor(ContextCompat.getColor(this, R.color.third))

        favoriteNavImg.setColorFilter(ContextCompat.getColor(this, R.color.third))
        favoriteNavText.setTextColor(ContextCompat.getColor(this, R.color.third))

        accountNavImg.setColorFilter(ContextCompat.getColor(this, R.color.third))
        accountNavText.setTextColor(ContextCompat.getColor(this, R.color.third))
    }

    private fun fetchAccountDetails() {
        apiService.getAccountDetails(sessionId).enqueue(object : Callback<AccountResponse> {
            override fun onResponse(call: Call<AccountResponse>, response: Response<AccountResponse>) {
                Log.i("LOD RESPONSE", "Log Response Account Detail: " + response.body())
                if (response.isSuccessful) {
                    val photo = response.body()?.avatar?.tmdb?.avatar_path
                    val photoUrl = "https://image.tmdb.org/t/p/w500" + photo
                    val name = response.body()?.name
                    val username = response.body()?.username

                    if (response.body() != null) {
                        accountName.text = name
                        accountUsername.text = username

                        Glide.with(this@AccountActivity)
                            .load(photoUrl)
                            .apply(RequestOptions.circleCropTransform())
                            .into(accountPhoto)
                    } else {
                        Toast.makeText(this@AccountActivity, "Account detail is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@AccountActivity, "Failed to load Account Detail", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AccountResponse>, t: Throwable) {
                Toast.makeText(this@AccountActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun logout() {
        apiService.logout(sessionId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    removeSessionId()
                    Toast.makeText(this@AccountActivity, "Sign out successful", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@AccountActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@AccountActivity, "Logout failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@AccountActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getSessionId(): String? {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        return sharedPreferences.getString("session_id", null)
    }

    private fun removeSessionId() {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}