package com.brunoaybar.chatdemos.data.impl

import android.util.Log
import com.brunoaybar.chatdemos.data.ChatRepository

import com.rabbitmq.client.ConnectionFactory
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.LinkedBlockingDeque
import android.os.Bundle
import android.os.Handler
import com.brunoaybar.chatdemos.data.ChatUtils
import com.rabbitmq.client.QueueingConsumer
import com.brunoaybar.chatdemos.data.Message


class RabbitMQRepository() : ChatRepository{
    override val name: String get() = "RabbitMQ"

    private val HOST = "10.11.80.78"
    private val PORT = 5673
    private val URI = "amqp://$HOST:$PORT"

    val factory = ConnectionFactory().apply {
        isAutomaticRecoveryEnabled = false
        host = HOST
        port = PORT
        username = "mobile"
        password = "1234"
        setUri(URI)
    }

    init {
        publishToAMQP()

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
        try {
            Log.d("RabbitMQ","[q] " + message);
            queue.putLast(message);
        } catch (e: Exception) {

        }
    }

    private lateinit var publishThread: Thread
    fun publishToAMQP() {
        publishThread = Thread(Runnable {
            while (true) {
                try {
                    val connection = factory.newConnection()
                    val ch = connection.createChannel()
                    ch.confirmSelect()

                    while (true) {
                        val message = queue.takeFirst()
                        try {
                            ch.basicPublish("amq.fanout", "chat", null, message.toByteArray())
                            Log.d("", "[s] " + message)
                            ch.waitForConfirmsOrDie()
                        } catch (e: Exception) {
                            Log.d("", "[f] " + message)
                            queue.putFirst(message)
                            throw e
                        }

                    }
                } catch (e: InterruptedException) {
                    break
                } catch (e: Exception) {
                    Log.d("", "Connection broken: " + e.javaClass.name)
                    try {
                        Thread.sleep(5000) //sleep and then try again
                    } catch (e1: InterruptedException) {
                        break
                    }

                }

            }
        })
        publishThread.start()
    }

    //endregion

    //region //Subscribe

    private lateinit var subscribeThread: Thread

    fun subscribe(handler: Handler) {
        subscribeThread = Thread(Runnable {
            while (true) {
                try {
                    val connection = factory.newConnection()
                    val channel = connection.createChannel()
                    channel.basicQos(1)
                    val q = channel.queueDeclare()
                    channel.queueBind(q.queue, "amq.fanout", "chat")
                    val consumer = QueueingConsumer(channel)
                    channel.basicConsume(q.queue, true, consumer)

                    while (true) {
                        val delivery = consumer.nextDelivery()
                        val message = String(delivery.body)
                        Log.d("", "[r] " + message)
                        val msg = handler.obtainMessage()
                        val bundle = Bundle()
                        bundle.putString("msg", message)
                        msg.setData(bundle)
                        handler.sendMessage(msg)
                    }
                } catch (e: InterruptedException) {
                    break
                } catch (e1: Exception) {
                    Log.d("", "Connection broken: " + e1.javaClass.name)
                    try {
                        Thread.sleep(5000) //sleep and then try again
                    } catch (e: InterruptedException) {
                        break
                    }

                }

            }
        })
        subscribeThread.start()
    }

    //endregion


}