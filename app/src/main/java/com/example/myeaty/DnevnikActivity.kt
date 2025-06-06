package com.example.myeaty

import android.os.Bundle
import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myeaty.Product

class DnevnikActivity : AppCompatActivity() {

    private var userId: Int = -1

    private lateinit var txtEaten: TextView

    private var totalCalories = 0f
    private var totalProtein = 0f
    private var totalFat = 0f
    private var totalCarbs = 0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dnevnik)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Log.d("DnevnikDebug", "onCreate стартовал")

        // Получение userId из Intent
        userId = intent.getIntExtra("userId", -1)
        Log.d("DnevnikDebug", "userId из Intent = $userId")

        if (userId == -1) {
            Toast.makeText(this, "Ошибка входа: userId не получен", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val kbjuArray = SQLBridge.nativeGetKBJUForUser(userId)
        val normCalories = kbjuArray[0]
        val normProtein = kbjuArray[1]
        val normFat = kbjuArray[2]
        val normCarbs = kbjuArray[3]

        val txtNorm = findViewById<TextView>(R.id.txt_kbju_total)
        txtNorm.text = "К: ${normCalories.toInt()}  Б: ${normProtein.toInt()}  Ж: ${normFat.toInt()}  У: ${normCarbs.toInt()}"


        val products = try {
            val result = SQLBridge.getAllProducts()
            Log.d("DnevnikDebug", "Получено продуктов: ${result.size}")
            result
        } catch (e: Exception) {
            Log.e("DnevnikDebug", "Ошибка при получении продуктов", e)
            Toast.makeText(this, "Ошибка при загрузке продуктов", Toast.LENGTH_SHORT).show()
            emptyList()
        }

        txtEaten = findViewById(R.id.txt_kbju_eaten)

        val btnAddBreakfast = findViewById<ImageButton>(R.id.btn_add_breakfast)
        val btnAddSnack = findViewById<ImageButton>(R.id.btn_add_snack)
        val btnAddLunch = findViewById<ImageButton>(R.id.btn_add_lunch)
        val btnAddAfternoon = findViewById<ImageButton>(R.id.btn_add_afternoon)
        val btnAddDinner = findViewById<ImageButton>(R.id.btn_add_dinner)

        val breakfastContainer = findViewById<LinearLayout>(R.id.breakfast_container)
        val snackContainer = findViewById<LinearLayout>(R.id.snack_container)
        val lunchContainer = findViewById<LinearLayout>(R.id.lunch_container)
        val afternoonContainer = findViewById<LinearLayout>(R.id.afternoon_container)
        val dinnerContainer = findViewById<LinearLayout>(R.id.dinner_container)

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

                    //тут у нас про съеденное
                    totalCalories += cal
                    totalProtein += prot
                    totalFat += fat
                    totalCarbs += carb

                    txtEaten.text = "Съедено: К: ${totalCalories.toInt()} Б: ${totalProtein.toInt()} Ж: ${totalFat.toInt()} У: ${totalCarbs.toInt()}"




                    val productBlock = RelativeLayout(this).apply {
                        setBackgroundResource(R.drawable.bg_meal_input)
                        val layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 8, 0, 8)
                        }
                        this.layoutParams = layoutParams
                        setPadding(24, 24, 24, 24)
                    }

                    val productText = TextView(this).apply {
                        text = "${product.name} - ${weight}г\nК: ${cal.toInt()} Б: ${prot.toInt()} Ж: ${fat.toInt()} У: ${carb.toInt()}"
                        textSize = 14f
                        setTextColor(resources.getColor(android.R.color.black))
                    }

                    productBlock.addView(productText)
                    container.addView(productBlock)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}
