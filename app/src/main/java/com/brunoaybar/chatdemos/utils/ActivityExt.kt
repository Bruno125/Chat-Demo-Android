package com.brunoaybar.chatdemos.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.app.ShareCompat
import android.view.View
import com.brunoaybar.chatdemos.App
import com.brunoaybar.chatdemos.data.ChatRepository
import com.brunoaybar.chatdemos.data.data.ChatInjection
import java.io.File
import java.io.FileOutputStream

inline fun <reified T> Activity.open(){
    startActivity(Intent(this,T::class.java))
}

inline fun <reified T> Activity.find(id: Int): T{
    return findViewById(id) as T
}

fun Context.getRepository(): ChatRepository = (applicationContext as App).repository

fun Activity.share(data: String){

    startActivity(ShareCompat.IntentBuilder
            .from(this)
            .setText(data)
            .setType("text/plain")
            .intent)

}

fun Activity.shareAsFile(data: String, fileName: String){

    val file = writeToFile(data, fileName)

    startActivity(ShareCompat.IntentBuilder
            .from(this)
            .setStream(Uri.fromFile(file))
            .setType("text/*")
            .intent)

}