package com.example.yandexmapkit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import kotlin.text.isNotEmpty

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

        val loginEditText: EditText = findViewById(R.id.login)
        val loginButton: Button = findViewById(R.id.button)

        loginButton.setOnClickListener {
            val login = loginEditText.text.toString().trim()
            if (login.isNotEmpty()) {
                val sharedPref = getSharedPreferences("MyApp", MODE_PRIVATE)
                sharedPref.edit().putString("login", login).apply()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Введите логин", Toast.LENGTH_SHORT).show()
            }
        }
    }
}