# Ably

## Concepts

- __Ably:__  a realtime data delivery platform providing developers everything they need to create, deliver and manage complex projects. Provides libraries for:
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

## Specifications

This project uses Ably's Android SDK
- Version: 1.0.0
- Compatible with Java, Kotlin
- Min SDK: 14

## Diagrams

### Sequence diagram

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/documentation/Documentation/Ably/Diagrams/Diagrama%20de%20secuencia-%20Ably.png)

### Components diagram

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/documentation/Documentation/Ably/Diagrams/Diagrama%20de%20componentes%20Ably.png)

### Deployment diagram

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/documentation/Documentation/Ably/Diagrams/Diagrama%20de%20despliege-%20Ably.png)

## Implementation

1. On the `build.gradle` file in the module[app](https://github.com/Bruno125/Communication-Demo-Android/blob/documentation/app/build.gradle), go to `dependencies` section and add:

`compile 'io.ably:ably-android:1.0.0'`

2. Configure Ably using their API key, and specifying the name of the channel which you'll create to send the messages.

```kotlin

private val realtime: AblyRealtime = AblyRealtime("YOUR_ABLY_KEY")
private val channel = realtime.channels.get("YOUR_CHANNEL_NAME")

```

3. Subscribe to the channel, to handle the messages that we receive.

```kotlin
channel.subscribe {
    val content = it.data.toString()
    //handle the message as you wish
}
```

4. To send a message, the the `publish` method on the `channel` object:

```kotlin
channel.publish("send","your message")
```

The final resut is the class [`AblyChatRepository`](https://github.com/Bruno125/Communication-Demo-Android/blob/documentation/app/src/main/java/com/brunoaybar/chatdemos/data/impl/AblyChatRepository.kt), which implements the ChatRepository interface

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


## Bibliography

- Ably. Official page. (https://www.ably.io/) 2017

- Ably. Ably's Android SDK integration tutorial. (https://www.ably.io/tutorials/publish-subscribe#lang-android) 2017
