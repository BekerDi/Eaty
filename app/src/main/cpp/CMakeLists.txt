# Укажи минимальную версию CMake
cmake_minimum_required(VERSION 3.22.1)

project("MyApplication")

add_library(sqlite3 STATIC
        sqlite/sqlite3.c)

target_include_directories(sqlite3 PUBLIC
        sqlite)

add_library(native-lib SHARED
        native-lib.cpp)

target_include_directories(native-lib PRIVATE
        sqlite)

target_link_libraries(native-lib
        sqlite3
        log)
