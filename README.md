# Communication-Demo-Android

This project was developed considering that no matter the inner behavior of any technology that allows us to establish realtime communication between mobile devices, the most basic requirements they must fullfill are:

- Sending messages
- Receiving messages

## Defining the interface

Considering this, we create an interface `ChatRepository` that defines two methods, one to send and another to receive data:

```kotlin
interface ChatRepository{
    fun send(message: String)
    fun receive(): Flowable<Message>
}
```

The `send` method allows the client to send an string of text, for itto be distributed to the other clients.

The `receive` method returns `Flowable<Message>`. This represents an stream of messages, which will allow us to be notified each time another device emits a message. The `Flowable` class comes from the RxJava 2 library.

Once the interface is defined, each technology will implement it using it's own dependencies. We have created 5 implementations:

- AblyRepository
- FirebaseRepository
- PubnunbRepository
- LightstreamerRepository
- RabbitMQRepository

## Build flavors

We'll use the same project to generate the Android apps for each technology. This way, we'll make sure that each one of them is executing on the same scenario.

The first step to setup the project es to go to the build.gradle file and define every flavor.

```
 productFlavors{
        general{
            resValue "string", "app_name", "Demo Chat"
        }

        firebase{
            applicationIdSuffix ".firebase"
            versionNameSuffix "-firebase"
            resValue "string", "app_name", "Demo Firebase"
        }

        ably{
            applicationIdSuffix ".ably"
            versionNameSuffix "-ably"
            resValue "string", "app_name", "Demo Ably"
        }

        pubnub{
            applicationIdSuffix ".pubnub"
            versionNameSuffix "-pubnub"
            resValue "string", "app_name", "Demo Pubnub"
        }

        lightstreamer{
            applicationIdSuffix ".lightstreamer"
            versionNameSuffix "-lightstreamer"
            resValue "string", "app_name", "Demo Lightstreamer"
        }

        p2p{
            applicationIdSuffix ".p2p"
            versionNameSuffix "-p2p"
            resValue "string", "app_name", "Demo P2PKit"
        }

        rabbitmq{
            applicationIdSuffix ".rabbitmq"
            versionNameSuffix "-rabbitmq"
            resValue "string", "app_name", "Demo RabbitMQ"
        }

    }

```

## Injecting the Repository

We'll create a folder for each technology, at the same level of the module `app/src`. Inside it, we'll create the class`ChatInjection`, which will return the implementation that match the technology. For example:

```
class ChatInjection{
    companion object{
        fun getRepo(context: Context): ChatRepository = ChatFactory.create(context,ChatProviders.ABLY)
    }
}
```

