package com.example.myeaty

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

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

        // === Добавляем обработку нижней панели навигации ===
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_profile

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_rations -> {
                    startActivity(
                        Intent(this, RationActivity::class.java)
                            .putExtra("userId", userId)
                    )
                    finish()
                    true
                }
                R.id.nav_recipes -> {
                    startActivity(
                        Intent(this, RecipeActivity::class.java)
                            .putExtra("userId", userId)
                    )
                    finish()
                    true
                }
                R.id.nav_diary -> {
                    startActivity(
                        Intent(this, DnevnikActivity::class.java)
                            .putExtra("userId", userId)
                    )
                    finish()
                    true
                }
                R.id.nav_create -> {
                    showAddCustomProductDialog()
                    true
                }

                R.id.nav_profile -> {
                    // остаемся на текущем экране
                    true
                }
                else -> false
            }
        }
    }
    private fun showAddCustomProductDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_custom_product, null)
        val inputName = dialogView.findViewById<EditText>(R.id.input_custom_name)
        val inputCalories = dialogView.findViewById<EditText>(R.id.input_custom_calories)
        val inputProtein = dialogView.findViewById<EditText>(R.id.input_custom_protein)
        val inputFat = dialogView.findViewById<EditText>(R.id.input_custom_fat)
        val inputCarbs = dialogView.findViewById<EditText>(R.id.input_custom_carbs)

        android.app.AlertDialog.Builder(this)
            .setTitle("Добавить продукт")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val name = inputName.text.toString()
                val cal = inputCalories.text.toString().toFloatOrNull()
                val prot = inputProtein.text.toString().toFloatOrNull()
                val fat = inputFat.text.toString().toFloatOrNull()
                val carb = inputCarbs.text.toString().toFloatOrNull()

                if (name.isBlank() || cal == null || prot == null || fat == null || carb == null) {
                    Toast.makeText(this, "Заполните все поля корректно", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                SQLBridge.insertCustomProduct(name, cal, prot, fat, carb)
                Toast.makeText(this, "Продукт добавлен", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

}
