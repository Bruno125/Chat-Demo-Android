package com.brunoaybar.chatdemos

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Button
import android.widget.EditText
import com.brunoaybar.chatdemos.data.ChatFactory
import com.brunoaybar.chatdemos.data.ChatProviders
import com.brunoaybar.chatdemos.data.ChatRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import com.brunoaybar.chatdemos.R.id.textsRecycler



class MainActivity : AppCompatActivity() {

    lateinit var textField: EditText
    lateinit var recyclerView: RecyclerView
    lateinit var sendButton: Button
    private var adapter: TextsAdapter? = null

    val repository: ChatRepository = ChatFactory.create(ChatProviders.ABLY)
    val disposables = CompositeDisposable()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textField = findViewById(R.id.eteText) as EditText
        recyclerView = findViewById(R.id.textsRecycler) as RecyclerView
        sendButton = findViewById(R.id.sendButton) as Button

        sendButton.setOnClickListener {
            val text = textField.text.toString().trim()
            if(text.isNotEmpty()){
                repository.send(text)
            }
        }

        adapter = TextsAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        title = repository.name

    }


    override fun onResume() {
        super.onResume()
        disposables.add(repository.receive()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    adapter?.add(it)
                })
    }


}
