  # RabbitMQ

## Introducción

Realizar una prueba de concepto en la que se pueda demostrar el uso de Lightstreamer entre dispositivos Android y iOS,
mediante la creación de un chat de dispositivos cercanos.

## Definición de conceptos

- __RabbitMQ__: es una implementación para servidores que permite múltiples formas de comunicación en tiempo real. Es lo suficientemente flexible para ser utilizaada en múltiples escenarios.

## Caracteristicas

Este proyecto utiliza la librería de RabbitMQ para Android.
- Version: 4.1.1
- Compatible con Java, Kotlin
- Min SDK: 14

## Diagramas

### Diagrama de secuencia

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/documentation/Documentation/RabbitMQ/Diagrams/Diagrama%20de%20secuencia-%20RabbitMQ.png)

### Diagrama de componentes

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/documentation/Documentation/RabbitMQ/Diagrams/Diagrama%20de%20componentes%20RabbitMQ.png)

### Diagrama de despliegue

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/documentation/Documentation/RabbitMQ/Diagrams/Diagrama%20de%20despliege-%20RabbitMQ.png)

## Implementación

1. En el archivo `build.gradle` del módulo [app](https://github.com/Bruno125/Communication-Demo-Android/blob/documentation/app/build.gradle), ir a la sección `dependencies`, y añadir la dependencia al proyecto: 
   
   `compile 'com.rabbitmq:amqp-client:4.1.1'` (o la última versión disponible)

2. Crear un cliente RabbitMQ, indicando la IP, puerto, y credenciales para conecctarse al servidor.

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

3. Suscribirse al canal , para poder realizar las operaciones que deseemos con el mensaje que recibamos

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

5. Para enviar mensajes, invocar el método `publish` sobre el objeto cliente:

```kotlin
pubnub.publish().channel("chat")
                .message(ChatUtils.createData(message))
                .async(object : PNCallback<PNPublishResult>() {
                    override fun onResponse(result: PNPublishResult?, status: PNStatus?) {
                        //do something
                    }
                })
```

En este proyecto, realizamos dicha implementación siguiendo el patrón Repository, invocando los métodos de subscripción / publicación en los lugares correspondientes. El resultado final es la clase [`RabbitMQRepository`](https://github.com/Bruno125/Communication-Demo-Android/blob/documentation/app/src/main/java/com/brunoaybar/chatdemos/data/impl/RabbitMQRepository.kt)

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


## Bibliografía

- RabbitMQ. Página oficial de - RabbitMQ. (http://www.rabbitmq.com/) Consulta:13 de Setiembre del 2017
