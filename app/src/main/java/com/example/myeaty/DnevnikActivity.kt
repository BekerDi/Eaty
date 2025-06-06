package com.example.myeaty

import android.os.Bundle
import android.app.AlertDialog
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DnevnikActivity : AppCompatActivity() {

    private var userId: Int = -1 // подставь свой userId при необходимости

    // Суммы КБЖУ
    private var totalCalories = 0f
    private var totalProtein = 0f
    private var totalFat = 0f
    private var totalCarbs = 0f

    private lateinit var txtEaten: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dnevnik)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val products = SQLBridge.getAllProducts()

        // Найти TextView для отображения съеденного КБЖУ
        txtEaten = findViewById(R.id.txt_kbju_eaten)

        // Кнопки
        val btnAddBreakfast = findViewById<ImageButton>(R.id.btn_add_breakfast)
        val btnAddSnack = findViewById<ImageButton>(R.id.btn_add_snack)
        val btnAddLunch = findViewById<ImageButton>(R.id.btn_add_lunch)
        val btnAddAfternoon = findViewById<ImageButton>(R.id.btn_add_afternoon)
        val btnAddDinner = findViewById<ImageButton>(R.id.btn_add_dinner)

        // Контейнеры
        val breakfastContainer = findViewById<LinearLayout>(R.id.breakfast_container)
        val snackContainer = findViewById<LinearLayout>(R.id.snack_container)
        val lunchContainer = findViewById<LinearLayout>(R.id.lunch_container)
        val afternoonContainer = findViewById<LinearLayout>(R.id.afternoon_container)
        val dinnerContainer = findViewById<LinearLayout>(R.id.dinner_container)

        // Назначаем обработчики
        btnAddBreakfast.setOnClickListener {
            showProductDialog("Завтрак", products, breakfastContainer)
        }

        btnAddSnack.setOnClickListener {
            showProductDialog("Перекус", products, snackContainer)
        }

        btnAddLunch.setOnClickListener {
            showProductDialog("Обед", products, lunchContainer)
        }

        btnAddAfternoon.setOnClickListener {
            showProductDialog("Полдник", products, afternoonContainer)
        }

        btnAddDinner.setOnClickListener {
            showProductDialog("Ужин", products, dinnerContainer)
        }
    }

    private fun showProductDialog(
        mealType: String,
        products: List<Product>,
        container: LinearLayout
    ) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinner_products)
        val inputWeight = dialogView.findViewById<EditText>(R.id.input_weight)

        val productNames = products.map { it.name }
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, productNames)

        AlertDialog.Builder(this)
            .setTitle("Добавить продукт в \"$mealType\"")
            .setView(dialogView)
            .setPositiveButton("Добавить") { _, _ ->
                val selectedIndex = spinner.selectedItemPosition
                val weightText = inputWeight.text.toString()

                if (selectedIndex >= 0 && weightText.isNotBlank()) {
                    val product = products[selectedIndex]
                    val weight = weightText.toFloat()

                    val cal = product.caloriesPer100g * weight / 100
                    val prot = product.proteinPer100g * weight / 100
                    val fat = product.fatPer100g * weight / 100
                    val carb = product.carbPer100g * weight / 100

                    // Обновляем итоговые значения
                    totalCalories += cal
                    totalProtein += prot
                    totalFat += fat
                    totalCarbs += carb

                    // Обновляем текст сверху
                    txtEaten.text = "Съедено: К: ${totalCalories.toInt()}  Б: ${totalProtein.toInt()}  Ж: ${totalFat.toInt()}  У: ${totalCarbs.toInt()}"

                    // Отображение в контейнере
                    val textView = TextView(this)
                    textView.text = "${product.name} - ${weight}г (К: ${cal.toInt()} Б: ${prot.toInt()} Ж: ${fat.toInt()} У: ${carb.toInt()})"
                    container.addView(textView)

                    // Можно добавить сохранение в БД
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}
