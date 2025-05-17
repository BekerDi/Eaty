#include <jni.h>
#include <string>
#include <sqlite3.h>

extern "C" JNIEXPORT jstring JNICALL
Kotlin_com_example_myeaty_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {

    std::string hello = "SQLite version: ";
    hello += sqlite3_libversion();
    return env->NewStringUTF(hello.c_str());
}