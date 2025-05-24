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
}
