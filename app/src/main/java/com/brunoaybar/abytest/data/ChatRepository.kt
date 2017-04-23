package com.brunoaybar.abytest.data

import io.reactivex.Flowable

interface ChatRepository{
    fun send(message: String)
    fun receive(): Flowable<Message>
}