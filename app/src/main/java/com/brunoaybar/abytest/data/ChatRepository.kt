package com.brunoaybar.abytest.data

import io.reactivex.Flowable

interface ChatRepository{
    val name: String
    fun send(message: String)
    fun receive(): Flowable<Message>
}