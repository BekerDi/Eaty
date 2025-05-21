#include <jni.h>
#include <string>
#include <sqlite3.h>
//для проверки записи в таблицу
#include <android/log.h>

#define LOG_TAG "MyEatyDebug"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

sqlite3* db = nullptr;



// Открывает базу данных по указанному пути
extern "C"
JNIEXPORT void JNICALL
Java_com_example_myeaty_SQLBridge_nativeOpenDatabase(
        JNIEnv *env,
        jobject /* this */,
        jstring dbPath_) {



    const char *dbPath = env->GetStringUTFChars(dbPath_, nullptr);

    int openResult = sqlite3_open(dbPath, &db);
    env->ReleaseStringUTFChars(dbPath_, dbPath);

    if (openResult != SQLITE_OK) {
        LOGI("Failed to open database.");
        sqlite3_close(db);
        db = nullptr;
        return; // Прерываем выполнение, если база не открыта
    }

    const char* createTableSQL =
            "CREATE TABLE IF NOT EXISTS Users ("
            "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            "name TEXT, "
            "age INTEGER, "
            "weight INTEGER, "
            "height INTEGER, "
            "goal INTEGER, "
            "activity_level INTEGER, "
            "password TEXT);";

    char* errMsg = nullptr;

    int execResult = sqlite3_exec(db, createTableSQL, nullptr, nullptr, &errMsg);
    if (execResult != SQLITE_OK) {
        LOGI("Failed to create table: %s", errMsg);
        sqlite3_free(errMsg);
    } else {
        LOGI("Users table created or already exists.");
    }
    LOGI("Opening database at path: %s", dbPath);

}



    //Проверяем запись данных о пользователе
extern "C"
JNIEXPORT void JNICALL
Java_com_example_myeaty_SQLBridge_nativePrintAllUsers(JNIEnv* env, jobject /* this */) {
    if (!db) {
        LOGI("Database not open.");
        return;
    }

     const char* sql = "SELECT id, name, age, weight, height, goal, activity_level, password FROM Users;";
    ;
    sqlite3_stmt* stmt;

    int rc = sqlite3_prepare_v2(db, sql, -1, &stmt, nullptr);
    if (rc != SQLITE_OK) {
        LOGI("Failed to prepare statement.");
        return;
    }

    while (sqlite3_step(stmt) == SQLITE_ROW) {
        int id = sqlite3_column_int(stmt, 0);
        const unsigned char* name = sqlite3_column_text(stmt, 1);
        int age = sqlite3_column_int(stmt, 2);
        int weight = sqlite3_column_int(stmt, 3);
        int height = sqlite3_column_int(stmt, 4);
        int goal = sqlite3_column_int(stmt, 5);
        int activityLevel = sqlite3_column_int(stmt, 6);
        const unsigned char* password = sqlite3_column_text(stmt, 7);

        LOGI("User ID: %d | Age: %d | Weight: %d | Height: %d | Goal: %d | Activity Level: %d | Password: %s",
             id, age, weight, height, goal, activityLevel, password);
    }

    sqlite3_finalize(stmt);
}

extern "C" //Вставляем данные
JNIEXPORT void JNICALL
Java_com_example_myeaty_SQLBridge_nativeSaveUserFullData(
        JNIEnv *env,
        jobject /* this */,
        jint age,
        jint weight,
        jint height,
        jint goal,
        jint activityLevel,
        jstring password_) {

    const char *password = env->GetStringUTFChars(password_, 0);

    if (db) {
        std::string sql =
                "INSERT INTO Users (age, weight, height, goal, activity_level, password) VALUES (" +
                std::to_string(age) + ", " +
                std::to_string(weight) + ", " +
                std::to_string(height) + ", " +
                std::to_string(goal) + ", " +
                std::to_string(activityLevel) + ", '" +
                std::string(password) + "');";

        char *errMsg = nullptr;
        int rc = sqlite3_exec(db, sql.c_str(), nullptr, nullptr, &errMsg);
        if (rc != SQLITE_OK) {

            sqlite3_free(errMsg);
        }
    }

    env->ReleaseStringUTFChars(password_, password);
}
    extern "C"//Закрываем бд
    JNIEXPORT void JNICALL
    Java_com_example_myeaty_SQLBridge_nativeCloseDatabase(JNIEnv* env, jobject /* this */) {
        if (db != nullptr) {
            sqlite3_close(db);
            db = nullptr;
            LOGI("Database closed.");
        }
    }


