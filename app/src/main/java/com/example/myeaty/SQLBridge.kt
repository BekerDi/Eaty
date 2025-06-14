package com.example.myeaty

import android.util.Log


object SQLBridge {

    init {
        System.loadLibrary("myeaty")
    }
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
    external fun nativeGetAllProducts(): Array<Product>
    external fun nativeGetUserProfile(userId: Int): UserProfile
    external fun nativeUpdateUserProfile(
        userId: Int,
        age: Int,
        weight: Int,
        height: Int,
        goal: Int,
        activityLevel: Int
    )
    external fun nativeCheckUserExists(name: String): Boolean

    private var isDbOpen = false

    fun openDatabaseWithLog(path: String) {
        Log.i("MyEatyDebug", "Открытие базы: $path")
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
        Log.i("MyEatyDebug", "Последний ID пользователя: $userId")
        return userId
    }

    fun getAllProducts(): List<Product> {
        return nativeGetAllProducts().toList()
    }

    fun getUserProfile(userId: Int): UserProfile {
        return nativeGetUserProfile(userId)
    }

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
        val result = nativeCalculateNutrition(userId, gender, age, weight, height, goal, activityLevel)
        Log.i("MyEatyDebug", "Пересчитаны КБЖУ: К=${result[0]}, Б=${result[1]}, Ж=${result[2]}, У=${result[3]}")
        return result
    }
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
    external fun nativeInsertProduct(name: String, calories: Float, protein: Float, fat: Float, carbs: Float)

    fun insertCustomProduct(name: String, calories: Float, protein: Float, fat: Float, carbs: Float) {
        nativeInsertProduct(name, calories, protein, fat, carbs)
    }
    external fun nativeClearTables()


}
