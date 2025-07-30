package com.example.attendancemonitoringsystem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class LoginScreen : AppCompatActivity() {

    // Define the base URL for your PHP backend.
    // IMPORTANT: Change this to your actual server address.
    // For Android Emulator, '10.0.2.2' refers to your host machine's localhost.
    // Ensure your PHP server is running and accessible.
    private val BASE_URL = "[http://10.0.2.2/android_auth/](http://10.0.2.2/android_auth/)" // Example: http://your_ip_address/your_php_folder/

    // Initialize OkHttpClient instance (can be a singleton for better performance)
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen) // Reusing the same layout as Volley example

        // Initialize UI elements
        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        val textViewRegister = findViewById<TextView>(R.id.textViewRegister)

        // Set OnClickListener for the Login button
        buttonLogin.setOnClickListener {
            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            // Basic input validation
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Call the login API using a Coroutine for network operation
            loginUser(username, password)
        }

        // Set OnClickListener for the Register text (to navigate to RegisterActivityOkHttp)
        textViewRegister.setOnClickListener {
            val intent = Intent(this, RegisterScreen::class.java)
            startActivity(intent)
        }
    }

    /**
     * Sends a login request to the PHP backend using OkHttp.
     * Network operations should be performed on a background thread.
     * We use Kotlin Coroutines here for asynchronous execution.
     * @param username The username entered by the user.
     * @param password The password entered by the user.
     */
    private fun loginUser(username: String, password: String) {
        // Launch a coroutine in the IO dispatcher for network operations
        CoroutineScope(Dispatchers.IO).launch {
            val url = BASE_URL + "login.php" // Construct the full URL for the login endpoint

            // Create a JSON object for the request body
            val jsonBody = JSONObject().apply {
                put("username", username)
                put("password", password)
            }.toString()

            // Define the media type for JSON
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

            // Create the request body
            val requestBody = jsonBody.toRequestBody(mediaType)

            // Build the OkHttp request
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            try {
                // Execute the request and get the response
                val response = client.newCall(request).execute()

                // Check if the response was successful (HTTP 2xx)
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("LoginActivityOkHttp", "Login Response: $responseBody")

                    // Parse the JSON response on the main thread
                    CoroutineScope(Dispatchers.Main).launch {
                        if (responseBody != null) {
                            val jsonResponse = JSONObject(responseBody)
                            val status = jsonResponse.optString("status")
                            val message = jsonResponse.optString("message")

                            if (status == "success") {
                                // Login successful
                                Toast.makeText(this@LoginScreen, message, Toast.LENGTH_LONG).show()
                                // You can navigate to another activity here, e.g., a DashboardActivity
                                // val intent = Intent(this@LoginActivityOkHttp, DashboardActivity::class.java)
                                // startActivity(intent)
                                // finish() // Close LoginActivityOkHttp
                            } else {
                                // Login failed (e.g., invalid credentials)
                                Toast.makeText(this@LoginScreen, "Login Failed: $message", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this@LoginScreen, "Empty response from server", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    // Handle unsuccessful HTTP responses (e.g., 404, 500)
                    Log.e("LoginActivityOkHttp", "Login HTTP Error: ${response.code} - ${response.message}")
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@LoginScreen, "Server error: ${response.code}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: IOException) {
                // Handle network errors (e.g., no internet connection, host unreachable)
                Log.e("LoginActivityOkHttp", "Login Network Error: ${e.message}", e)
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(this@LoginScreen, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                // Handle other potential exceptions (e.g., JSON parsing errors)
                Log.e("LoginActivityOkHttp", "Login Error: ${e.message}", e)
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(this@LoginScreen, "An unexpected error occurred: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}