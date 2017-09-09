package com.brunoaybar.chatdemos

import android.app.Application
import com.brunoaybar.chatdemos.data.ChatRepository
import com.brunoaybar.chatdemos.data.data.ChatInjection
import com.brunoaybar.chatdemos.utils.getRepository

class App: Application() {

    lateinit var repository: ChatRepository

    override fun onCreate() {
        super.onCreate()
        repository = ChatInjection.getRepo(this)
    }

}