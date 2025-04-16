package com.datalogic.codiscan.sample

import android.content.Context
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** File name of the log. */
const val LOG_FILENAME = "codiscan-log.txt"

/**
 * Helper function to write a log message to [LOG_FILENAME].
 * @param context the activity/service that is writing the log.
 * @param message the log message to write.
 */
fun writeLog(context: Context, message: String) {
    val logFile = File(context.getExternalFilesDir(null), LOG_FILENAME)
    if (!logFile.exists()) {
        logFile.createNewFile()
    }

    try {
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logMessage = "[$timeStamp] $message\n"
        FileWriter(logFile, true).use { writer ->
            writer.append(logMessage)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

/**
 * Helper function to clear the [LOG_FILENAME].
 * @param context the activity/service that is clearing the log.
 */
fun clearLog(context: Context){
    val logFile = File(context.getExternalFilesDir(null), LOG_FILENAME)
    try {
        if (logFile.exists()) {
            FileWriter(logFile).use { writer ->
                writer.write("")
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

