package com.example.myeaty

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        val loginEditText    = findViewById<EditText>(R.id.edt_login)
        val passwordEditText = findViewById<EditText>(R.id.edt_password)
        val enterButton      = findViewById<Button>(R.id.btn_enter)

        // Подключаем базу данных
        val dbPath = getDatabasePath("MyEaty.db").absolutePath
        SQLBridge.openDatabaseWithLog(dbPath)

        enterButton.setOnClickListener {
            val name     = loginEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (name.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Введите имя и пароль", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = SQLBridge.nativeLoginUser(name, password)
            Log.d("LoginDebug", "userId = $userId")

            if (userId > 0) {
                Toast.makeText(this, "Вход успешен. ID: $userId", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, DnevnikActivity::class.java).apply {
                    putExtra("userId", userId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

                Log.d("LoginDebug", "Запускаем DnevnikActivity")
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Неверное имя или пароль", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
