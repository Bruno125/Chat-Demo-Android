package com.brunoaybar.chatdemos.data

import android.content.Context
import com.brunoaybar.chatdemos.data.impl.*

enum class ChatProviders{
    FIREBASE,
    ABLY,
    P2PKIT,
    ZEROMQ,
    PUBNUB,
    LIGHTSTREAMER,
    RABBITMQ
}

class ChatFactory {
    companion object{

        fun create(context: Context, provider: ChatProviders): ChatRepository{
            when(provider){
                ChatProviders.ABLY          -> return createAbly()
                ChatProviders.FIREBASE      -> return createFirebase()
                ChatProviders.PUBNUB        -> return createPubNub()
                ChatProviders.LIGHTSTREAMER -> return createLightstreamer()
                ChatProviders.P2PKIT        -> return createP2PKit(context)
                ChatProviders.RABBITMQ      -> return createRabbitMQ()
                else                        -> return createAbly()
            }
        }

        private fun createAbly(): AblyChatRepository{
            return AblyChatRepository()
        }

        private fun createPubNub(): PubNubRepository{
            return PubNubRepository()
        }

        private fun createFirebase(): FirebaseChatRepository{
            return FirebaseChatRepository()
        }

        private fun createLightstreamer(): LightstreamerRepository{
            return LightstreamerRepository()
        }

        private fun createP2PKit(context: Context): P2PKitChatRepository{
            return P2PKitChatRepository(context)
        }

        private fun createRabbitMQ(): RabbitMQRepository{
            return RabbitMQRepository()
        }
    }
}