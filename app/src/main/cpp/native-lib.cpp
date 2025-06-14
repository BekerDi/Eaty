#include <jni.h>
#include <string>
#include <sqlite3.h>
#include <android/log.h>
#include <vector>

#define LOG_TAG "MyEatyDebug"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

sqlite3* db = nullptr;


extern "C"
JNIEXPORT void JNICALL
Java_com_example_myeaty_SQLBridge_nativeClearTables(JNIEnv*, jobject) {

    const char* tables[] = { "Users", "KBJU", "Products" };

    for (const char* table : tables) {
        std::string sql = "DELETE FROM ";
        sql += table;
        sql += ";";

        char* errMsg = nullptr;
        if (sqlite3_exec(db, sql.c_str(), nullptr, nullptr, &errMsg) != SQLITE_OK) {
            LOGI("Failed to clear table %s: %s", table, errMsg);
            sqlite3_free(errMsg);
        } else {
            LOGI("Cleared table %s", table);
        }
    }
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_myeaty_SQLBridge_nativeOpenDatabase(JNIEnv* env, jobject, jstring dbPath_) {
    const char* dbPath = env->GetStringUTFChars(dbPath_, nullptr);
    LOGI("Opening database at path: %s", dbPath);

    bool success = false;
    if (sqlite3_open(dbPath, &db) != SQLITE_OK) {
        LOGI("Failed to open database.");
        sqlite3_close(db);
        db = nullptr;
        success = false;
    } else {
        const char* createSQL = "CREATE TABLE IF NOT EXISTS Users ("
                                "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                                "name TEXT, "
                                "gender INTEGER,"
                                "age INTEGER, "
                                "weight INTEGER, "
                                "height INTEGER, "
                                "goal INTEGER, "
                                "activity_level INTEGER, "
                                "password TEXT);";

        char* errMsg = nullptr;
        if (sqlite3_exec(db, createSQL, nullptr, nullptr, &errMsg) != SQLITE_OK) {
            LOGI("Table create error: %s", errMsg);
            sqlite3_free(errMsg);
            success = false;
        } else {
            LOGI("Users table is ready.");
            success = true;
        }
    }

    env->ReleaseStringUTFChars(dbPath_, dbPath);
    return success ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_myeaty_SQLBridge_nativeSaveUserFullData(JNIEnv* env, jobject,
                                                         jstring name_, jint gender,
                                                         jint age, jint weight, jint height,
                                                         jint goal, jint activityLevel,
                                                         jstring password_) {
    if (!db) {
        LOGI("Database is not open.");
        return;
    }

    const char* name = env->GetStringUTFChars(name_, nullptr);
    const char* password = env->GetStringUTFChars(password_, nullptr);

    const char* sql = "INSERT INTO Users (name, gender, age, weight, height, goal, activity_level, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
    sqlite3_stmt* stmt;

    if (sqlite3_prepare_v2(db, sql, -1, &stmt, nullptr) == SQLITE_OK) {
        sqlite3_bind_text(stmt, 1, name, -1, SQLITE_TRANSIENT);
        sqlite3_bind_int(stmt, 2, gender);
        sqlite3_bind_int(stmt, 3, age);
        sqlite3_bind_int(stmt, 4, weight);
        sqlite3_bind_int(stmt, 5, height);
        sqlite3_bind_int(stmt, 6, goal);
        sqlite3_bind_int(stmt, 7, activityLevel);
        sqlite3_bind_text(stmt, 8, password, -1, SQLITE_TRANSIENT);

        if (sqlite3_step(stmt) == SQLITE_DONE) {
            LOGI("User inserted successfully.");
        } else {
            LOGI("Failed to insert user.");
        }

        sqlite3_finalize(stmt);
    } else {
        LOGI("Failed to prepare insert statement.");
    }

    env->ReleaseStringUTFChars(name_, name);
    env->ReleaseStringUTFChars(password_, password);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_myeaty_SQLBridge_nativeCloseDatabase(JNIEnv*, jobject) {
    if (db) {
        sqlite3_close(db);
        db = nullptr;
        LOGI("Database closed.");
    }
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_myeaty_SQLBridge_nativeCheckUserExists(JNIEnv* env, jobject, jstring jName) {
    if (!db) return JNI_FALSE;

    const char* name = env->GetStringUTFChars(jName, nullptr);
    const char* sql = "SELECT 1 FROM Users WHERE name = ? LIMIT 1;";
    sqlite3_stmt* stmt;
    bool exists = false;

    if (sqlite3_prepare_v2(db, sql, -1, &stmt, nullptr) == SQLITE_OK) {
        sqlite3_bind_text(stmt, 1, name, -1, SQLITE_STATIC);

        if (sqlite3_step(stmt) == SQLITE_ROW) {
            exists = true;
        }

        sqlite3_finalize(stmt);
    }

    env->ReleaseStringUTFChars(jName, name);
    return exists ? JNI_TRUE : JNI_FALSE;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_myeaty_SQLBridge_nativeLoginUser(JNIEnv* env, jobject,
                                                  jstring jName, jstring jPassword) {
    const char* name = env->GetStringUTFChars(jName, nullptr);
    const char* password = env->GetStringUTFChars(jPassword, nullptr);

    const char* sql = "SELECT id FROM Users WHERE name = ? AND password = ?;";
    sqlite3_stmt* stmt;
    int userId = -1;

    if (sqlite3_prepare_v2(db, sql, -1, &stmt, nullptr) == SQLITE_OK) {
        sqlite3_bind_text(stmt, 1, name, -1, SQLITE_STATIC);
        sqlite3_bind_text(stmt, 2, password, -1, SQLITE_STATIC);

        if (sqlite3_step(stmt) == SQLITE_ROW) {
            userId = sqlite3_column_int(stmt, 0);
        }

        sqlite3_finalize(stmt);
    }

    env->ReleaseStringUTFChars(jName, name);
    env->ReleaseStringUTFChars(jPassword, password);

    return userId;
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_example_myeaty_SQLBridge_nativeCalculateNutrition(JNIEnv* env, jobject,
                                                           jint userId,
                                                           jint gender, jint age,
                                                           jint weight, jint height,
                                                           jint goal, jint activityLevel) {
    float bmr;
    if (gender == 0) {
        bmr = 10 * weight + 6.25 * height - 5 * age + 5;
    } else {
        bmr = 10 * weight + 6.25 * height - 5 * age - 161;
    }

    float activityMultiplier;
    switch (activityLevel) {
        case 1: activityMultiplier = 1.2f; break;
        case 2: activityMultiplier = 1.375f; break;
        case 3: activityMultiplier = 1.4f; break;
        case 4: activityMultiplier = 1.5f; break;
        default: activityMultiplier = 1.2f; break;
    }

    float maintenanceCalories = bmr * activityMultiplier;

    if (goal == 0) maintenanceCalories -= 300;
    else if (goal == 2) maintenanceCalories += 300;

    float protein = weight * 1.8f;
    float fat = weight * 1.0f;
    float proteinCalories = protein * 4;
    float fatCalories = fat * 9;
    float remainingCalories = maintenanceCalories - (proteinCalories + fatCalories);
    float carbs = remainingCalories / 4;

    const char* createKBJUTable = "CREATE TABLE IF NOT EXISTS KBJU ("
                                  "user_id INTEGER PRIMARY KEY, "
                                  "calories INTEGER, "
                                  "proteins REAL, "
                                  "fats REAL, "
                                  "carbs REAL, "
                                  "FOREIGN KEY(user_id) REFERENCES Users(id));";

    char* errMsg = nullptr;
    if (sqlite3_exec(db, createKBJUTable, nullptr, nullptr, &errMsg) != SQLITE_OK) {
        LOGI("KBJU Table error: %s", errMsg);
        sqlite3_free(errMsg);
    } else {
        LOGI("KBJU table is ready.");
    }

    const char* insertSQL = "INSERT OR REPLACE INTO KBJU (user_id, calories, proteins, fats, carbs) VALUES (?, ?, ?, ?, ?);";
    sqlite3_stmt* stmt;

    if (sqlite3_prepare_v2(db, insertSQL, -1, &stmt, nullptr) == SQLITE_OK) {
        sqlite3_bind_int(stmt, 1, userId);
        sqlite3_bind_int(stmt, 2, static_cast<int>(maintenanceCalories));
        sqlite3_bind_double(stmt, 3, protein);
        sqlite3_bind_double(stmt, 4, fat);
        sqlite3_bind_double(stmt, 5, carbs);

        if (sqlite3_step(stmt) == SQLITE_DONE) {
            LOGI("KBJU inserted successfully.");
        } else {
            LOGI("Failed to insert KBJU.");
        }

        sqlite3_finalize(stmt);
    } else {
        LOGI("Failed to prepare KBJU insert.");
    }

    jfloatArray result = env->NewFloatArray(4);
    float values[4] = { maintenanceCalories, protein, fat, carbs };
    env->SetFloatArrayRegion(result, 0, 4, values);
    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_myeaty_SQLBridge_nativeGetLastUserId(JNIEnv*, jobject) {
    if (!db) return -1;

    const char* sql = "SELECT MAX(id) FROM Users;";
    sqlite3_stmt* stmt;
    int lastId = -1;

    if (sqlite3_prepare_v2(db, sql, -1, &stmt, nullptr) == SQLITE_OK) {
        if (sqlite3_step(stmt) == SQLITE_ROW) {
            lastId = sqlite3_column_int(stmt, 0);
        }
        sqlite3_finalize(stmt);
    }

    return lastId;
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_example_myeaty_SQLBridge_nativeGetKBJUForUser(JNIEnv* env, jobject, jint userId) {
    if (!db) return nullptr;

    const char* sql = "SELECT calories, proteins, fats, carbs FROM KBJU WHERE user_id = ?;";
    sqlite3_stmt* stmt;

    jfloatArray result = env->NewFloatArray(4);
    float values[4] = {0, 0, 0, 0};

    if (sqlite3_prepare_v2(db, sql, -1, &stmt, nullptr) == SQLITE_OK) {
        sqlite3_bind_int(stmt, 1, userId);

        if (sqlite3_step(stmt) == SQLITE_ROW) {
            values[0] = static_cast<float>(sqlite3_column_int(stmt, 0));
            values[1] = static_cast<float>(sqlite3_column_double(stmt, 1));
            values[2] = static_cast<float>(sqlite3_column_double(stmt, 2));
            values[3] = static_cast<float>(sqlite3_column_double(stmt, 3));
        }

        sqlite3_finalize(stmt);
    }

    env->SetFloatArrayRegion(result, 0, 4, values);
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_myeaty_SQLBridge_nativeInitProductDatabase(JNIEnv* env, jobject) {
    if (!db) return;

    const char *createProductsTableSQL = "CREATE TABLE IF NOT EXISTS Products ("
                                         "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                                         "name TEXT UNIQUE, "
                                         "calories_per_100g REAL, "
                                         "protein_per_100g REAL, "
                                         "fat_per_100g REAL, "
                                         "carb_per_100g REAL);";
    char *errMsg = nullptr;
    if (sqlite3_exec(db, createProductsTableSQL, nullptr, nullptr, &errMsg) != SQLITE_OK) {
        LOGI("Failed to create Products table: %s", errMsg);
        sqlite3_free(errMsg);
    } else {
        LOGI("Products table created successfully.");
    }

    // Создаем таблицу FoodEntry
    const char* createFoodEntrySQL = "CREATE TABLE IF NOT EXISTS FoodEntry ("
                                     "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                                     "meal_id INTEGER, "
                                     "product_id INTEGER, "
                                     "portion_size REAL, "
                                     "FOREIGN KEY(meal_id) REFERENCES Meal(id), "
                                     "FOREIGN KEY(product_id) REFERENCES Products(id));";

    if (sqlite3_exec(db, createFoodEntrySQL, nullptr, nullptr, &errMsg) != SQLITE_OK) {
        LOGI("Failed to create FoodEntry table: %s", errMsg);
        sqlite3_free(errMsg);
    } else {
        LOGI("FoodEntry table created.");
    }
}
extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_example_myeaty_SQLBridge_nativeGetAllProducts(JNIEnv* env, jobject) {
    if (!db) return nullptr;

    const char* sql = "SELECT id, name, calories_per_100g, protein_per_100g, fat_per_100g, carb_per_100g FROM Products;";
    sqlite3_stmt* stmt;

    if (sqlite3_prepare_v2(db, sql, -1, &stmt, nullptr) != SQLITE_OK) {
        LOGI("Failed to prepare SELECT from Products.");
        return nullptr;
    }

    jclass productClass = env->FindClass("com/example/myeaty/Product");
    if (!productClass) {
        LOGI("Can't find Product class.");
        return nullptr;
    }

    jmethodID ctor = env->GetMethodID(productClass, "<init>", "(ILjava/lang/String;FFFF)V");
    if (!ctor) {
        LOGI("Can't find Product constructor.");
        return nullptr;
    }

    std::vector<jobject> products;

    while (sqlite3_step(stmt) == SQLITE_ROW) {
        int id = sqlite3_column_int(stmt, 0);
        const char* nameC = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 1));
        jstring name = env->NewStringUTF(nameC ? nameC : "");

        jfloat cal = sqlite3_column_double(stmt, 2);
        jfloat prot = sqlite3_column_double(stmt, 3);
        jfloat fat = sqlite3_column_double(stmt, 4);
        jfloat carb = sqlite3_column_double(stmt, 5);

        jobject product = env->NewObject(productClass, ctor, id, name, cal, prot, fat, carb);
        products.push_back(product);
    }

    sqlite3_finalize(stmt);

    jobjectArray array = env->NewObjectArray(products.size(), productClass, nullptr);
    for (size_t i = 0; i < products.size(); ++i) {
        env->SetObjectArrayElement(array, i, products[i]);
    }

    return array;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_myeaty_SQLBridge_nativeGetUserProfile(JNIEnv* env, jobject, jint userId) {
    if (!db) return nullptr;

    const char* sql = "SELECT name, gender, age, weight, height, goal, activity_level FROM Users WHERE id = ?;";
    sqlite3_stmt* stmt;
    jobject result = nullptr;

    if (sqlite3_prepare_v2(db, sql, -1, &stmt, nullptr) == SQLITE_OK) {
        sqlite3_bind_int(stmt, 1, userId);

        if (sqlite3_step(stmt) == SQLITE_ROW) {
            const char* nameC = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 0));
            jstring name = env->NewStringUTF(nameC ? nameC : "");

            int gender = sqlite3_column_int(stmt, 1);
            int age = sqlite3_column_int(stmt, 2);
            int weight = sqlite3_column_int(stmt, 3);
            int height = sqlite3_column_int(stmt, 4);
            int goal = sqlite3_column_int(stmt, 5);
            int activityLevel = sqlite3_column_int(stmt, 6);

            jclass userProfileClass = env->FindClass("com/example/myeaty/UserProfile");
            jmethodID ctor = env->GetMethodID(userProfileClass, "<init>", "(Ljava/lang/String;IIIIII)V");

            result = env->NewObject(userProfileClass, ctor, name, gender, age, weight, height, goal, activityLevel);
        }

        sqlite3_finalize(stmt);
    }

    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_myeaty_SQLBridge_nativeUpdateUserProfile(JNIEnv* env, jobject,
                                                          jint userId,
                                                          jint age, jint weight,
                                                          jint height, jint goal,
                                                          jint activityLevel) {
    if (!db) return;

    const char* updateSQL = "UPDATE Users SET age = ?, weight = ?, height = ?, goal = ?, activity_level = ? WHERE id = ?;";
    sqlite3_stmt* stmt;

    if (sqlite3_prepare_v2(db, updateSQL, -1, &stmt, nullptr) == SQLITE_OK) {
        sqlite3_bind_int(stmt, 1, age);
        sqlite3_bind_int(stmt, 2, weight);
        sqlite3_bind_int(stmt, 3, height);
        sqlite3_bind_int(stmt, 4, goal);
        sqlite3_bind_int(stmt, 5, activityLevel);
        sqlite3_bind_int(stmt, 6, userId);

        if (sqlite3_step(stmt) == SQLITE_DONE) {
            LOGI("User data updated.");
        } else {
            LOGI("Error updating user.");
        }

        sqlite3_finalize(stmt);
    }

    const char* genderSQL = "SELECT gender FROM Users WHERE id = ?;";
    sqlite3_stmt* genderStmt;
    int gender = 0;

    if (sqlite3_prepare_v2(db, genderSQL, -1, &genderStmt, nullptr) == SQLITE_OK) {
        sqlite3_bind_int(genderStmt, 1, userId);

        if (sqlite3_step(genderStmt) == SQLITE_ROW) {
            gender = sqlite3_column_int(genderStmt, 0);
        }

        sqlite3_finalize(genderStmt);
    }

    Java_com_example_myeaty_SQLBridge_nativeCalculateNutrition(env, nullptr,
                                                               userId, gender, age, weight, height, goal, activityLevel);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_myeaty_SQLBridge_nativeInsertProduct(JNIEnv* env, jobject,
                                                      jstring name_, jfloat cal,
                                                      jfloat prot, jfloat fat, jfloat carb) {
    if (!db) return;

    const char* name = env->GetStringUTFChars(name_, nullptr);
    const char* sql = "INSERT OR IGNORE INTO Products (name, calories_per_100g, protein_per_100g, fat_per_100g, carb_per_100g) VALUES (?, ?, ?, ?, ?);";

    sqlite3_stmt* stmt;
    if (sqlite3_prepare_v2(db, sql, -1, &stmt, nullptr) == SQLITE_OK) {
        sqlite3_bind_text(stmt, 1, name, -1, SQLITE_TRANSIENT);
        sqlite3_bind_double(stmt, 2, cal);
        sqlite3_bind_double(stmt, 3, prot);
        sqlite3_bind_double(stmt, 4, fat);
        sqlite3_bind_double(stmt, 5, carb);

        sqlite3_step(stmt);
        sqlite3_finalize(stmt);
    }

    env->ReleaseStringUTFChars(name_, name);
}
// ---- NativeLib (рецепты через RecipeManager) ----

#undef LOG_TAG
#define LOG_TAG "NativeLib"

#include "RecipeManager.h"
#include "RationManager.h"

static std::string jstringToStdString(JNIEnv* env, jstring jStr) {
    if (!jStr) return "";

    const char* chars = env->GetStringUTFChars(jStr, nullptr);
    std::string s(chars);
    env->ReleaseStringUTFChars(jStr, chars);
    return s;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_myeaty_NativeLib_getRecipeTitle(
        JNIEnv* env,
        jobject /* this */,
        jstring jRecipeId) {
    std::string recipeId = jstringToStdString(env, jRecipeId);
    std::string title = RecipeManager::getRecipeTitle(recipeId);
    return env->NewStringUTF(title.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_myeaty_NativeLib_getRecipeShortDescription(
        JNIEnv* env,
        jobject /* this */,
        jstring jRecipeId) {
    std::string recipeId = jstringToStdString(env, jRecipeId);
    std::string shortDesc = RecipeManager::getRecipeShortDescription(recipeId);
    return env->NewStringUTF(shortDesc.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_myeaty_NativeLib_getRecipeFullText(
        JNIEnv* env,
        jobject /* this */,
        jstring jRecipeId) {
    std::string recipeId = jstringToStdString(env, jRecipeId);
    std::string fullText = RecipeManager::getRecipeFullText(recipeId);
    return env->NewStringUTF(fullText.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_myeaty_NativeLib_getRecipeImageName(
        JNIEnv* env,
        jobject /* this */,
        jstring jRecipeId) {
    std::string recipeId = jstringToStdString(env, jRecipeId);
    std::string imageName = RecipeManager::getRecipeImageName(recipeId);
    return env->NewStringUTF(imageName.c_str());
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_myeaty_NativeLib_getRationTitle(
        JNIEnv* env,
        jobject /* this */,
        jstring jRationId) {
    std::string rationId = jstringToStdString(env, jRationId);
    std::string title = RationManager::getRationTitle(rationId);
    return env->NewStringUTF(title.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_myeaty_NativeLib_getRationShortDescription(
        JNIEnv* env,
        jobject /* this */,
        jstring jRationId) {
    std::string rationId = jstringToStdString(env, jRationId);
    std::string shortDesc = RationManager::getRationShortDescription(rationId);
    return env->NewStringUTF(shortDesc.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_myeaty_NativeLib_getRationFullPlan(
        JNIEnv* env,
        jobject /* this */,
        jstring jRationId) {
    std::string rationId = jstringToStdString(env, jRationId);
    std::string fullPlan = RationManager::getRationFullPlan(rationId);
    return env->NewStringUTF(fullPlan.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_myeaty_NativeLib_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
