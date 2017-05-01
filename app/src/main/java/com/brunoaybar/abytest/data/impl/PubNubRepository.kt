package com.brunoaybar.abytest.data.impl

import com.brunoaybar.abytest.data.ChatRepository
import com.brunoaybar.abytest.data.Message
import io.reactivex.Flowable
import android.support.v4.app.NotificationCompat.getCategory
import io.reactivex.internal.util.NotificationLite.isError
import android.R.id.message
import com.brunoaybar.abytest.data.ChatUtils
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
        pnConfiguration.setSubscribeKey("sub-c-f70f1c40-2d2c-11e7-9488-0619f8945a4f")
        pnConfiguration.setPublishKey("pub-c-e1c91660-d62c-41a5-88c3-9b9e2d71ce3a")

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
                result.message.asString.let { data ->
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