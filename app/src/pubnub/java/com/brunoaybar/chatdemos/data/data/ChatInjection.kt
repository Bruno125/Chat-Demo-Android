package com.brunoaybar.chatdemos.data.data

import com.brunoaybar.chatdemos.data.ChatFactory
import com.brunoaybar.chatdemos.data.ChatProviders
import com.brunoaybar.chatdemos.data.ChatRepository

class ChatInjection{
    companion object{
        val repository: ChatRepository = ChatFactory.create(ChatProviders.PUBNUB)
    }
}