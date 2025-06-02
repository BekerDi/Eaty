package com.example.myeaty

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets


        }
        val loginEditText = findViewById<EditText>(R.id.edt_login)
        val passwordEditText = findViewById<EditText>(R.id.edt_password)
        val entrButton = findViewById<Button>(R.id.btn_enter)


        val dbPath = getDatabasePath("MyEaty.db").absolutePath
        SQLBridge.openDatabaseWithLog(dbPath)

        entrButton.setOnClickListener {
            val name = loginEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (name.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Введите имя и пароль", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = SQLBridge.nativeLoginUser(name, password)
            if (userId > 0) {
                Toast.makeText(this, "Вход успешен. ID: $userId", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DnevnikActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Неверное имя или пароль", Toast.LENGTH_SHORT).show()
            }
        }




    }
}