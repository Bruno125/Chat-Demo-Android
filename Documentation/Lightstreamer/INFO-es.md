# Firebase

## Introducción

Realizar una prueba de concepto en la que se pueda demostrar el uso de Lightstreamer entre dispositivos Android y iOS,
mediante la creación de un chat de dispositivos cercanos.

## Definición de conceptos

- __Lighstreamer:__ es una implementación para servidores que permite múltiples formas de comunicación en tiempo real. Es lo suficientemente flexible para ser utilizaada en múltiples escenarios.

## Caracteristicas

Este proyecto utiliza la librería de Lighstreamer para Android.
- Version: 3.0.0
- Compatible con Java, Kotlin
- Min SDK: 14

## Diagramas

### Diagrama de secuencia

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/documentation/Documentation/Firebase/Diagrams/Diagrama%20de%20secuencia-%20Firebase.png)

### Diagrama de componentes

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/documentation/Documentation/Firebase/Diagrams/Diagrama%20de%20componentes%20Firebase.png)

### Diagrama de despliegue

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/documentation/Documentation/Firebase/Diagrams/Diagrama%20de%20despliegue.png)

## Implementación

1. En el archivo `build.gradle` del módulo [app](https://github.com/Bruno125/Communication-Demo-Android/blob/documentation/app/build.gradle), ir a la sección `dependencies`, y añadir la dependencia al proyecto: 
   
   `compile 'com.lightstreamer:ls-android-client:3.0.0'` (o la última versión disponible)

2. Crear un cliente Lighstreamer, indicando el modo, el item, el adpter y los elementos que se transmitirán.

```kotlin

val serverAddress = "http://YOUR_SERVER_IP:YOUR_SERVER_PORT"
val subscription = Subscription("DISTINCT","chat_room", arrayOf("your","variables")).apply {
    addListener(this@LightstreamerRepository)
    dataAdapter = "CHAT_ROOM"
    requestedSnapshot = "yes"
}
val lsClient = LightstreamerClient(serverAddress,"CHAT").apply {
    connectionDetails.serverAddress = serverAddress
    connect()
    subscribe(subscription)
    addListener(this@LightstreamerRepository)
}


```

3. Opcionalmente, implementar la interfaz `ClientListener` para detectar eventos relacionados a la conexión.

4. Suscribirse al canal implementando la interfaz `SubscriptionListener`, para poder realizar las operaciones que deseemos con el mensaje que recibamos

```kotlin
override fun onItemUpdate(p0: ItemUpdate?) {
    try {
        if(p0 != null && p0.fields != null){
            val message = p0.fields["message"]
            //handle the message as you wish
        }else{
            print("Algo es nulo")
        }


        /*p0?.fields?.get("message")?.let{
            val message = ChatUtils.parseMessage(it)
            messageSubject.onNext(message)
        }*/

    }catch (e: Exception){
        print("Exc: $e")
    }

}

```

5. Para enviar mensajes, invocar el método `sendMessage` sobre el objeto cliente (indicar el nombre del chat):

```kotlin
lsClient.sendMessage("CHAT| your message")
```

En este proyecto, realizamos dicha implementación siguiendo el patrón Repository, invocando los métodos de subscripción / publicación en los lugares correspondientes. El resultado final es la clase [`LightstreamerChatRepository`](https://github.com/Bruno125/Communication-Demo-Android/blob/documentation/app/src/main/java/com/brunoaybar/chatdemos/data/impl/LighstreamerChatRepository.kt)

```kotlin
class LightstreamerRepository : ChatRepository,
        SubscriptionListener by LightstreamerListenerLogger,
        ClientListener by LightstreamerListenerLogger{
    override val name: String get() = "Demo Lightstreamer"

    val serverAddress = "http://10.11.80.88:8080"
    val subscription = Subscription("DISTINCT","chat_room", arrayOf("message", "raw_timestamp", "IP","nick")).apply {
        addListener(this@LightstreamerRepository)
        dataAdapter = "CHAT_ROOM"
        requestedSnapshot = "yes"
    }
    val lsClient = LightstreamerClient(serverAddress,"CHAT").apply {
        connectionDetails.serverAddress = serverAddress
        connect()
        subscribe(subscription)
        addListener(this@LightstreamerRepository)
    }

    private val messageSubject: PublishSubject<Message> = PublishSubject.create()

    override fun send(message: String) {
        ChatUtils.createData(message).let {
            lsClient.sendMessage("CHAT| $it")
        }
    }

    override fun receive(): Flowable<Message> {
        return messageSubject.toFlowable(BackpressureStrategy.BUFFER)
    }

    override fun onItemUpdate(p0: ItemUpdate?) {
        try {
            if(p0 != null && p0.fields != null){
                val message = p0.fields["message"]
                if (message != null){
                    val entry = ChatUtils.parseMessage(message)
                    messageSubject.onNext(entry)
                }else{
                    print("Message es null")
                }
            }else{
                print("Algo es nulo")
            }


            /*p0?.fields?.get("message")?.let{
                val message = ChatUtils.parseMessage(it)
                messageSubject.onNext(message)
            }*/

        }catch (e: Exception){
            print("Exc: $e")
        }

    }

}

object LightstreamerListenerLogger : SubscriptionListener, ClientListener{
    //implementing all the methods to log each event...
}

```


## Bibliografía

- Lighstreamer. Página oficial de Lighstreamer. (http://www.lightstreamer.com/) Consulta:13 de Setiembre del 2017
