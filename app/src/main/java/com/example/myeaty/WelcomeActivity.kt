package com.example.myeaty

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class WelcomeActivity : AppCompatActivity() {

    private lateinit var dbHelper: UserDatabaseHelper
    private var selectedGender: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Инициализация базы данных
        dbHelper = UserDatabaseHelper(this)

        // Поиск элементов интерфейса
        val buttonMale = findViewById<ImageButton>(R.id.imgBtn_M)
        val buttonFemale = findViewById<ImageButton>(R.id.imgBtn_F)
        val buttonSave = findViewById<Button>(R.id.btn_Next2)

        // Логика выбора пола
        buttonMale.setOnClickListener {
            selectedGender = 0
            buttonMale.alpha = 1.0f
            buttonFemale.alpha = 0.5f
        }

        buttonFemale.setOnClickListener {
            selectedGender = 1
            buttonFemale.alpha = 1.0f
            buttonMale.alpha = 0.5f
        }

        // Кнопка "далее" — сохранение данных
        buttonSave.setOnClickListener {
            if (selectedGender == null) {
                Toast.makeText(this, "Выберите пол", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

        }
    }
}
