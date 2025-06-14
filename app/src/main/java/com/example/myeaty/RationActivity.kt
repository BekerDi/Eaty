package com.example.myeaty

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class RationActivity : AppCompatActivity() {

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ration_activity)

        userId = intent.getIntExtra("userId", -1)

        // BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_rations

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_rations -> {
                    true
                }
                R.id.nav_recipes -> {
                    startActivity(Intent(this, RecipeActivity::class.java).putExtra("userId", userId))
                    finish()
                    true
                }
                R.id.nav_diary -> {
                    startActivity(Intent(this, DnevnikActivity::class.java).putExtra("userId", userId))
                    finish()
                    true
                }
                R.id.nav_create -> {
                    showAddCustomProductDialog()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, EditProfileActivity::class.java).putExtra("userId", userId))
                    finish()
                    true
                }
                else -> false
            }
        }

        // ======= Обработка нажатия на кнопки рационов =======

        val btnRation1 = findViewById<LinearLayout>(R.id.btn_ration1)
        val btnRation2 = findViewById<LinearLayout>(R.id.btn_ration2)
        val btnRation3 = findViewById<LinearLayout>(R.id.btn_ration3)

        btnRation1.setOnClickListener {
            showRation("high_protein")
        }

        btnRation2.setOnClickListener {
            showRation("calorie_deficit")
        }

        btnRation3.setOnClickListener {
            showRation("ration3")
        }
    }

    private fun showRation(rationId: String) {
        val title = NativeLib.getRationTitle(rationId)
        val shortDesc = NativeLib.getRationShortDescription(rationId)
        val fullPlan = NativeLib.getRationFullPlan(rationId)

        Log.d("RationActivity", "== Рацион $rationId ==")
        Log.d("RationActivity", "Title: $title")
        Log.d("RationActivity", "Short Desc: $shortDesc")
        Log.d("RationActivity", "Full Plan: $fullPlan")

        val intent = Intent(this, RationDetailActivity::class.java)
        intent.putExtra("RATION_ID", rationId)
        startActivity(intent)
    }

    private fun showAddCustomProductDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_custom_product, null)
        val inputName = dialogView.findViewById<android.widget.EditText>(R.id.input_custom_name)
        val inputCalories = dialogView.findViewById<android.widget.EditText>(R.id.input_custom_calories)
        val inputProtein = dialogView.findViewById<android.widget.EditText>(R.id.input_custom_protein)
        val inputFat = dialogView.findViewById<android.widget.EditText>(R.id.input_custom_fat)
        val inputCarbs = dialogView.findViewById<android.widget.EditText>(R.id.input_custom_carbs)

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
                    android.widget.Toast.makeText(this, "Заполните все поля корректно", android.widget.Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                SQLBridge.insertCustomProduct(name, cal, prot, fat, carb)
                android.widget.Toast.makeText(this, "Продукт добавлен", android.widget.Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}
