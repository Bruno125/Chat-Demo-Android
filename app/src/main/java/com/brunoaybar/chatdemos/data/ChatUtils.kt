package com.brunoaybar.chatdemos.data

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ChatUtils{
    companion object{
        val dateFormatter = SimpleDateFormat("y-MM-dd H:m:ss.SSSS")

        fun createSendMessage(content: String) : SendMessage{
            val timestamp = dateFormatter.format(Date())
            return SendMessage(timestamp,content)
        }

        fun createData(content: String) : String{
            val info = createSendMessage(content)
            return Gson().toJson(info,SendMessage::class.java)
        }

        fun parseMessage(data: String) : Message?{
            val info = Gson().fromJson(data,SendMessage::class.java)
            return parseMessage(info)

        }

        fun parseMessage(data: SendMessage): Message?{
            var content = "--"; var delay = "--"; var size = ""
            if(data.value.isNotEmpty()){
                content = data.value
                size = "${content.toByteArray().size} bytes"
            }

            if (data.timestamp.isNotEmpty()){
                dateFormatter.parse(data.timestamp).let { sendDate ->
                    val now = Date()
                    delay = "${now.time - sendDate.time} ms"
                }
            }
            return Message(content,delay,size)
        }

    }

    data class SendMessage(@SerializedName("timestamp") val timestamp: String = "",
                           @SerializedName("value") val value: String = "")

}