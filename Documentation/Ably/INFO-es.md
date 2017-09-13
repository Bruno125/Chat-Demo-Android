# Ably

## Introducción

Realizar una prueba de concepto en la que se pueda demostrar el uso de Ably entre dispositivos Android y iOS,
mediante la creación de un chat de dispositivos cercanos

## Definición de conceptos

- __Ably:__ plataforma de envío de información en tiempo real. Provee librerías listas para ser utilizadas por las aplicaciones cliente (Ably, 2017). Actualemente está disponible para:
  - Javascript
  - Android
  - iOS (Objective-C / Swift)
  - .NET
  - NodeJS
  - PHP
  - Java
  - Ruby
  - Python
  - Go

## Caracteristicas

Este proyecto utiliza la librería de Ably para Android.
- Version: 1.0.0
- Compatible con Java, Kotlin
- Min SDK: 14

## Diagramas

### Diagrama de secuencia

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/documentation/Documentation/Ably/Diagrams/Diagrama%20de%20secuencia-%20Ably.png)

### Diagrama de componentes

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/documentation/Documentation/Ably/Diagrams/Diagrama%20de%20componentes%20Ably.png)

### Diagrama de despliegue

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/documentation/Documentation/Ably/Diagrams/Diagrama%20de%20despliege-%20Ably.png)

## Implementación

1. En el archivo `build.gradle` del módulo [app](https://github.com/Bruno125/Communication-Demo-Android/blob/documentation/app/build.gradle), ir a la sección `dependencies` y añadir la dependencia al proyecto:

`compile 'io.ably:ably-android:1.0.0'`

2. Configurar Ably usando la llave que ellos te proveen, y el nombre del canal por el cual transmitirás los mensajes.

```kotlin

private val realtime: AblyRealtime = AblyRealtime("YOUR_ABLY_KEY")
private val channel = realtime.channels.get("YOUR_CHANNEL_NAME")

```

3. Suscribirse al canal, para poder realizar las operaciones que deseemos con el mensaje que recibamos

```kotlin
channel.subscribe {
    val content = it.data.toString()
    //handle the message as you wish
}
```

4. Para enviar mensajes, invocar el método `publish` sobre el `channel` que creamos:

```kotlin
channel.publish("send","your message")
```

En este proyecto, realizamos dicha implementación siguiendo el patrón Repository, invocando los métodos de subscripción / publicación en los lugares correspondientes. El resultado final es la clase [`AblyChatRepository`](https://github.com/Bruno125/Communication-Demo-Android/blob/documentation/app/src/main/java/com/brunoaybar/chatdemos/data/impl/AblyChatRepository.kt)

```kotlin
class AblyChatRepository() : ChatRepository{
    override val name: String get() = "Ably Chat"
    private val realtime: AblyRealtime = AblyRealtime("YOUR_ABLY_KEY")
    private val channel = realtime.channels.get("YOUR_ABLY_CHANNEL")
    private val messageSubject: PublishSubject<Message> = PublishSubject.create()

    init {
        channel.subscribe {
            val content = it.data.toString()
            val message = ChatUtils.parseMessage(content)
            messageSubject.onNext(message)
        }
    }

    override fun send(message: String) {
        channel.publish("send",ChatUtils.createData(message))
    }

    override fun receive(): Flowable<Message> {
        return messageSubject.toFlowable(BackpressureStrategy.BUFFER)
    }

}
```


## Bibliografía

- Ably. Página oficial de la empresa Ably. (https://www.ably.io/) Consulta:13 de Setiembre del 2017

- Ably. Tutorial para la integracin de Ably en un proyecto Android. (https://www.ably.io/tutorials/publish-subscribe#lang-android) Consulta:13 de Setiembre del 2017
