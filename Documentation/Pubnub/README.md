# Pubnub

## Concepts

- __Pubnub__: Platform that allows you to send and receive messages cross-platform and cross-device with robust yet simple APIs for global publish/subscribe messaging.

## Specifications

This project uses the Pubnub Android SDK
- Version: 4.6.2
- Compatible with Java, Kotlin
- Min SDK: 14

## Diagrams

### Sequence diagram

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/master/Documentation/Pubnub/Diagrams/Sequence%20Diagram%20Pubnub.png)

### Components diagram

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/master/Documentation/Pubnub/Diagrams/Components%20Diagram%20Pubnub.png)

### Deployment diagram

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/master/Documentation/Pubnub/Diagrams/Deployment%20Diagram%20Pubnub.png)

## Implementation

1. On the `build.gradle` file of the [app](https://github.com/Bruno125/Communication-Demo-Android/blob/documentation/app/build.gradle) module, go to the `dependencies` section, and add the dependency to the project: 
   
   `compile group: 'com.pubnub', name: 'pubnub-gson', version: '4.6.2'` (o la última versión disponible)

2. Create a Pubnub client, specifying the subscription and publishing keys.

```kotlin

val pnConfiguration = PNConfiguration()
pnConfiguration.setSubscribeKey("YOUR_SUBSCRIBE_KEY")
pnConfiguration.setPublishKey("YOUR_PUBLISHER_KEY")

val pubnub = PubNub(pnConfiguration)

```

3. Subscribe to the channel, to handle the messages that we receive.

```kotlin
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

4. To send messages, call the `publish` method on the client object:

```kotlin
pubnub.publish().channel("chat")
                .message(ChatUtils.createData(message))
                .async(object : PNCallback<PNPublishResult>() {
                    override fun onResponse(result: PNPublishResult?, status: PNStatus?) {
                        //do something
                    }
                })
```

The final resut is the class [`PubnubChatRepository`](https://github.com/Bruno125/Communication-Demo-Android/blob/documentation/app/src/main/java/com/brunoaybar/chatdemos/data/impl/PubnubChatRepository.kt)

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


## Bibliography

- Pubnub. Official Page. (https://www.pubnub.com/) 2017
