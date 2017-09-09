package com.brunoaybar.chatdemos

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.brunoaybar.chatdemos.data.ChatFactory
import com.brunoaybar.chatdemos.data.ChatProviders
import com.brunoaybar.chatdemos.data.ChatRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import com.brunoaybar.chatdemos.R.id.textsRecycler
import com.brunoaybar.chatdemos.data.data.ChatInjection
import android.widget.Toast
import java.lang.reflect.AccessibleObject.setAccessible
import android.telephony.TelephonyManager
import android.content.Context.TELEPHONY_SERVICE
import android.net.NetworkInfo
import android.net.ConnectivityManager
import android.content.Context.CONNECTIVITY_SERVICE
import android.support.v7.app.AlertDialog
import com.brunoaybar.chatdemos.utils.getRepository
import com.brunoaybar.chatdemos.utils.open
import io.reactivex.disposables.Disposable


class MainActivity : AppCompatActivity() {

    lateinit var textField: EditText
    lateinit var recyclerView: RecyclerView
    private var adapter: TextsAdapter? = null

    lateinit var repository: ChatRepository
    var disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        repository = getRepository()

        textField = findViewById(R.id.eteText) as EditText
        recyclerView = findViewById(R.id.textsRecycler) as RecyclerView

        (findViewById(R.id.sendButton)).setOnClickListener {
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
        disposables = CompositeDisposable()
        disposables.add(repository.receive()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    adapter?.add(it)
                })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when(item?.itemId){
        R.id.menu_item_network -> {
            showNetworkType()
            true
        }
        R.id.menu_item_test -> {
            showTestModeAlert()
            true
        }
        else -> false
    }

    private fun showNetworkType(){
        val conn = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if(conn.activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI ){
            showAlert("Wifi")
            return
        }

        val mTelephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val networkType = mTelephonyManager.networkType
        when (networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS,
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_CDMA,
            TelephonyManager.NETWORK_TYPE_1xRTT,
            TelephonyManager.NETWORK_TYPE_IDEN -> return showAlert("2G")

            TelephonyManager.NETWORK_TYPE_UMTS,
            TelephonyManager.NETWORK_TYPE_EVDO_0,
            TelephonyManager.NETWORK_TYPE_EVDO_A,
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_EVDO_B,
            TelephonyManager.NETWORK_TYPE_EHRPD,
            TelephonyManager.NETWORK_TYPE_HSPAP -> return showAlert("3G")

            TelephonyManager.NETWORK_TYPE_LTE -> return showAlert("4G")

            else -> return showAlert("Unknown")
        }

    }

    private fun showAlert(msg: String){
        AlertDialog.Builder(this)
                .setTitle(R.string.txt_network)
                .setMessage(msg)
                .show()
    }

    private fun showTestModeAlert(){
        val options = arrayOf(R.string.txt_test_mode_sender, R.string.txt_test_mode_receiver)

        AlertDialog.Builder(this)
                .setTitle(R.string.txt_test_mode_title)
                .setItems(options.map { getString(it) }.toTypedArray()) { dialog, which ->
                    when(options[which]){
                        R.string.txt_test_mode_sender -> open<SenderActivity>()
                        R.string.txt_test_mode_receiver -> open<ReceiverActivity>()
                    }
                    dialog.dismiss()
                }.show()

    }

    private fun openSendActivity(){
        open<SenderActivity>()
    }

    private fun openReceiverActivity(){

    }



}
