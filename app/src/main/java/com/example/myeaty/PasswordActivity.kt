package com.example.myeaty

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_password)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val passwordInput = findViewById<EditText>(R.id.edtTxt_Password)
        val buttonSave = findViewById<Button>(R.id.btn_count)

        // Открытие базы данных
        val dbPath = getDatabasePath("MyEaty.db").absolutePath
        SQLBridge.nativeOpenDatabase(dbPath)



        // Очистка подсказки при фокусе
        passwordInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && passwordInput.text.toString() == "...") {
                passwordInput.setText("")
            }
        }

        // Сохранение данных пользователя
        buttonSave.setOnClickListener {
            val password = passwordInput.text.toString()

            if (!password.matches(Regex("\\d+"))) {
                Toast.makeText(this, "Введите только цифры", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (UserData.age == null || UserData.weight == null || UserData.height == null ||
                UserData.goal == null || UserData.activityLevel == null
            ) {
                Toast.makeText(this, "Не все данные введены", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            UserData.password = password

            // Сохраняем в базу через C++
            SQLBridge.nativeSaveUserFullData(
                UserData.age!!,
                UserData.weight!!,
                UserData.height!!,
                UserData.goal!!,
                UserData.activityLevel!!,
                password
            )

            Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show()


        }
    }
}
