package com.example.myeaty

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dbPath = DatabaseHelper.copyDatabaseFromAssets(this, "MyEaty.db")
        SQLBridge.nativeOpenDatabase(dbPath)  // вызываем native функцию через мост

        setContentView(R.layout.activity_main)

        val nameEditText = findViewById<EditText>(R.id.edt_Name)
        val continueButton = findViewById<Button>(R.id.btn_next)
        val loginButton = findViewById<Button>(R.id.btn_login)

        nameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && nameEditText.text.toString() == "...") {
                nameEditText.setText("")
            }
        }

        continueButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            if (name.isNotEmpty()) {
                val intent = Intent(this, WelcomeActivity::class.java)
                intent.putExtra("USER_NAME", name)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Пожалуйста, введите имя", Toast.LENGTH_SHORT).show()
            }
        }

        loginButton.setOnClickListener {
            Toast.makeText(this, "Переход не реализован", Toast.LENGTH_SHORT).show()
        }
    }
}
