package com.brunoaybar.chatdemos.data.data

import com.brunoaybar.chatdemos.data.ChatFactory
import com.brunoaybar.chatdemos.data.ChatProviders
import com.brunoaybar.chatdemos.data.ChatRepository
import android.content.Context

class ChatInjection{
    companion object{
        fun getRepo(context: Context): ChatRepository = ChatFactory.create(context,ChatProviders.RABBITMQ)
    }
}