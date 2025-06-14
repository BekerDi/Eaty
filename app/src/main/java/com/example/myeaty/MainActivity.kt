package com.example.myeaty

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object {
        init {
            System.loadLibrary("myeaty")
        }
    }

    // 2) Объявляем внешний (JNI) метод, который мы реализовали в native-lib.cpp.
    //    Сигнатура должна соответствовать именно:
    //      Java_com_example_myeaty_MainActivity_stringFromJNI
    //    (имя нашего класса + пакет).
    external fun stringFromJNI(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dbPath = DatabaseHelper.copyDatabaseFromAssets(this, "MyEaty.db")
        SQLBridge.nativeOpenDatabase(dbPath)
        SQLBridge.nativeInitProductDatabase()
        val dbFile = File(dbPath)

        if (dbFile.exists()) {
            Toast.makeText(this, "База данных MyEaty существует", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "База данных MyEaty не найдена", Toast.LENGTH_SHORT).show()
        }

        setContentView(R.layout.activity_main)

        val nameEditText = findViewById<EditText>(R.id.edt_Name)
        val continueButton = findViewById<Button>(R.id.btn_next)
        val loginButton = findViewById<Button>(R.id.btn_login)

        // Фильтр — разрешены только английские буквы и цифры
        val inputFilter = object : InputFilter {
            val regex = Regex("^[a-zA-Z0-9]+$")

            override fun filter(
                source: CharSequence,
                start: Int,
                end: Int,
                dest: Spanned,
                dstart: Int,
                dend: Int
            ): CharSequence? {
                val input = source.subSequence(start, end).toString()
                return if (input.matches(regex)) null else ""
            }
        }
        nameEditText.filters = arrayOf(inputFilter)

        nameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && nameEditText.text.toString() == "...") {
                nameEditText.setText("")
            }
        }

        continueButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()

            when {
                name.isEmpty() -> {
                    Toast.makeText(this, "Пожалуйста, введите имя", Toast.LENGTH_SHORT).show()
                }
                !name.matches(Regex("^[a-zA-Z0-9]+$")) -> {
                    Toast.makeText(this, "Имя может содержать только английские буквы и цифры", Toast.LENGTH_SHORT).show()
                }
                SQLBridge.nativeCheckUserExists(name) -> {
                    Toast.makeText(this, "Имя уже занято. Выберите другое.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    UserData.name = name
                    val intent = Intent(this, WelcomeActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
