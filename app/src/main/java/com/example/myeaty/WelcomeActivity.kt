package com.example.myeaty

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    private var selectedGender: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val buttonMale = findViewById<ImageButton>(R.id.imgBtn_M)
        val buttonFemale = findViewById<ImageButton>(R.id.imgBtn_F)
        val buttonSave = findViewById<Button>(R.id.btn_Next2)
        val buttonBack = findViewById<Button>(R.id.btn_back)

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

        buttonSave.setOnClickListener {
            if (selectedGender == null) {
                Toast.makeText(this, "Выберите пол", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            UserData.gender = selectedGender // сохраняем пол

            val intent = Intent(this, DateActivity::class.java)
            startActivity(intent)
        }

        buttonBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
