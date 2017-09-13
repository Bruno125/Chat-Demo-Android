# Communication-Demo-Android

Este proyecto se desarrolló teniendo en consideración que sin importar el comportamiento interno de cualquier tecnología que nos permita realizar una comunicación en tiempo real entre dispositivos móviles, las funcionalidades más básicas que cualquier de estas debería cumplir son:

- Envío de mensajes
- Recepción de mensajes

## Definiendo la interfaz

Teniendo eso cuenta, creamos la interfaz `ChatRepository` que define dos funciones, una para el envío y otra para la recepción:

```kotlin
interface ChatRepository{
    fun send(message: String)
    fun receive(): Flowable<Message>
}
```

El método `send` permite al cliente enviar una cadena de texto, para que sea distribuido a los demás dispositivos.

El método `receive` devuelve un tipo de dato `Flowable<Message>`. Esto representa un flujo de mensajes, lo cual nos permite detectar cada vez que otro dispositivo nos haya enviado un mensaje. Para poder manejar el flujo de mensajes con `Flowable`, hacemos uso de la librería RxJava.

Una vez definida la interfaz, la implementación para cada tecnología extenderá de esta interfaz. Hemos creado cinco implementaciones:

- AblyRepository
- FirebaseRepository
- PubnunbRepository
- LightstreamerRepository
- RabbitMQRepository

## Creando las variantes

Usaremos este mismo proyecto para generar las aplicaciones Android de cada tecnologías, garantizando que cada una de ellas se esté ejecutando bajo un escenario común.

El primer paso es entrar al archivo build.gradle y definir cada una de las variaciones que podría tener el app

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

## Inyectando los repositorios

Creamos una carpeta para cada una de las tecnologías, al mismo nivel del módulo `app/src`. Dentro de estas, creamos la clase `ChatInjection`, la cual devolverá la implementación adecuada del repositorio. Por ejemplo:

```
class ChatInjection{
    companion object{
        fun getRepo(context: Context): ChatRepository = ChatFactory.create(context,ChatProviders.ABLY)
    }
}
```

