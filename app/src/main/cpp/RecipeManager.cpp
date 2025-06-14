#include "RecipeManager.h"

std::string RecipeManager::getRecipeTitle(const std::string& recipeId) {
    if (recipeId == "oatmeal") {
        return u8"Овсяноблин";
    } else if (recipeId == "syrniki") {
        return u8"Сырники";
    } else if (recipeId == "brownie") {
        return u8"Брауни";
    } else {
        return u8"Неизвестный рецепт";
    }
}

std::string RecipeManager::getRecipeShortDescription(const std::string& recipeId) {
    if (recipeId == "oatmeal") {
        return u8"Блин из овсянки с плотной текстурой и ореховым вкусом.";
    } else if (recipeId == "syrniki") {
        return u8"Жареные творожные блинчики в форме биточков.";
    } else if (recipeId == "brownie") {
        return u8"Шоколадный печёный десерт: мягкий или хрустящий.";
    } else {
        return u8"Описание недоступно.";
    }
}

std::string RecipeManager::getRecipeFullText(const std::string& recipeId) {
    std::string fullText;

    if (recipeId == "oatmeal") {
        fullText =
                u8"Ингредиенты:\n"
                u8"- Овсянка: 30 г\n"
                u8"- Яйцо: 1 шт\n"
                u8"- Сметана: 1 ст. л.\n"
                u8"- Соль щепотка\n\n"
                u8"Шаги приготовления:\n"
                u8"1. Перемешать овсянку до состояния муки.\n"
                u8"2. Добавить яйцо, мёд и соль, тщательно перемешать.\n"
                u8"3. Выпекать блин на разогретой сковороде 2–3 мин с каждой стороны.\n"
                u8"4. Подавать со сметаной, бананом или ягодами по вкусу.";
    }
    else if (recipeId == "syrniki") {
        fullText =
                u8"Ингредиенты:\n"
                u8"- Творог: 180 г\n"
                u8"- Манка: 2 ст. л.\n"
                u8"- Сахар: 1 ст. л.\n"
                u8"- Яйцо: 1 шт\n"
                u8"- Соль: щепотка\n\n"
                u8"Шаги приготовления:\n"
                u8"1. Перемешать все ингредиенты до однородной массы.\n"
                u8"2. Сформировать сырники по 55 г.\n"
                u8"3. Жарить на масле на среднем огне по 3–4 мин с каждой стороны до золотистой корочки.\n"
                u8"4. Подавать со сметаной, ягодным соусом или сгущёнкой.";
    }
    else if (recipeId == "brownie") {
        fullText =
                u8"Ингредиенты:\n"
                u8"- Шоколад: 100 г\n"
                u8"- Сливочное масло: 80 г\n"
                u8"- Сахар: 100 г\n"
                u8"- Яйца: 2 шт\n"
                u8"- Мука: 50 г\n"
                u8"- Разрыхлитель: 1 ч. л.\n\n"
                u8"Шаги приготовления:\n"
                u8"1. Растопить шоколад с маслом на водяной бане.\n"
                u8"2. Взбить яйца с сахаром до пышной массы.\n"
                u8"3. Влить растопленный шоколад в яичную массу, перемешать.\n"
                u8"4. Добавить муку и разрыхлитель, аккуратно перемешать лопаткой.\n"
                u8"5. Вылить тесто в форму, выпекать в разогретой до 180 °C духовке 15–20 мин.\n"
                u8"6. Остудить, нарезать на квадраты и подавать.";
    }
    else {
        fullText = u8"Информация о рецепте отсутствует.";
    }

    return fullText;
}

std::string RecipeManager::getRecipeImageName(const std::string& recipeId) {
    if (recipeId == "oatmeal") {
        return "oatmeal_image";
    } else if (recipeId == "syrniki") {
        return "syrniki_image";
    } else if (recipeId == "brownie") {
        return "brownie_image";
    } else {
        return "unknown_image";
    }
}
