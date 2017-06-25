package com.brunoaybar.chatdemos.data.impl

import android.util.Log
import com.brunoaybar.chatdemos.data.ChatRepository

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.LinkedBlockingDeque
import android.os.Bundle
import android.os.Handler
import com.brunoaybar.chatdemos.data.ChatUtils
import com.brunoaybar.chatdemos.data.Message
import com.rabbitmq.client.*
import java.io.IOException
import com.rabbitmq.client.ConnectionFactory

class RabbitMQRepository() : ChatRepository{
    override val name: String get() = "RabbitMQ"

    private val HOST = "10.11.80.94"
    private val PORT = 5673
    private val URI = "amqp://$HOST:$PORT"

    private val EXCHANGE_NAME = "chat"

    private lateinit var connection : Connection

    val factory = ConnectionFactory().apply {
        isAutomaticRecoveryEnabled = false
        host = HOST
        port = PORT
        username = "mobile"
        password = "1234"
        setUri(URI)
    }

    init {
        subscribe(object : Handler() {
            override fun handleMessage(msg: android.os.Message) {
                val message = msg.data.getString("msg")
                ChatUtils.parseMessage(message)?.let {
                    messageSubject.onNext(it)
                }
            }
        })
    }

    private val messageSubject: PublishSubject<Message> = PublishSubject.create()
    override fun receive(): Flowable<Message> {
        return messageSubject.toFlowable(BackpressureStrategy.BUFFER)
    }

    val queue = LinkedBlockingDeque<String>()
    override fun send(message: String) {
        publishMessage(ChatUtils.createData(message))
    }

    //region //Publish

    private fun publishMessage(message: String){
        val channel = connection.createChannel()

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout")

        channel.basicPublish(EXCHANGE_NAME, "", null, message.toByteArray())
        println(" [x] Sent '$message'")

        channel.close()

        try {
            Log.d("RabbitMQ","[q] " + message);
            queue.putLast(message);
        } catch (e: Exception) {

        }
    }

    //endregion

    //region //Subscribe

    private lateinit var subscribeThread: Thread

    fun subscribe(handler: Handler) {
        subscribeThread = Thread(Runnable {
            connection = factory.newConnection()
            val channel = connection.createChannel()

            channel.exchangeDeclare(EXCHANGE_NAME, "fanout")
            val queueName = channel.queueDeclare().queue
            channel.queueBind(queueName, EXCHANGE_NAME, "")

            val consumer = object : DefaultConsumer(channel) {
                @Throws(IOException::class)
                override fun handleDelivery(consumerTag: String, envelope: Envelope,
                                            properties: AMQP.BasicProperties, body: ByteArray) {
                    val message = String(body)
                    println(" [x] Received '$message'")
                    val msg = handler.obtainMessage()
                    val bundle = Bundle()
                    bundle.putString("msg", message)
                    msg.data = bundle
                    handler.sendMessage(msg)
                }
            }

            channel.basicConsume(queueName, true, consumer)
        })
        subscribeThread.start()

    }

    //endregion


}