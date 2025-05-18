package com.example.myeaty

import android.content.Context
import java.io.File

object DatabaseHelper {

    fun copyDatabaseFromAssets(context: Context, dbName: String): String {
        val dbPath = context.getDatabasePath(dbName)

        if (!dbPath.exists()) {
            dbPath.parentFile?.mkdirs()
            context.assets.open(dbName).use { input ->
                dbPath.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        return dbPath.absolutePath
    }
}
