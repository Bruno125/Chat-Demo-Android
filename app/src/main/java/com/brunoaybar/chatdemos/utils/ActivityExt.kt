package com.brunoaybar.chatdemos.utils

import android.app.Activity
import android.content.Intent

inline fun <reified T> Activity.open(){
    startActivity(Intent(this,T::class.java))
}
