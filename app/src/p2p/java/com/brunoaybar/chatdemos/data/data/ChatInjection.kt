package com.brunoaybar.chatdemos.data.data

import android.content.Context
import com.brunoaybar.chatdemos.data.ChatFactory
import com.brunoaybar.chatdemos.data.ChatProviders
import com.brunoaybar.chatdemos.data.ChatRepository

class ChatInjection{
    companion object{
        fun getRepo(context: Context): ChatRepository = ChatFactory.create(context,ChatProviders.P2PKIT)
    }
}