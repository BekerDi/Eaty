package com.example.myeaty
import android.widget.ImageButton

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecipeDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Указываем XML макет для экрана деталей
        setContentView(R.layout.activity_recipe_detail)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        // Лог - старт Activity
        Log.d("RecipeDetailActivity", "onCreate стартовал")

        // 1) Извлекаем recipeId из Intent
        val recipeId = intent.getStringExtra("RECIPE_ID") ?: "oatmeal"
        Log.d("RecipeDetailActivity", "Получен recipeId = $recipeId")

        // 2) Находим View-элементы
        val ivRecipeImage: ImageView = findViewById(R.id.ivRecipeImage)
        val tvRecipeTitle: TextView = findViewById(R.id.tvRecipeTitle)
        val tvRecipeFullText: TextView = findViewById(R.id.tvRecipeFullText)

        // 3) Запрашиваем данные из нативной библиотеки
        val titleFromCpp: String = NativeLib.getRecipeTitle(recipeId)
        val fullTextFromCpp: String = NativeLib.getRecipeFullText(recipeId)
        val imageNameFromCpp: String = NativeLib.getRecipeImageName(recipeId)

        // Логи по полученным данным
        Log.d("RecipeDetailActivity", "titleFromCpp = $titleFromCpp")
        Log.d("RecipeDetailActivity", "fullTextFromCpp = ${fullTextFromCpp.take(50)}...") // только первые 50 символов
        Log.d("RecipeDetailActivity", "imageNameFromCpp = $imageNameFromCpp")

        // 4) Устанавливаем название
        tvRecipeTitle.text = titleFromCpp

        // 5) Устанавливаем длинный текст
        tvRecipeFullText.text = fullTextFromCpp

        // 6) Находим идентификатор drawable по имени (возвращаемому из C++)
        val imageResId = resources.getIdentifier(imageNameFromCpp, "drawable", packageName)
        if (imageResId != 0) {
            ivRecipeImage.setImageResource(imageResId)
            Log.d("RecipeDetailActivity", "Установлено изображение $imageNameFromCpp (resId = $imageResId)")
        } else {
            Log.w("RecipeDetailActivity", "Не найдено изображение $imageNameFromCpp — устанавливаем заглушку")
            val defaultResId = resources.getIdentifier("default_recipe", "drawable", packageName)
            if (defaultResId != 0) {
                ivRecipeImage.setImageResource(defaultResId)
            }
        }
    }
}
