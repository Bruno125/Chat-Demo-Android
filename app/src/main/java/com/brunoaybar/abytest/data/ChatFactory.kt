package com.brunoaybar.abytest.data

import com.brunoaybar.abytest.data.impl.AblyChatRepository

enum class ChatProviders{
    FIREBASE,
    ABLY,
    P2PKIT,
    ZEROMQ
}

class ChatFactory {
    companion object{

        fun create(provider: ChatProviders): ChatRepository{
            when(provider){
                ChatProviders.ABLY          -> return createAbly()
                else                        -> return createAbly()
            }
        }

        private fun createAbly(): AblyChatRepository{
            return AblyChatRepository()
        }

    }
}