 package com.example.myeaty

object NativeLib {
    init {
        System.loadLibrary("myeaty")
    }

    external fun stringFromJNI(): String

    // =======================
    // Ниже объявим внешние функции, которые вернут данные по рецептам.
    // Мы позже реализуем их на C++.
    // =======================

    /**
     * Получить заголовок рецепта (название) по ID рецепта.
     * @param recipeId в виде строки: "oatmeal", "syrniki", "brownie"
     * @return название рецепта, например "Овсяноблин"
     */
    external fun getRecipeTitle(recipeId: String): String

    /**
     * Получить описание (короткий анонс) для карточки рецепта.
     * @param recipeId — ключ рецепта
     * @return краткая строка-описание
     */
    external fun getRecipeShortDescription(recipeId: String): String

    /**
     * Получить подробный текст рецепта (список ингредиентов + шаги) в виде одного большого String.
     * Можно форматировать, например, через разрыв строки "\n".
     * @param recipeId — ключ рецепта
     * @return полное описание (ингредиенты + шаги)
     */
    external fun getRecipeFullText(recipeId: String): String

    /**
     * Получить название ресурса drawable для картинки рецепта: например "oatmeal_image"
     * (будем брать его из C++ и конвертировать в идентификатор ресурса в Kotlin).
     */
    external fun getRecipeImageName(recipeId: String): String

    external fun getRationTitle(rationId: String): String
    external fun getRationShortDescription(rationId: String): String
    external fun getRationFullPlan(rationId: String): String

}
