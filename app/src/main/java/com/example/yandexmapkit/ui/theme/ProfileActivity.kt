package com.example.yandexmapkit.ui.theme

import android.content.ClipData
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.ClipboardManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.yandexmapkit.R

class ProfileActivity : AppCompatActivity() {
    private lateinit var clipboardManager: ClipboardManager
    private lateinit var userId: TextView
    private lateinit var friendId: EditText
    private lateinit var button: Button
    private lateinit var clipData: ClipData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userId = findViewById(R.id.prf_userid)
        friendId = findViewById(R.id.prf_addfriend)
        button = findViewById(R.id.prf_button)

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val userIdString = intent.getStringExtra("USER_ID") ?: "unknown"
        userId.text = userIdString
        userId.setOnClickListener {
            val textToCopy = userId.text.toString()

            clipData = ClipData.newPlainText("Copied Text", textToCopy)

            clipboardManager.setPrimaryClip(clipData)

            Toast.makeText(this, "Скопировано в буфер обмена", Toast.LENGTH_SHORT).show()
        }
    }
}