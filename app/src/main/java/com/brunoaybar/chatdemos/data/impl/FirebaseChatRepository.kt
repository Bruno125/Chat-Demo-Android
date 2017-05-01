package com.brunoaybar.chatdemos.data.impl

import com.brunoaybar.chatdemos.data.ChatRepository
import com.brunoaybar.chatdemos.data.ChatUtils
import com.brunoaybar.chatdemos.data.Message
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import org.json.JSONObject
import java.security.Timestamp

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