package com.example.myeaty

import android.content.Intent
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

        val dbPath = getDatabasePath("MyEaty.db").absolutePath
        SQLBridge.nativeOpenDatabase(dbPath)

        passwordInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && passwordInput.text.toString() == "...") {
                passwordInput.setText("")
            }
        }

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

            // Сохраняем пользователя и рассчитываем КБЖУ
            val result = SQLBridge.saveUserAndCalculateKBJU(
                UserData.name ?: "",
                UserData.gender!!,
                UserData.age!!,
                UserData.weight!!,
                UserData.height!!,
                UserData.goal!!,
                UserData.activityLevel!!,
                password
            )

            val userId = SQLBridge.nativeLoginUser(UserData.name ?: "", password)

            if (userId > 0) {
                val calories = result[0]
                val protein = result[1]
                val fat = result[2]
                val carbs = result[3]

                val message = "КБЖУ рассчитаны:\nКкал: $calories\nБелки: $protein г\nЖиры: $fat г\nУглеводы: $carbs г"
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                Toast.makeText(this, "Регистрация успешна! Входим в дневник...", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, DnevnikActivity::class.java).apply {
                    putExtra("userId", userId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Ошибка: не удалось войти после регистрации", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SQLBridge.nativeCloseDatabase()
    }
}
