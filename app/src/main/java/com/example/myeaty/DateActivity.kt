package com.example.myeaty

import android.app.admin.TargetUser
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val dbPath = DatabaseHelper.copyDatabaseFromAssets(this, "MyEaty.db")
        SQLBridge.nativeOpenDatabase(dbPath)

        val editTextAge = findViewById<EditText>(R.id.ediTxt_Age)
        val editTextWeight = findViewById<EditText>(R.id.edtTxt_Weight)
        val editTextHeight = findViewById<EditText>(R.id.edtTxt_Height)

        val radioSkinny = findViewById<RadioButton>(R.id.rBtn_skinny)
        val radioNorm = findViewById<RadioButton>(R.id.rBtn_norm)
        val radioFat = findViewById<RadioButton>(R.id.rBtn_fat)

        val continueButton = findViewById<Button>(R.id.btn_next2)
        val buttonBack = findViewById<Button>(R.id.btn_back2)

        // Очистка "..." при фокусе
        listOf(editTextAge, editTextHeight, editTextWeight).forEach { editText ->
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && editText.text.toString() == "...") {
                    editText.setText("")
                }
            }
        }

        continueButton.setOnClickListener {
            val age = editTextAge.text.toString().toIntOrNull()
            val weight = editTextWeight.text.toString().toIntOrNull()
            val height = editTextHeight.text.toString().toIntOrNull()

            if (age == null || weight == null || height == null) {
                Toast.makeText(this, "Введите возраст, вес и рост корректно", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val goal = when {
                radioSkinny.isChecked -> 0
                radioNorm.isChecked -> 1
                radioFat.isChecked -> 2
                else -> null
            }

            if (goal == null) {
                Toast.makeText(this, "Выберите цель", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Сохраняем во временное хранилище
            UserData.age = age
            UserData.weight = weight
            UserData.height = height
            UserData.goal = goal

            // Переход к следующему экрану
            startActivity(Intent(this, ActivLvlActivity::class.java))
        }

        buttonBack.setOnClickListener {
            startActivity(Intent(this, WelcomeActivity::class.java))
        }
    }
}
