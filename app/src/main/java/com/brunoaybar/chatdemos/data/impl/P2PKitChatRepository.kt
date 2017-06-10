package com.brunoaybar.chatdemos.data.impl

import android.content.Context
import android.util.Log
import ch.uepaa.p2pkit.P2PKit
import ch.uepaa.p2pkit.P2PKitStatusListener
import ch.uepaa.p2pkit.StatusResult
import ch.uepaa.p2pkit.discovery.DiscoveryListener
import ch.uepaa.p2pkit.discovery.DiscoveryPowerMode
import ch.uepaa.p2pkit.discovery.Peer
import com.brunoaybar.chatdemos.data.ChatRepository
import com.brunoaybar.chatdemos.data.ChatUtils
import com.brunoaybar.chatdemos.data.Message
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject

class P2PKitChatRepository(context: Context) : ChatRepository,
        P2PKitStatusListener by StatusListenerLogger,
        DiscoveryListener by DiscoveryListenerLogger {

    override val name: String get() = "P2PKit"

    private val API_KEY = "3297b6f470cb422ab20b900b786d4d88"
    private val messageSubject: PublishSubject<Message> = PublishSubject.create()

    init {
        P2PKit.enable(context, API_KEY, this)
        P2PKit.startDiscovery("Helloooo".toByteArray(), DiscoveryPowerMode.HIGH_PERFORMANCE, this)
    }

    override fun send(message: String) {
        ChatUtils.createData(message).let {
            P2PKit.pushDiscoveryInfo(it.toByteArray())
        }
    }

    override fun receive(): Flowable<Message> {
        return messageSubject.toFlowable(BackpressureStrategy.BUFFER)
    }

    override fun onPeerUpdatedDiscoveryInfo(peer: Peer?) {
        val message = peer?.discoveryInfo?.toString()
        Log.d("DiscoveryListener", "Peer updated: " + peer?.peerId + " with new info: " + message);
        if (message == null || message.isEmpty())
            return
        ChatUtils.parseMessage(message)?.let {
            messageSubject.onNext(it)
        }
    }
}

private object DiscoveryListenerLogger : DiscoveryListener{
    override fun onPeerUpdatedDiscoveryInfo(peer: Peer?) {
        TODO("not implemented")
    }

    override fun onPeerLost(peer: Peer?) {
        print("onPeerLost: $peer")
    }

    override fun onStateChanged(state: Int) {
        print("onStateChanged: $state")
    }

    override fun onProximityStrengthChanged(peer: Peer?) {
        print("onProximityStrengthChanged: $peer")
    }

    override fun onPeerDiscovered(peer: Peer?) {
        print("onPeerDiscovered: $peer")
    }

    fun print(message: String){ Log.i("P2PKit Discovery Logger", message) }
}

private object StatusListenerLogger : P2PKitStatusListener{

    override fun onEnabled() { print("onEnable")}

    override fun onException(throwable: Throwable?) { print("onException") }

    override fun onError(result: StatusResult?) { print("onError: $result") }

    override fun onDisabled() { print("onDisabled") }

    fun print(message: String){ Log.i("P2PKit Status Logger", message) }

}