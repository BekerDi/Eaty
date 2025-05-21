package com.example.myeaty


object UserData {
    var name: String? = null
    var gender: Int? = null
    var age: Int? = null
    var weight: Int? = null
    var height: Int? = null
    var bodyType: String? = null
    var goal: Int? = null
    var activityLevel: Int? = null
    var password: String? = null

    fun clear() {
        name = null
        gender = null
         age = null
         weight = null
         height = null
         bodyType = null
         goal = null
         activityLevel = null
         password = null
    }
}