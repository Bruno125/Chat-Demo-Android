package com.brunoaybar.abytest.data

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ChatUtils{
    companion object{
        val KEY_TIMESTAMP = "timestamp"
        val KEY_VALUE = "value"
        val dateFormatter = SimpleDateFormat("y-MM-dd H:m:ss.SSSS")

        fun createData(content: String) : String{
            var json = JSONObject()
            json.put(KEY_VALUE,content)

            val timestamp = dateFormatter.format(Date())
            json.put(KEY_TIMESTAMP,timestamp)

            return json.toString()

        }

        fun parseMessage(data: String) : Message?{
            var content = "--"; var delay = "--"; var size = ""

            try {
                JSONObject(data).let { info ->
                    info.getString(KEY_VALUE).let {
                        content = it
                        size = "${it.toByteArray().size} bytes"
                    }

                    info.getString(KEY_TIMESTAMP).let {
                        dateFormatter.parse(it).let { sendDate ->
                            val now = Date()
                            delay = "${now.time - sendDate.time} ms"
                        }
                    }
                }
                return Message(content,delay,size)
            }catch (e: Exception){
                return null
            }
        }
    }


}