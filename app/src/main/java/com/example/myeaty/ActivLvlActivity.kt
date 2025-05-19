package com.example.myeaty

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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
        val buttonLow = findViewById<Button>(R.id.btn_low)
        val buttonNorm = findViewById<Button>(R.id.btn_norm)
        val buttonMid = findViewById<Button>(R.id.btn_mid)
        val buttonHigh = findViewById<Button>(R.id.btn_hight)
        val buttonBack = findViewById<Button>(R.id.btn_back1)

        val allButtons = listOf(buttonLow, buttonNorm, buttonMid, buttonHigh)

        fun selectButton(selectedButton: Button, level: Int) {
            selectedLevel = level

            // Установка прозрачности: выбранная — яркая, остальные — полупрозрачные
            allButtons.forEach { it.alpha = 0.5f }
            selectedButton.alpha = 1.0f
        }
        buttonBack.setOnClickListener {
            val intent = Intent(this, DateActivity::class.java)
            startActivity(intent)
        }

        // Обработчики нажатий
        buttonLow.setOnClickListener { selectButton(buttonLow, 1) }
        buttonNorm.setOnClickListener { selectButton(buttonNorm, 2) }
        buttonMid.setOnClickListener { selectButton(buttonMid, 3) }
        buttonHigh.setOnClickListener { selectButton(buttonHigh, 4) }
    }
}
