package com.example.myeaty

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
        val radioButtonSkinny = findViewById<RadioButton>(R.id.rBtn_skinny)
        val radioButtonFat = findViewById<RadioButton>(R.id.rBtn_fat)
        val radioButtonNorm = findViewById<RadioButton>(R.id.rBtn_norm)
        val continueButton = findViewById<Button>(R.id.btn_next2)
        val buttonBack = findViewById<Button>(R.id.btn_back2)

        // Очистка "..." при фокусе
        editTextAge.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && editTextAge.text.toString() == "...") {
                editTextAge.setText("")
            }
        }

        editTextHeight.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && editTextHeight.text.toString() == "...") {
                editTextHeight.setText("")
            }
        }

        editTextWeight.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && editTextWeight.text.toString() == "...") {
                editTextWeight.setText("")
            }
        }

        // Только при клике
        continueButton.setOnClickListener {
            val intent = Intent(this, ActivLvlActivity::class.java)
            startActivity(intent)
        }

        buttonBack.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }

    }
}
