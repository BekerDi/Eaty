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
                                                         jstring name_,
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
    const char* sql = "INSERT INTO Users (name, age, weight, height, goal, activity_level, password) VALUES (?, ?, ?, ?, ?, ?, ?);";
    sqlite3_stmt* stmt;

    if (sqlite3_prepare_v2(db, sql, -1, &stmt, nullptr) == SQLITE_OK) {
        sqlite3_bind_text(stmt, 1, name, -1, SQLITE_TRANSIENT);
        sqlite3_bind_int(stmt, 2, age);
        sqlite3_bind_int(stmt, 3, weight);
        sqlite3_bind_int(stmt, 4, height);
        sqlite3_bind_int(stmt, 5, goal);
        sqlite3_bind_int(stmt, 6, activityLevel);
        sqlite3_bind_text(stmt, 7, password, -1, SQLITE_TRANSIENT);

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
