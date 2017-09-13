package com.brunoaybar.chatdemos.data.impl

import android.util.Log
import com.brunoaybar.chatdemos.data.ChatRepository
import com.brunoaybar.chatdemos.data.ChatUtils
import com.brunoaybar.chatdemos.data.Message
import com.lightstreamer.client.*
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject

class LightstreamerRepository : ChatRepository,
        SubscriptionListener by LightstreamerListenerLogger,
        ClientListener by LightstreamerListenerLogger{
    override val name: String get() = "Demo Lightstreamer"

    val serverAddress = "http://192.168.1.38:8080"
    val subscription = Subscription("DISTINCT","chat_room", arrayOf("message", "raw_timestamp", "IP","nick")).apply {
        addListener(this@LightstreamerRepository)
        dataAdapter = "CHAT_ROOM"
        requestedSnapshot = "yes"
    }
    val lsClient = LightstreamerClient(serverAddress,"CHAT").apply {
        connectionDetails.serverAddress = serverAddress
        connect()
        subscribe(subscription)
        addListener(this@LightstreamerRepository)
    }

    private val messageSubject: PublishSubject<Message> = PublishSubject.create()

    override fun send(message: String) {
        ChatUtils.createData(message).let {
            lsClient.sendMessage("CHAT| $it")
        }
    }

    override fun receive(): Flowable<Message> {
        return messageSubject.toFlowable(BackpressureStrategy.BUFFER)
    }

    override fun onItemUpdate(p0: ItemUpdate?) {
        try {
            if(p0 != null && p0.fields != null){
                val message = p0.fields["message"]
                if (message != null){
                    val entry = ChatUtils.parseMessage(message)
                    messageSubject.onNext(entry)
                }else{
                    print("Message es null")
                }
            }else{
                print("Algo es nulo")
            }


            /*p0?.fields?.get("message")?.let{
                val message = ChatUtils.parseMessage(it)
                messageSubject.onNext(message)
            }*/

        }catch (e: Exception){
            print("Exc: $e")
        }

    }

}

object LightstreamerListenerLogger : SubscriptionListener, ClientListener{
    override fun onListenEnd(p0: LightstreamerClient?) {
        print("onListenEnd")
    }

    override fun onListenStart(p0: LightstreamerClient?) {
        print("onListenStart")
    }

    override fun onStatusChange(p0: String?) {
        print("onStatusChange: $p0")
    }

    override fun onPropertyChange(p0: String?) {
        print("onPropertyChange")
    }

    override fun onServerError(p0: Int, p1: String?) {
        print("onServerError")
    }

    fun print(message: String){
        Log.i("Lightstreamer listener", message)
    }

    override fun onListenEnd(p0: Subscription?) {
        print("onListenEnd")
    }

    override fun onItemUpdate(p0: ItemUpdate?) {
        print("onItemUpdated")
    }

    override fun onSubscription() {
        print("onSubscription")
    }

    override fun onEndOfSnapshot(p0: String?, p1: Int) {
        print("onEndOfSnapshot")
    }

    override fun onItemLostUpdates(p0: String?, p1: Int, p2: Int) {
        print("onItemLostUpdates")
    }

    override fun onSubscriptionError(p0: Int, p1: String?) {
        print("onSubscriptionError")
    }

    override fun onClearSnapshot(p0: String?, p1: Int) {
        print("onClearSnapshot")
    }

    override fun onCommandSecondLevelSubscriptionError(p0: Int, p1: String?, p2: String?) {
        print("onCommandSecondLevelSubscriptionError")
    }

    override fun onUnsubscription() {
        print("onUnsubscription")
    }

    override fun onCommandSecondLevelItemLostUpdates(p0: Int, p1: String?) {
        print("onCommandSecondLevelItemLostUpdates")
    }

    override fun onListenStart(p0: Subscription?) {
        print("onListenStart")
    }

    override fun onRealMaxFrequency(p0: String?) {
        print("onRealMaxFrequency")
    }
}
