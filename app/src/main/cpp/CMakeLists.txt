cmake_minimum_required(VERSION 3.22.1)

project("myeaty")

add_library(
        sqlite3
        STATIC
        sqlite/sqlite3.c
)

target_include_directories(
        sqlite3
        PUBLIC
        ${CMAKE_CURRENT_SOURCE_DIR}/sqlite
)

add_library(
        myeaty
        SHARED
        native-lib.cpp
        RecipeManager.cpp
        RationManager.cpp
)

target_include_directories(
        myeaty
        PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/sqlite
        ${CMAKE_SYSROOT}/usr/include
)

find_library(
        log-lib
        log
)

target_link_libraries(
        myeaty
        sqlite3
        ${log-lib}
        android
        jnigraphics
)

if (NOT ANDROID)
    message(STATUS "Configuring unit tests (host build)")

    enable_testing()

    add_subdirectory(external/googletest)

    include_directories(external/googletest/googletest/include)

    add_executable(my_tests
            RationManagerTest.cpp
            RecipeManagerTest.cpp
    )

    target_link_libraries(my_tests
            gtest
            gtest_main
            sqlite3
    )
endif()
