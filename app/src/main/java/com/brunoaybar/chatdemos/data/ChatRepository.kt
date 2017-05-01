package com.brunoaybar.chatdemos.data

import io.reactivex.Flowable

interface ChatRepository{
    val name: String
    fun send(message: String)
    fun receive(): Flowable<Message>
}