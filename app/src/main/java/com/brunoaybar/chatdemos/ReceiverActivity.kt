package com.brunoaybar.chatdemos

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

import com.brunoaybar.chatdemos.data.Message
import com.brunoaybar.chatdemos.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.*

class ReceiverActivity : AppCompatActivity() {

    val disposables = CompositeDisposable()
    private lateinit var counterTextView: TextView
    private var EXPECTED_MESSAGES = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receiver)
        counterTextView = find(R.id.counterTextView)
        counterTextView.text = "0/$EXPECTED_MESSAGES"
    }

    override fun onResume() {
        super.onResume()
        disposables.add(getRepository().receive()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ receiveMessage(it) }, { _ ->  })
        )
    }

    private val messages = mutableListOf<Message>()
    private fun receiveMessage(message: Message){
        messages.add(message)
        counterTextView.text = "${messages.size}/$EXPECTED_MESSAGES"

        if(messages.size == EXPECTED_MESSAGES){
            disposables.clear()
            shareResults()
        }
    }

    private fun shareResults(){
        val textToWrite = messages.asTabbedText()
        val date = Date()
        val fileName = "mediciones-$date.txt"
        shareAsFile(textToWrite, fileName)
    }

    private fun List<Message>.asTabbedText(): String{
        return mapIndexed { index, message -> "${index+1}\t${message.delayValue}" }
                .reduce { a, b -> "$a\n$b" }
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }

}
