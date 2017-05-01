package com.brunoaybar.chatdemos.data

import com.brunoaybar.chatdemos.data.impl.AblyChatRepository
import com.brunoaybar.chatdemos.data.impl.FirebaseChatRepository
import com.brunoaybar.chatdemos.data.impl.PubNubRepository

enum class ChatProviders{
    FIREBASE,
    ABLY,
    P2PKIT,
    ZEROMQ,
    PUBNUB
}

class ChatFactory {
    companion object{

        fun create(provider: ChatProviders): ChatRepository{
            when(provider){
                ChatProviders.ABLY          -> return createAbly()
                ChatProviders.FIREBASE      -> return createFirebase()
                ChatProviders.PUBNUB        -> return createPubNub()
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
    }
}