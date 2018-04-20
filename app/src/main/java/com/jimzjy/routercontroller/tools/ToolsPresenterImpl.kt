package com.jimzjy.routercontroller.tools

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import com.jimzjy.dialog.CommandData
import com.jimzjy.routercontroller.status.StatusPresenterImpl
import com.jimzjy.routersshutils.common.Connector
import com.jimzjy.routersshutils.common.ConnectorInfo
import com.jimzjy.routersshutils.nvram.NvramConnector
import com.jimzjy.routersshutils.uci.UciConnector
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class ToolsPresenterImpl(private var mToolsView: ToolsView?, private var ctx: Context?) : ToolsPresenter {
    companion object {
        private const val TAG = "ToolsPresenterImpl"
        private const val MAX_CONNECT_TIMES = 10
    }
    private var mConnector: Connector? = null
    private val mDisposable = CompositeDisposable()

    override fun onStart() {
        startObservable()
    }

    override fun onStop() {
        disposeObservable()
    }

    override fun onDestroyView() {
        Thread(Runnable {
            mConnector?.disconnect()
            mConnector = null
        }).start()
        mToolsView = null
        ctx = null
    }

    override fun onClickReconnect() {
        disposeObservable()
        Thread(Runnable {
            mConnector?.disconnect()
            mConnector = null
        }).start()
        startObservable()
    }

    override fun isConnected(): Boolean {
        return mConnector?.isConnected == true
    }

    override fun executeCommand(command: String): Array<String> {
        if (isConnected()) {
            val outputString = StringBuilder()
            val errorString = StringBuilder()
            mConnector?.executeCommands(command, outputString, errorString)
            return arrayOf(outputString.toString(), errorString.toString())
        }
        return arrayOf("","")
    }

    override fun getCommandList(): MutableList<CommandData> {
        val editor = ctx?.getSharedPreferences("data", Context.MODE_PRIVATE)
        val commandSet = editor?.getStringSet("commandSet", emptySet())
        val commandList = mutableListOf<CommandData>()
        commandSet?.forEach {
            val tmp = it.split("!FNNDP!")
            if (tmp.size >= 2) {
                commandList.add(CommandData(tmp[0], tmp[1]))
            }
        }
        return commandList
    }

    override fun setCommandList(commandList: List<CommandData>) {
        val commandSet = mutableSetOf<String>()
        commandList.forEach {
            commandSet.add("${it.name}!FNNDP!${it.content}")
        }
        val editor = ctx?.getSharedPreferences("data", Context.MODE_PRIVATE)?.edit()
        editor?.putStringSet("commandSet", commandSet)
        editor?.apply()
    }

    private fun connectorObservable(): Observable<Boolean> {
        return Observable.create {
            if (!isConnected()){
                this.connect()
            }
            var count = 0
            for (i in 1..MAX_CONNECT_TIMES) {
                if (mConnector?.isConnected == true) {
                    it.onNext(true)
                    break
                }
                count = i
                Thread.sleep(1000)
            }
            if (count >= MAX_CONNECT_TIMES) {
                it.onNext(false)
            }
        }
    }

    private fun connect() {
        val preference = PreferenceManager.getDefaultSharedPreferences(ctx)
        val connectorInfo = ConnectorInfo(preference.getString("pref_key_username", ""),
                preference.getString("pref_key_password", ""),
                preference.getString("pref_key_ip_address", ""),
                preference.getString("pref_key_port", "22").toInt())

        mConnector = if (preference.getString("pref_key_connector_type", "") == "NVRAM") {
            NvramConnector(connectorInfo)
        } else {
            UciConnector(connectorInfo)
        }
        mConnector?.connect()
    }

    private fun startObservable() {
        try {
            mDisposable.add(connectorObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        if (it) {

                        } else {

                        }
                    })
        } catch (e: Exception) {
            Log.e(StatusPresenterImpl.TAG, e.toString())
        }
    }

    private fun disposeObservable() {
        if (!mDisposable.isDisposed) mDisposable.clear()
    }
}