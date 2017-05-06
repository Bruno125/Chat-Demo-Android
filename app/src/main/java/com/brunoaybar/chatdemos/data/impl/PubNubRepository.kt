package com.brunoaybar.chatdemos.data.impl

import com.brunoaybar.chatdemos.data.ChatRepository
import com.brunoaybar.chatdemos.data.Message
import io.reactivex.Flowable
import android.support.v4.app.NotificationCompat.getCategory
import io.reactivex.internal.util.NotificationLite.isError
import android.R.id.message
import com.brunoaybar.chatdemos.data.ChatUtils
import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.PNCallback
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.enums.PNStatusCategory
import com.pubnub.api.models.consumer.PNPublishResult
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import io.reactivex.BackpressureStrategy
import io.reactivex.subjects.PublishSubject
import java.util.*


class PubNubRepository() : ChatRepository {
    override val name: String get() = "PubNub Chat"
    val pubnub : PubNub
    private val messageSubject: PublishSubject<Message> = PublishSubject.create()

    init{
        val pnConfiguration = PNConfiguration()
        pnConfiguration.setSubscribeKey("sub-c-010f45da-2d2d-11e7-a696-0619f8945a4f")
        pnConfiguration.setPublishKey("pub-c-8aa801f5-05d1-42bd-9c6f-6ee9e829f37f")

        pubnub = PubNub(pnConfiguration)

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, status: PNStatus) {
                if (status.getCategory() === PNStatusCategory.PNUnexpectedDisconnectCategory) {
                    // This event happens when radio / connectivity is lost
                } else if (status.getCategory() === PNStatusCategory.PNConnectedCategory) {

                    // Connect event. You can do stuff like publish, and know you'll get it.
                    // Or just use the connected event to confirm you are subscribed for
                    // UI / internal notifications, etc

                } else if (status.getCategory() === PNStatusCategory.PNReconnectedCategory) {

                    // Happens as part of our regular operation. This event happens when
                    // radio / connectivity is lost, then regained.
                } else if (status.getCategory() === PNStatusCategory.PNDecryptionErrorCategory) {

                    // Handle messsage decryption error. Probably client configured to
                    // encrypt messages and on live data feed it received plain text.
                }
            }

            override fun message(pubnub: PubNub, result: PNMessageResult) {
                // Handle new message stored in message.message
                var data : String?
                try{
                    data = result.message.asString
                }catch (e: Exception){
                    data = result.message.toString().replace("\\\"","\"")
                }

                if(data != null){
                    val message = ChatUtils.parseMessage(data)
                    if (message != null){
                        messageSubject.onNext(message)
                    }
                }
            }

            override fun presence(pubnub: PubNub, presence: PNPresenceEventResult) {

            }
        })

        pubnub.subscribe().channels(Arrays.asList("chat")).execute()
    }

    override fun send(message: String) {
        pubnub.publish().channel("chat")
                .message(ChatUtils.createData(message))
                .async(object : PNCallback<PNPublishResult>() {
                    override fun onResponse(result: PNPublishResult?, status: PNStatus?) {
                        //do nothing
                }
        })
    }

    override fun receive(): Flowable<Message> {
        return messageSubject.toFlowable(BackpressureStrategy.BUFFER)
    }

}