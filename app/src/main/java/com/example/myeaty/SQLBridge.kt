package com.example.myeaty

import android.util.Log

object SQLBridge {

    init {
        System.loadLibrary("native-lib")
    }

    // 🔧 Нативные методы
    external fun nativeOpenDatabase(path: String): Boolean
    external fun nativeCloseDatabase()
    external fun nativeSaveUserFullData(
        name: String,
        gender: Int,
        age: Int,
        weight: Int,
        height: Int,
        goal: Int,
        activityLevel: Int,
        password: String
    )
    external fun nativeCalculateNutrition(
        userId: Int,
        gender: Int,
        age: Int,
        weight: Int,
        height: Int,
        goal: Int,
        activityLevel: Int
    ): FloatArray

    external fun nativeGetLastUserId(): Int
    external fun nativeLoginUser(name: String, password: String): Int
    external fun nativeGetKBJUForUser(userId: Int): FloatArray
    external fun nativeInitProductDatabase()
    external fun nativeGetAllProducts(): Array<Products>

    external fun nativeGetUserProfile(userId: Int): UserProfile
    external fun nativeUpdateUserProfile(
        userId: Int,
        age: Int,
        weight: Int,
        height: Int,
        goal: Int,
        activityLevel: Int
    )

    // 🔧 Состояние базы
    private var isDbOpen = false

    fun openDatabaseWithLog(path: String) {
        Log.i("MyEatyDebug", "DB Path: $path")
        openDatabaseOnce(path)
    }

    fun openDatabaseOnce(path: String) {
        if (!isDbOpen) {
            isDbOpen = nativeOpenDatabase(path)
        }
    }

    fun closeDatabase() {
        if (isDbOpen) {
            nativeCloseDatabase()
            isDbOpen = false
        }
    }

    // 🔧 Сохранение + пересчёт КБЖУ
    fun saveUserAndCalculateKBJU(
        name: String,
        gender: Int,
        age: Int,
        weight: Int,
        height: Int,
        goal: Int,
        activityLevel: Int,
        password: String
    ): FloatArray {
        nativeSaveUserFullData(name, gender, age, weight, height, goal, activityLevel, password)
        val userId = getLastInsertedUserId()
        return nativeCalculateNutrition(userId, gender, age, weight, height, goal, activityLevel)
    }

    private fun getLastInsertedUserId(): Int {
        val userId = nativeGetLastUserId()
        Log.i("MyEatyDebug", "Получен ID последнего пользователя: $userId")
        return userId
    }

    // 🔧 Работа с продуктами
    fun getAllProducts(): List<Products> {
        return nativeGetAllProducts().toList()
    }

    // 🔧 Получение профиля
    fun getUserProfile(userId: Int): UserProfile {
        return nativeGetUserProfile(userId)
    }

    // 🔧 Обновление профиля без пересчёта (оставляем для совместимости)
    fun updateUserProfile(
        userId: Int,
        age: Int,
        weight: Int,
        height: Int,
        goal: Int,
        activityLevel: Int
    ) {
        nativeUpdateUserProfile(userId, age, weight, height, goal, activityLevel)
    }

    // 🔁 Обновление и пересчёт (если есть gender)
    fun updateUserProfileAndRecalculate(
        userId: Int,
        gender: Int,
        age: Int,
        weight: Int,
        height: Int,
        goal: Int,
        activityLevel: Int
    ): FloatArray {
        nativeUpdateUserProfile(userId, age, weight, height, goal, activityLevel)
        return nativeCalculateNutrition(userId, gender, age, weight, height, goal, activityLevel)
    }

    // 🔁 Обновление и пересчёт (автоопределение gender)
    fun updateUserProfileWithFetchAndRecalculate(
        userId: Int,
        age: Int,
        weight: Int,
        height: Int,
        goal: Int,
        activityLevel: Int
    ): FloatArray {
        val gender = getUserProfile(userId).gender
        return updateUserProfileAndRecalculate(userId, gender, age, weight, height, goal, activityLevel)
    }
}
