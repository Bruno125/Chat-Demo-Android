package com.brunoaybar.chatdemos.utils

import android.content.Context
import java.io.FileOutputStream
import android.R.attr.path
import android.os.Environment
import java.io.File
import android.os.Environment.getExternalStorageDirectory




fun Context.writeToFile(data: String, fileName: String): File {
    val file = File("${Environment.getExternalStorageDirectory()}${File.separator}$fileName")
    file.createNewFile()
    if (file.exists()) {
        val fo = FileOutputStream(file)
        fo.write(data.toByteArray())
        fo.close()
    }
    return file
}