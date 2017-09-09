package com.brunoaybar.chatdemos

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

import com.brunoaybar.chatdemos.R
import com.brunoaybar.chatdemos.utils.find
import com.brunoaybar.chatdemos.utils.getRepository
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class SenderActivity : AppCompatActivity() {

    private lateinit var amountSentTextView: TextView
    private lateinit var startEmittingButton: Button
    val disposables = CompositeDisposable()

    private var MESSAGES_INTERVAL = 100L
    private var MESSAGES_AMOUNT = 100L
    private var INITIAL_DELAY = 500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sender)

        amountSentTextView = find(R.id.amountSentTextView)
        startEmittingButton = find(R.id.startEmittingButton)

        amountSentTextView.text = "0/$MESSAGES_AMOUNT"
        startEmittingButton.setOnClickListener { startEmitting() }

    }

    private fun startEmitting(){

        val emissions = Flowable.intervalRange(1,
                MESSAGES_AMOUNT,
                INITIAL_DELAY,
                MESSAGES_INTERVAL,
                TimeUnit.MILLISECONDS)

        val repository = getRepository()

        disposables.add(emissions
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    repository.send("Test #$it")
                    trackNewEmission()
                } )
    }

    private var emissionsCount = 0
    private fun trackNewEmission(){
        emissionsCount++
        amountSentTextView.text = "$emissionsCount/$MESSAGES_AMOUNT"
    }

}
