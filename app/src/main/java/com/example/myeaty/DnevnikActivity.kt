package com.example.myeaty

import android.os.Bundle
import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DnevnikActivity : AppCompatActivity() {

    private var userId: Int = -1
    private lateinit var txtEaten: TextView

    private var totalCalories = 0f
    private var totalProtein = 0f
    private var totalFat = 0f
    private var totalCarbs = 0f

    private lateinit var products: List<Product>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dnevnik)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userId = intent.getIntExtra("userId", -1)
        if (userId == -1) {
            Toast.makeText(this, "Ошибка входа: userId не получен", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val kbjuArray = SQLBridge.nativeGetKBJUForUser(userId)
        val txtNorm = findViewById<TextView>(R.id.txt_kbju_total)
        txtNorm.text = "К: ${kbjuArray[0].toInt()}  Б: ${kbjuArray[1].toInt()}  Ж: ${kbjuArray[2].toInt()}  У: ${kbjuArray[3].toInt()}"

        txtEaten = findViewById(R.id.txt_kbju_eaten)

        findViewById<Button>(R.id.btn_edit_profile).setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btn_add_custom_product).setOnClickListener {
            showAddCustomProductDialog()
        }

        products = loadProducts()

        findViewById<ImageButton>(R.id.btn_add_breakfast).setOnClickListener {
            showProductDialog("Завтрак", findViewById(R.id.breakfast_container))
        }
        findViewById<ImageButton>(R.id.btn_add_snack).setOnClickListener {
            showProductDialog("Перекус", findViewById(R.id.snack_container))
        }
        findViewById<ImageButton>(R.id.btn_add_lunch).setOnClickListener {
            showProductDialog("Обед", findViewById(R.id.lunch_container))
        }
        findViewById<ImageButton>(R.id.btn_add_afternoon).setOnClickListener {
            showProductDialog("Полдник", findViewById(R.id.afternoon_container))
        }
        findViewById<ImageButton>(R.id.btn_add_dinner).setOnClickListener {
            showProductDialog("Ужин", findViewById(R.id.dinner_container))
        }
    }

    private fun loadProducts(): List<Product> {
        return try {
            val result = SQLBridge.getAllProducts()
            Log.d("DnevnikDebug", "Получено продуктов: ${result.size}")
            result
        } catch (e: Exception) {
            Log.e("DnevnikDebug", "Ошибка при получении продуктов", e)
            Toast.makeText(this, "Ошибка при загрузке продуктов", Toast.LENGTH_SHORT).show()
            emptyList()
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
                products = loadProducts()
                Toast.makeText(this, "Продукт добавлен", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showProductDialog(mealType: String, container: LinearLayout) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null)
        val searchInput = dialogView.findViewById<AutoCompleteTextView>(R.id.input_product_search)
        val inputWeight = dialogView.findViewById<EditText>(R.id.input_weight)

        val productNames = products.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, productNames)
        searchInput.setAdapter(adapter)

        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s.toString().lowercase()
                val filteredNames = productNames.filter { it.lowercase().contains(query) }
                searchInput.setAdapter(
                    ArrayAdapter(this@DnevnikActivity, android.R.layout.simple_dropdown_item_1line, filteredNames)
                )
                searchInput.showDropDown()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        AlertDialog.Builder(this)
            .setTitle("Добавить продукт в \"$mealType\"")
            .setView(dialogView)
            .setPositiveButton("Добавить") { _, _ ->
                val selectedName = searchInput.text.toString().trim()
                val weightText = inputWeight.text.toString()
                val product = products.find { it.name.equals(selectedName, ignoreCase = true) }

                if (product != null && weightText.isNotBlank()) {
                    val weight = weightText.toFloat()
                    val cal = product.calories * weight / 100
                    val prot = product.protein * weight / 100
                    val fat = product.fat * weight / 100
                    val carb = product.carbs * weight / 100

                    totalCalories += cal
                    totalProtein += prot
                    totalFat += fat
                    totalCarbs += carb

                    txtEaten.text = "Съедено: К: ${totalCalories.toInt()} Б: ${totalProtein.toInt()} Ж: ${totalFat.toInt()} У: ${totalCarbs.toInt()}"

                    val productBlock = RelativeLayout(this).apply {
                        setBackgroundResource(R.drawable.bg_meal_input)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { setMargins(0, 8, 0, 8) }
                        setPadding(24, 24, 24, 24)
                    }

                    val productText = TextView(this).apply {
                        text = "${product.name} - ${weight}г\nК: ${cal.toInt()} Б: ${prot.toInt()} Ж: ${fat.toInt()} У: ${carb.toInt()}"
                        textSize = 14f
                        setTextColor(resources.getColor(android.R.color.black))
                    }

                    productBlock.addView(productText)
                    container.addView(productBlock)
                } else {
                    Toast.makeText(this, "Выберите продукт и введите вес", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        val kbjuArray = SQLBridge.nativeGetKBJUForUser(userId)
        val txtNorm = findViewById<TextView>(R.id.txt_kbju_total)
        txtNorm.text = "К: ${kbjuArray[0].toInt()}  Б: ${kbjuArray[1].toInt()}  Ж: ${kbjuArray[2].toInt()}  У: ${kbjuArray[3].toInt()}"
    }
}
