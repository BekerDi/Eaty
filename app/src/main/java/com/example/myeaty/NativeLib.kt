 package com.example.myeaty

object NativeLib {
    init {
        System.loadLibrary("myeaty")
    }
    external fun getRecipeTitle(recipeId: String): String
    external fun getRecipeShortDescription(recipeId: String): String
    external fun getRecipeFullText(recipeId: String): String
    external fun getRecipeImageName(recipeId: String): String

    external fun getRationTitle(rationId: String): String
    external fun getRationShortDescription(rationId: String): String
    external fun getRationFullPlan(rationId: String): String

}
