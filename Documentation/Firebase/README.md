# Firebase

## Concepts

- __Firebase:__ plataform that offers developers a a suite of tools to develop, mantain and monitor múltiple mobile and backend solutions.
- __Firebase Database__: one of the tools offered by Firebase. It allows you to save information on a no relational database. Firebase Realtime Database clients can then subscribe to any changes that occur on this database.

## Specifications

This project uses Firebase Android SDK
- Version: 10.0.2
- Compatible with Java, Kotlin
- Min SDK: 14

## Diagrams

### Sequence Diagram

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/documentation/Documentation/Firebase/Diagrams/Diagrama%20de%20secuencia-%20Firebase.png)

### Components Diagram

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/documentation/Documentation/Firebase/Diagrams/Diagrama%20de%20componentes%20Firebase.png)

### Deployment Diagram

![alt tag](https://raw.githubusercontent.com/Bruno125/Communication-Demo-Android/documentation/Documentation/Firebase/Diagrams/Diagrama%20de%20despliegue.png)

## Immplementation

1. On the `build.gradle` file inside the [app](https://github.com/Bruno125/Communication-Demo-Android/blob/documentation/app/build.gradle) module:

   - Add the dependency to the project: 
   
   `compile 'com.google.firebase:firebase-database:10.2.0'` (or the latest one available)
   
   - Add the google-services.json file that you can download from Firebase Console, and include it on the app module.
   - At the end of the file, add the plugin:
   
   `apply plugin: 'com.google.gms.google-services'`

2. Create a refrence to the database, specifying the name of the node for which you'll publish / send messages.

```kotlin

private val mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference.child("chat")

```

3. Subcribe to the database, so that we can be notify when something changes.

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

4. To send messages, invoke the `push` method:

```kotlin
mDatabase.push().setValue("your message")
```

The final resut is the class [`FirebaseChatRepository`](https://github.com/Bruno125/Communication-Demo-Android/blob/documentation/app/src/main/java/com/brunoaybar/chatdemos/data/impl/FirebaseChatRepository.kt), which implements the ChatRepository interface

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


## Bibliography

- Firebase. Official page. (https://firebase.google.com/) 2017

- Firebase Database. Firebase Database Android SDK tutorial. (https://firebase.google.com/docs/android/setup) 2017
