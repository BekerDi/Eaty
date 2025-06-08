package com.example.myeaty

import androidx.annotation.Keep

@Keep
data class Products(
    val id: Int,
    val name: String,
    val caloriesPer100g: Float,
    val proteinPer100g: Float,
    val fatPer100g: Float,
    val carbPer100g: Float
)
