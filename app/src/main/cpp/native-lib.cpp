#include <jni.h>
#include <string>
#include <sqlite3.h>
#include <android/log.h>

#define LOG_TAG "MyEatyDebug"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

sqlite3* db = nullptr;

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

    // Используем подготовленные выражения (лучше и безопаснее)
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
//Считаем калории!!!! и БЖУ
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

    // 🔧 Создаем таблицу KBJU
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

    // 🔧 Вставляем данные в таблицу KBJU
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

    // 🔁 Возвращаем результат
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
//Делаем запрос на имя и пароль, чтобы пользователь мог соврешить вход
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
//Возвращаем КБЖУ авторизованного пользователя
extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_example_myeaty_SQLBridge_nativeGetKBJUForUser(JNIEnv* env, jobject, jint userId) {
    if (!db) return nullptr;

    const char* sql = "SELECT calories, proteins, fats, carbs FROM KBJU WHERE user_id = ?;";
    sqlite3_stmt* stmt;

    jfloatArray result = env->NewFloatArray(4);
    float values[4] = {0, 0, 0, 0}; // default

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
//Создаем таблицу с продуктами
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

    const char *insertProductSQL = "INSERT OR IGNORE INTO Products "
                                   "(name, calories_per_100g, protein_per_100g, fat_per_100g, carb_per_100g) "
                                   "VALUES (?, ?, ?, ?, ?);";
    sqlite3_stmt *stmt;

    if (sqlite3_prepare_v2(db, insertProductSQL, -1, &stmt, nullptr) == SQLITE_OK) {

        struct Product {
            const char *name;
            float cal, prot, fat, carb;
        };

        Product products[] = {
                {"Гречка",      310, 12,   2,    61},
                {"Рис",         323, 7,    0,    73},
                {"Пшено",       334, 12,   2,    69},
                {"Чечевица",    310, 24,   1,    53},
                {"Геркулес",    355, 13,   6,    66},
                {"Кукурузная ", 325, 8.0,  1.0,  75.0},
                {"Макароны ",   335, 10.0, 1.0,  69.0},
                {"Булгур",      342, 12.0, 1.0,  58.0},
                {"Кабачок",     24,  0.6,  0.6,  4.6},
                {"Картофель",   80,  2.0,  0.4,  18.0},
                {"Огурец",      15,  0.8,  0.1,  2.8},
                {"Пастернак",   47,  1.4,  0.5,  9.2},
                {"Помидоры",    20,  0.6,  0.2,  4.2},
                {"Тыква",       22,  1.0,  0.1,  4.4},
                {"Банан",       89,  2.0,  0.0,  22.0},
                {"Яблоко",      47,  0.0,  0.0,  10.0},
                {"Груша",       42,  0.0,  0.0,  11.0},
                {"Авокадо ",    200, 2.0,  20.0, 7.0},
                {"Яйцо",        157, 12.7, 11.5, 0.7},
                {"Молоко 2,5%", 53,  2.8,  2.5,  4.6},
                {"Молоко 3,2%", 58,  2.8,  3.2,  4.6},
                {"Кефир 1%",    37,  2.8,  1.0,  4.0},
                {"Кефир 2,5%",  51,  3.0,  2.5,  4.0},
                {"Кефир 3,2%",  58,  3.2,  3.2,  4.1},
                {"Творог 0,5%", 71,  16.0, 0.5,  1.0},
                {"Творог 5%",   121, 17.2, 5.0,  1.8},
                {"Творог 9%",   159, 16.7, 9.0,  2.0},
                {"Курица",      110, 23.0, 1.2,  0.0},
                {"Индейка",     84,  19.2, 0.7,  0.0},
                {"Баранина",    209, 15.6, 16.3, 0.0},
                {"Говядина",    187, 18.9, 12.4, 0.0},
                {"Свинина",     259, 16.0, 21.6, 0.0},
                {"Конина",      187, 20.2, 7.0,  0.0},
                {"Горбуша",     140, 20.0, 6.0,  0.0},
                {"Семга",       225, 21.0, 15.0, 0.0},
                {"Тунец",       108, 23.0, 1.0,  0.0},
                {"Минтай",      81,  17.0, 0.8,  0.0}

        };

        for (auto &p: products) {
            sqlite3_bind_text(stmt, 1, p.name, -1, SQLITE_STATIC);
            sqlite3_bind_double(stmt, 2, p.cal);
            sqlite3_bind_double(stmt, 3, p.prot);
            sqlite3_bind_double(stmt, 4, p.fat);
            sqlite3_bind_double(stmt, 5, p.carb);

            if (sqlite3_step(stmt) != SQLITE_DONE) {
                LOGI("Failed to insert product: %s", p.name);
            }
            sqlite3_reset(stmt);
        }

        sqlite3_finalize(stmt);
        LOGI("Products inserted.");
    } else {
        LOGI("Failed to prepare product insert.");
    }
    // Создаем таблицу Meal
    const char* createMealTableSQL = "CREATE TABLE IF NOT EXISTS Meal ("
                                     "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                                     "user INTEGER, "
                                     "date TEXT, "
                                     "mealType TEXT, "
                                     "FOREIGN KEY(user) REFERENCES Users(id));";

    if (sqlite3_exec(db, createMealTableSQL, nullptr, nullptr, &errMsg) != SQLITE_OK) {
        LOGI("Failed to create Meal table: %s", errMsg);
        sqlite3_free(errMsg);
    } else {
        LOGI("Meal table created.");
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