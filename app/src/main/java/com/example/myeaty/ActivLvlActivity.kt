package com.example.myeaty

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ActivLvlActivity : AppCompatActivity() {

    private var selectedLevel: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_activ_lvl)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Кнопки уровней активности
        val buttonNext = findViewById<Button>(R.id.btn_next3)
        val buttonBack = findViewById<Button>(R.id.btn_back1)

        val buttonLow = findViewById<Button>(R.id.btn_low)
        val buttonNorm = findViewById<Button>(R.id.btn_norm)
        val buttonMid = findViewById<Button>(R.id.btn_mid)
        val buttonHigh = findViewById<Button>(R.id.btn_hight)

        val allButtons = listOf(buttonLow, buttonNorm, buttonMid, buttonHigh)

        fun selectButton(selectedButton: Button, level: Int) {
            selectedLevel = level
            allButtons.forEach { it.alpha = 0.5f }
            selectedButton.alpha = 1.0f
        }

        buttonLow.setOnClickListener { selectButton(buttonLow, 1) }
        buttonNorm.setOnClickListener { selectButton(buttonNorm, 2) }
        buttonMid.setOnClickListener { selectButton(buttonMid, 3) }
        buttonHigh.setOnClickListener { selectButton(buttonHigh, 4) }

        buttonNext.setOnClickListener {
            if (selectedLevel == null) {
                Toast.makeText(this, "Пожалуйста, выберите уровень активности", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Сохраняем в UserData
            UserData.activityLevel = selectedLevel!!

            // Переход к следующему экрану
            val intent = Intent(this, PasswordActivity::class.java) // Заменить на ваш экран
            startActivity(intent)
        }

        buttonBack.setOnClickListener {
            val intent = Intent(this, DateActivity::class.java)
            startActivity(intent)
        }
    }
}
