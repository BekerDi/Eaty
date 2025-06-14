package com.example.myeaty

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class RecipeActivity : AppCompatActivity() {

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe)

        userId = intent.getIntExtra("userId", -1)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_recipes

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
                    startActivity(
                        Intent(this, EditProfileActivity::class.java)
                            .putExtra("userId", userId)
                    )
                    finish()
                    true
                }
                else -> false
            }
        }

        // ТВОЯ логика кнопок рецептов
        val btnOatmeal: Button = findViewById(R.id.btnOatmeal)
        val btnSyrniki: Button = findViewById(R.id.btnSyrniki)
        val btnBrownie: Button = findViewById(R.id.btnBrownie)

        btnOatmeal.setOnClickListener {
            Log.d("RecipeActivity", "Нажата кнопка Oatmeal — запускаем RecipeDetailActivity")
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", "oatmeal")
            startActivity(intent)
        }

        btnSyrniki.setOnClickListener {
            Log.d("RecipeActivity", "Нажата кнопка Syrniki — запускаем RecipeDetailActivity")
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", "syrniki")
            startActivity(intent)
        }

        btnBrownie.setOnClickListener {
            Log.d("RecipeActivity", "Нажата кнопка Brownie — запускаем RecipeDetailActivity")
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", "brownie")
            startActivity(intent)
        }
    }

    private fun showAddCustomProductDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_custom_product, null)
        val inputName = dialogView.findViewById<EditText>(R.id.input_custom_name)
        val inputCalories = dialogView.findViewById<EditText>(R.id.input_custom_calories)
        val inputProtein = dialogView.findViewById<EditText>(R.id.input_custom_protein)
        val inputFat = dialogView.findViewById<EditText>(R.id.input_custom_fat)
        val inputCarbs = dialogView.findViewById<EditText>(R.id.input_custom_carbs)

        AlertDialog.Builder(this)
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
