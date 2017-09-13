# Firebase

## Introducción

Realizar una prueba de concepto en la que se pueda demostrar el uso de Firebase entre dispositivos Android y iOS,
mediante la creación de un chat de dispositivos cercanos.

## Definición de conceptos

- __Firebase:__ plataforma de Google, que ofrece a los desarrolladores de aplicaciones móviles / web una suite de herramientas para el desarrollo, mantenimiento y monitoreo.
- __Firebase Database__: una de las tecnologías ofrecidas por Firebase. Te permite almacenar información en una base de datos no relacional. Los clientes de Firebase Realtime Database pueden suscribirse a esta base de datos, para ser notificados cada vez que ocurra algún cambio sobre esta. 

## Caracteristicas

Este proyecto utiliza la librería de Firebase para Android.
- Version: 10.0.2
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

1. En el archivo `build.gradle` del módulo [app](https://github.com/Bruno125/Communication-Demo-Android/blob/documentation/app/build.gradle), ir a la sección `dependencies`:

   - Añadir la dependencia al proyecto: 
   
   `compile 'com.google.firebase:firebase-database:10.2.0'` (o la última versión disponible)
   - Agregar el archivo google-services.json que se obtiene desde la consola de Firebase, e incluirlo en el módulo `app`del proyecto
   - Al final del archivo, agregar el plugin de google services:
   
   `apply plugin: 'com.google.gms.google-services'`

2. Crear una referencia al base de datos, indicando el nombre del nodo del cual se desea publicar / recibir mensajes.

```kotlin

private val mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference.child("chat")

```

3. Suscribirse al canal, para poder realizar las operaciones que deseemos con el mensaje que recibamos

```kotlin
val childEventListener = object : ChildEventListener {
    override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
        val message = dataSnapshot.getValue(..)
        //handle the message as you wish
    }

    override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {

    }

    override fun onChildRemoved(dataSnapshot: DataSnapshot) {}

    override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {}

    override fun onCancelled(databaseError: DatabaseError) {

    }
}
mDatabase.addChildEventListener(childEventListener)
```

4. Para enviar mensajes, invocar el método `push` sobre la referencia a la base de datos:

```kotlin
mDatabase.push().setValue("your message")
```

En este proyecto, realizamos dicha implementación siguiendo el patrón Repository, invocando los métodos de subscripción / publicación en los lugares correspondientes. El resultado final es la clase [`AblyChatRepository`](https://github.com/Bruno125/Communication-Demo-Android/blob/documentation/app/src/main/java/com/brunoaybar/chatdemos/data/impl/FirebaseChatRepository.kt)

```kotlin
class FirebaseChatRepository() : ChatRepository {
    override val name: String get() = "Firebase"

    private val mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference.child("chat")
    private val messageSubject: PublishSubject<Message> = PublishSubject.create()

    init {
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val message = ChatUtils.parseMessage(dataSnapshot.getValue(ChatUtils.SendMessage::class.java))
                if (message != null){
                    messageSubject.onNext(message)
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        mDatabase.addChildEventListener(childEventListener)
    }

    override fun send(message: String) {
        ChatUtils.createSendMessage(message).let {
            mDatabase.push().setValue(it)
        }
    }

    override fun receive(): Flowable<Message> {
        return messageSubject.toFlowable(BackpressureStrategy.BUFFER)
    }
}
```


## Bibliografía

- Firebase. Página oficial de Firebase. (https://firebase.google.com/) Consulta:13 de Setiembre del 2017

- Firebase Database. Tutorial para la integración de Firebase Database en un proyecto Android. (https://firebase.google.com/docs/android/setup) Consulta:13 de Setiembre del 2017
