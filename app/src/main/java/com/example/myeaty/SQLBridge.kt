package com.example.myeaty

import android.util.Log

object SQLBridge {

    init {
        System.loadLibrary("native-lib")
    }

    external fun nativeOpenDatabase(path: String): Boolean
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
    external fun nativeCloseDatabase()
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

    fun getAllProducts(): List<Product> {
        return nativeGetAllProducts().toList()
    }
}
