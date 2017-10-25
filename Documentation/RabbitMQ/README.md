# RabbitMQ

## Concepts

- __RabbitMQ__: RabbitMQ is lightweight and easy to deploy on premises and in the cloud. It supports multiple messaging protocols. RabbitMQ can be deployed in distributed and federated configurations to meet high-scale, high-availability requirements.

## Specification

This project uses the RabbitMQ Android SDK
- Version: 4.1.1
- Compatible with Java, Kotlin
- Min SDK: 14

## Diagrams

### Sequence diagram

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/master/Documentation/RabbitMQ/Diagrams/Sequence%20Diagram%20RabbitMQ.png)

### Component diagram

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/master/Documentation/RabbitMQ/Diagrams/Components%20Diagram%20RabbitMQ.png)

### Deployment diagram

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/master/Documentation/RabbitMQ/Diagrams/Deployment%20diagram%20RabbitMQ.png)

## Implementation

1. On the `build.gradle` file of the[app](https://github.com/Bruno125/Communication-Demo-Android/blob/documentation/app/build.gradle) module, go to the `dependencies` section and add the dependency to the project: 
   
   `compile 'com.rabbitmq:amqp-client:4.1.1'` (o la última versión disponible)

2. Create a RabbitMQ client, specifying the IP, port, and credentials to connect to the server.

```kotlin

private val HOST = "192.168.1.1"
private val PORT = 1234
private val URI = "amqp://$HOST:$PORT"

private val EXCHANGE_NAME = "chat"

private lateinit var connection : Connection

val factory = ConnectionFactory().apply {
    isAutomaticRecoveryEnabled = false
    host = HOST
    port = PORT
    username = "username"
    password = "password"
    setUri(URI)
}

```

3. Subscribe to the channel, to handle the messages that we receive.

```kotlin

init { subscribe(handler) }

val handler = object : Handler() {
    override fun handleMessage(msg: android.os.Message) {
        val message = msg.data.getString("msg")
        //handle the message as you wish
    }
}

fun subscribe(handler: Handler) {
    subscribeThread = Thread(Runnable {
        try{
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
        }catch (e: Exception){
            Handler().postDelayed({
                subscribe(handler)
            }, 5000)
        }
    })
    subscribeThread.start()

}

```

5. To send messages, call the `publish` method on the client object:

```kotlin
pubnub.publish().channel("chat")
                .message(ChatUtils.createData(message))
                .async(object : PNCallback<PNPublishResult>() {
                    override fun onResponse(result: PNPublishResult?, status: PNStatus?) {
                        //do something
                    }
                })
```

The final resut is the class [`RabbitMQRepository`](https://github.com/Bruno125/Communication-Demo-Android/blob/documentation/app/src/main/java/com/brunoaybar/chatdemos/data/impl/RabbitMQRepository.kt)

```kotlin
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

  val handler = object : Handler() {
      override fun handleMessage(msg: android.os.Message) {
          val message = msg.data.getString("msg")
          ChatUtils.parseMessage(message)?.let {
              messageSubject.onNext(it)
          }
      }
  }

  init {
      subscribe(handler)
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
          try{
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
          }catch (e: Exception){
              Handler().postDelayed({
                  subscribe(handler)
              }, 5000)
          }
      })
      subscribeThread.start()

  }

  //endregion


}

```


## Bibliography

- RabbitMQ. Official Page - RabbitMQ. (http://www.rabbitmq.com/) 2017
