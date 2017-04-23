package com.brunoaybar.abytest.data.impl

import com.brunoaybar.abytest.data.ChatRepository
import com.brunoaybar.abytest.data.Message
import io.reactivex.Flowable
import io.ably.lib.realtime.AblyRealtime
import io.reactivex.FlowableOnSubscribe
import io.reactivex.BackpressureStrategy
import io.reactivex.FlowableEmitter
import io.reactivex.subjects.PublishSubject


class AblyChatRepository() : ChatRepository{
    private val realtime: AblyRealtime = AblyRealtime("gZ_l9g.y5d3Tw:kSJRo2CIeXcw9w2p")
    private val channel = realtime.channels.get("chat")
    private val messageSubject: PublishSubject<Message> = PublishSubject.create()

    init {
        channel.subscribe {
            val content = it.data.toString()
            val message = Message(content, "1s", "${content.toByteArray().size} bytes")
            messageSubject.onNext(message)
        }
    }


    override fun send(message: String) {
        channel.publish("send",message)
    }

    override fun receive(): Flowable<Message> {
        return messageSubject.toFlowable(BackpressureStrategy.BUFFER)
    }

}