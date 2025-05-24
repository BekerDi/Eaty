package com.example.myeaty


class KBJU {
    object KBJUManager {
        fun saveUserAndCalculate(): Boolean {
            SQLBridge.nativeSaveUserFullData(
                UserData.name ?: "",
                UserData.gender!!,
                UserData.age!!,
                UserData.weight!!,
                UserData.height!!,
                UserData.goal!!,
                UserData.activityLevel!!,
                UserData.password ?: ""
            )

            val userId = SQLBridge.nativeGetLastUserId()
            if (userId == -1) return false

            SQLBridge.nativeCalculateNutrition(
                userId,
                UserData.gender!!,
                UserData.age!!,
                UserData.weight!!,
                UserData.height!!,
                UserData.goal!!,
                UserData.activityLevel!!
            )

            return true
        }
    }
}
