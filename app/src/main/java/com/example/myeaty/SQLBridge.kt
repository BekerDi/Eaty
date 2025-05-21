package com.example.myeaty

object SQLBridge {

    init {
        System.loadLibrary("native-lib")
    }


        external fun nativeOpenDatabase(path: String)

        external fun nativePrintAllUsers() //проверяем запись данных
        external fun nativeSaveUserFullData(
            age: Int,
            weight: Int,
            height: Int,
            goal: Int,
            activityLevel: Int,
            password: String
        )
    external fun nativeCloseDatabase()


}




