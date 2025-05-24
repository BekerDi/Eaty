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
                                                           jint gender, jint age,
                                                           jint weight, jint height,
                                                           jint goal, jint activityLevel) {
    float bmr;

    // формула Миффлина-Сан Жеора
    if (gender == 0) { // 0 - мужчина
        bmr = 10 * weight + 6.25 * height - 5 * age + 5;
    } else { // 1 - женщина
        bmr = 10 * weight + 6.25 * height - 5 * age - 161;
    }

    // Коэффициенты активности
    float activityMultiplier;
    switch (activityLevel) {
        case 1: activityMultiplier = 1.2f; break;
        case 2: activityMultiplier = 1.375f; break;
        case 3: activityMultiplier = 1.4f; break;
        case 4: activityMultiplier = 1.5f; break;
        // максимум
        default: activityMultiplier = 1.2f; break;
    }

    float maintenanceCalories = bmr * activityMultiplier;

    // Цель: 0 - сброс, 1 - поддержание, 2 - набор
    if (goal == 0) maintenanceCalories -= 300;
    else if (goal == 2) maintenanceCalories += 300;

    // Расчёт БЖУ
    float protein = weight * 1.8f; // 1.8 г/кг
    float fat = weight * 1.0f;     // 1 г/кг
    float proteinCalories = protein * 4;
    float fatCalories = fat * 9;
    float remainingCalories = maintenanceCalories - (proteinCalories + fatCalories);
    float carbs = remainingCalories / 4;

    jfloatArray result = env->NewFloatArray(4);
    float values[4] = { maintenanceCalories, protein, fat, carbs };
    env->SetFloatArrayRegion(result, 0, 4, values);

    return result;
}

