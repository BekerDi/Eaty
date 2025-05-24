package com.example.myeaty

import android.util.Log

object SQLBridge {

    init {
        System.loadLibrary("native-lib")
    }

    external fun nativeOpenDatabase(path: String): Boolean  // исправлено: теперь возвращает Boolean
    external fun nativeSaveUserFullData(
        name: String,
        gender: Int ,
        age: Int,
        weight: Int,
        height: Int,
        goal: Int,
        activityLevel: Int,
        password: String
    )
    external fun nativeCloseDatabase()

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
    external fun nativeCalculateNutrition(
        userId: Int,
        gender: Int,
        age: Int,
        weight: Int,
        height: Int,
        goal: Int,
        activityLevel: Int
    ): FloatArray
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
        // 1. Сохраняем пользователя
        nativeSaveUserFullData(name, gender, age, weight, height, goal, activityLevel, password)

        // 2. Получаем последний ID пользователя
         val userId = getLastInsertedUserId() // ← эту функцию нужно реализовать

        // 3. Рассчитываем и сохраняем KBJU
        return nativeCalculateNutrition(userId, gender, age, weight, height, goal, activityLevel)

    }
    private fun getLastInsertedUserId(): Int {
        val userId = nativeGetLastUserId()
        Log.i("MyEatyDebug", "Получен ID последнего пользователя: $userId")
        return userId
    }

    external fun nativeGetLastUserId(): Int

}
