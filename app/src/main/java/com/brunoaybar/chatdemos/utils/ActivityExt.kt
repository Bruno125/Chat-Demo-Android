package com.brunoaybar.chatdemos.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import com.brunoaybar.chatdemos.App
import com.brunoaybar.chatdemos.data.ChatRepository
import com.brunoaybar.chatdemos.data.data.ChatInjection

inline fun <reified T> Activity.open(){
    startActivity(Intent(this,T::class.java))
}

inline fun <reified T> Activity.find(id: Int): T{
    return findViewById(id) as T
}

fun Context.getRepository(): ChatRepository = (applicationContext as App).repository