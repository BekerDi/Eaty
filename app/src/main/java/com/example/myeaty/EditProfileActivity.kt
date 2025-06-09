package com.example.myeaty

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class EditProfileActivity : AppCompatActivity() {

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        userId = intent.getIntExtra("userId", -1)
        if (userId == -1) {
            Toast.makeText(this, "Ошибка: нет ID пользователя", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val inputAge = findViewById<EditText>(R.id.edit_age)
        val inputWeight = findViewById<EditText>(R.id.edit_weight)
        val inputHeight = findViewById<EditText>(R.id.edit_height)
        val spinnerActivity = findViewById<Spinner>(R.id.spinner_activity)
        val radioGroup = findViewById<RadioGroup>(R.id.radio_goal_group)
        val btnSave = findViewById<Button>(R.id.btn_save)
        val btnCancel = findViewById<Button>(R.id.btn_cancel)
        val txtUserName = findViewById<TextView>(R.id.txt_user_name)

        // Активность — наполняем спиннер
        ArrayAdapter.createFromResource(
            this,
            R.array.activity_levels_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerActivity.adapter = adapter
        }

        // Получаем профиль
        val profile = SQLBridge.getUserProfile(userId)

        if (profile == null) {
            Toast.makeText(this, "Ошибка: профиль не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Заполняем поля профиля
        txtUserName.text = "Имя: ${profile.name}"
        inputAge.setText(profile.age.toString())
        inputWeight.setText(profile.weight.toString())
        inputHeight.setText(profile.height.toString())
        spinnerActivity.setSelection(profile.activityLevel - 1)

        when (profile.goal) {
            0 -> radioGroup.check(R.id.radio_lose)
            1 -> radioGroup.check(R.id.radio_keep)
            2 -> radioGroup.check(R.id.radio_gain)
        }

        btnSave.setOnClickListener {
            val age = inputAge.text.toString().toIntOrNull()
            val weight = inputWeight.text.toString().toIntOrNull()
            val height = inputHeight.text.toString().toIntOrNull()
            val activity = spinnerActivity.selectedItemPosition + 1
            val goal = when (radioGroup.checkedRadioButtonId) {
                R.id.radio_lose -> 0
                R.id.radio_keep -> 1
                R.id.radio_gain -> 2
                else -> -1
            }

            if (age == null || weight == null || height == null || goal == -1) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            SQLBridge.updateUserProfileWithFetchAndRecalculate(
                userId,
                age,
                weight,
                height,
                goal,
                activity
            )


            Toast.makeText(this, "Профиль обновлён", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }
}
