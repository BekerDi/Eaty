#include <jni.h>
#include <string>
#include <sqlite3.h>

sqlite3* db = nullptr;

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_myeaty_SQLBridge_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {

    std::string hello = "SQLite version: ";
    hello += sqlite3_libversion();
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_myeaty_SQLBridge_nativeOpenDatabase(
        JNIEnv *env,
        jobject /* this */,
        jstring dbPath_) {

    const char *dbPath = env->GetStringUTFChars(dbPath_, nullptr);

    int rc = sqlite3_open(dbPath, &db);
    if (rc) {
        // Ошибка при открытии базы
        sqlite3_close(db);
        db = nullptr;
    }

    env->ReleaseStringUTFChars(dbPath_, dbPath);
}
