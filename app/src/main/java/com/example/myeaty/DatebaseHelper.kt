package com.example.myeaty

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object DatabaseHelper {

    fun copyDatabaseFromAssets(context: Context, dbName: String): String {
        val dbPath = context.getDatabasePath(dbName)

        if (!dbPath.exists()) {
            dbPath.parentFile?.mkdirs()

            try {
                val inputStream: InputStream = context.assets.open(dbName)
                val outputStream: OutputStream = FileOutputStream(dbPath)

                val buffer = ByteArray(1024)
                var length: Int

                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return dbPath.absolutePath
    }
}
