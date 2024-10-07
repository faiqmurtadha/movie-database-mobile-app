package com.example.themoviesdb.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.themoviesdb.R
import com.example.themoviesdb.api.client.ApiClient
import com.example.themoviesdb.api.response.LoginResponse
import com.example.themoviesdb.api.response.RequestTokenResponse
import com.example.themoviesdb.api.response.SessionResponse
import com.example.themoviesdb.api.service.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var errorTextView: TextView
    private lateinit var btnSignIn: Button
    private lateinit var btnBack: Button
    private val apiService: ApiService by lazy { ApiClient.apiService }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        usernameEditText = findViewById(R.id.usernameForm)
        passwordEditText = findViewById(R.id.passwordForm)
        errorTextView = findViewById(R.id.errorTextView)

        btnSignIn = findViewById(R.id.buttonSignIn)
        btnBack = findViewById(R.id.buttonBack)

        btnSignIn.setOnClickListener { signIn() }

        btnBack.setOnClickListener {
            val intentDestination = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intentDestination)
        }
    }

    private fun signIn() {
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            errorTextView.setText("Username or Password cannot be empty")
            errorTextView.visibility = View.VISIBLE
            return
        }

        // Get Request Token
        apiService.createRequestToken().enqueue(object : Callback<RequestTokenResponse> {
            override fun onResponse(call: Call<RequestTokenResponse>, response: Response<RequestTokenResponse>) {
                val requestToken = response.body()?.request_token
                if (requestToken != null) {
                    validateWithLogin(username, password, requestToken)
                } else {
                    errorTextView.setText("Failed to get request token")
                    errorTextView.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<RequestTokenResponse>, t: Throwable) {
                errorTextView.setText("Error: ${t.message}")
                errorTextView.visibility = View.VISIBLE
            }
        })
    }

    private fun validateWithLogin(username: String, password: String, requestToken: String) {
        apiService.validateTokenWithLogin(username, password, requestToken)
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val validatedToken = response.body()?.request_token
                        if (validatedToken != null) {
                            createSession(validatedToken)
                        } else {
                            errorTextView.setText("Failed to validate token")
                            errorTextView.visibility = View.VISIBLE
                        }
                    } else {
                        errorTextView.setText("Invalid username or password")
                        errorTextView.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    errorTextView.setText("Error: ${t.message}")
                    errorTextView.visibility = View.VISIBLE
                }
            })
    }

    private fun createSession(requestToken: String) {
        apiService.createSession(requestToken).enqueue(object : Callback<SessionResponse> {
            override fun onResponse(call: Call<SessionResponse>, response: Response<SessionResponse>) {
                if (response.isSuccessful) {
                    val sessionId = response.body()?.session_id
                    if (sessionId != null) {
                        saveSessionId(sessionId)
                        Log.i("LOG SESSION", "Log Session Id: " + sessionId)

                        val intentDestination = Intent(this@LoginActivity, HomeActivity::class.java)
                        startActivity(intentDestination)
                    }
                } else {
                    errorTextView.setText("Falied to create session")
                    errorTextView.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<SessionResponse>, t: Throwable) {
                errorTextView.setText("Error: ${t.message}")
                errorTextView.visibility = View.VISIBLE
            }
        })
    }

    private fun saveSessionId(sessionId: String) {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("session_id", sessionId)
        editor.apply()
    }
}