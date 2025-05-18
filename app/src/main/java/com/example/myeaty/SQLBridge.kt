package com.example.myeaty

object SQLBridge {

    init {
        System.loadLibrary("native-lib")
    }

    external fun nativeOpenDatabase(path: String)
    external fun saveGenderNative(name: String, gender: Int)
}
