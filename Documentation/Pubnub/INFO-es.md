# Firebase

## Introducción

Realizar una prueba de concepto en la que se pueda demostrar el uso de Lightstreamer entre dispositivos Android y iOS,
mediante la creación de un chat de dispositivos cercanos.

## Definición de conceptos

- __Pubnub__: es una implementación para servidores que permite múltiples formas de comunicación en tiempo real. Es lo suficientemente flexible para ser utilizaada en múltiples escenarios.

## Caracteristicas

Este proyecto utiliza la librería de Lighstreamer para Android.
- Version: 4.6.2
- Compatible con Java, Kotlin
- Min SDK: 14

## Diagramas

### Diagrama de secuencia

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/documentation/Documentation/Pubnub/Diagrams/Diagrama%20de%20secuencia_%20Pubnub.png)

### Diagrama de componentes

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/documentation/Documentation/Pubnub/Diagrams/Diagrama%20de%20componentes%20Pubnub.png)

### Diagrama de despliegue

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/documentation/Documentation/Pubnub/Diagrams/Diagrama%20de%20despliege-%20Pubnub.png)

## Implementación

1. En el archivo `build.gradle` del módulo [app](https://github.com/Bruno125/Communication-Demo-Android/blob/documentation/app/build.gradle), ir a la sección `dependencies`, y añadir la dependencia al proyecto: 
   
   `compile group: 'com.pubnub', name: 'pubnub-gson', version: '4.6.2'` (o la última versión disponible)

2. Crear un cliente Pubnub, indicando las llaves de subscripción y publicación

```kotlin

val pnConfiguration = PNConfiguration()
pnConfiguration.setSubscribeKey("YOUR_SUBSCRIBE_KEY")
pnConfiguration.setPublishKey("YOUR_PUBLISHER_KEY")

val pubnub = PubNub(pnConfiguration)

```

3. Suscribirse al canal , para poder realizar las operaciones que deseemos con el mensaje que recibamos

pubnub.addListener(object : SubscribeCallback() {
    override fun status(pubnub: PubNub, status: PNStatus) {
        ...
    }

    override fun message(pubnub: PubNub, result: PNMessageResult) {
        // Handle new message stored in message.message
        var data : String?
        try{
            data = result.message.asString
        }catch (e: Exception){
            data = result.message.toString().replace("\\\"","\"")
        }
    }

    override fun presence(pubnub: PubNub, presence: PNPresenceEventResult) {

    }
})

pubnub.subscribe().channels(Arrays.asList("your_channel_name")).execute()

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

En este proyecto, realizamos dicha implementación siguiendo el patrón Repository, invocando los métodos de subscripción / publicación en los lugares correspondientes. El resultado final es la clase [`PubnubChatRepository`](https://github.com/Bruno125/Communication-Demo-Android/blob/documentation/app/src/main/java/com/brunoaybar/chatdemos/data/impl/PubnubChatRepository.kt)

```kotlin
class PubNubRepository() : ChatRepository {
    override val name: String get() = "PubNub Chat"
    val pubnub : PubNub
    private val messageSubject: PublishSubject<Message> = PublishSubject.create()

    init{
        val pnConfiguration = PNConfiguration()
        pnConfiguration.setSubscribeKey("sub-c-010f45da-2d2d-11e7-a696-0619f8945a4f")
        pnConfiguration.setPublishKey("pub-c-8aa801f5-05d1-42bd-9c6f-6ee9e829f37f")

        pubnub = PubNub(pnConfiguration)

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, status: PNStatus) { }

            override fun message(pubnub: PubNub, result: PNMessageResult) {
                // Handle new message stored in message.message
                var data : String?
                try{
                    data = result.message.asString
                }catch (e: Exception){
                    data = result.message.toString().replace("\\\"","\"")
                }

                if(data != null){
                    val message = ChatUtils.parseMessage(data)
                    if (message != null){
                        messageSubject.onNext(message)
                    }
                }
            }

            override fun presence(pubnub: PubNub, presence: PNPresenceEventResult) { }
        })

        pubnub.subscribe().channels(Arrays.asList("chat")).execute()
    }

    override fun send(message: String) {
        pubnub.publish().channel("chat")
                .message(ChatUtils.createData(message))
                .async(object : PNCallback<PNPublishResult>() {
                    override fun onResponse(result: PNPublishResult?, status: PNStatus?) {
                        //do nothing
                }
        })
    }

    override fun receive(): Flowable<Message> {
        return messageSubject.toFlowable(BackpressureStrategy.BUFFER)
    }

}

```


## Bibliografía

- Pubnub. Página oficial de Pubnub. (https://www.pubnub.com/) Consulta:13 de Setiembre del 2017
