package com.brunoaybar.chatdemos.utils

import java.text.SimpleDateFormat
import java.util.*

fun Date.withFormat(pattern: String, locale: Locale = Locale.US): String{
    return SimpleDateFormat(pattern,locale).format(this)
}