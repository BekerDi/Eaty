#pragma once
#include <string>

class RecipeManager {
public:
    static std::string getRecipeTitle(const std::string& recipeId);

    static std::string getRecipeShortDescription(const std::string& recipeId);

    static std::string getRecipeFullText(const std::string& recipeId);

    static std::string getRecipeImageName(const std::string& recipeId);
};
