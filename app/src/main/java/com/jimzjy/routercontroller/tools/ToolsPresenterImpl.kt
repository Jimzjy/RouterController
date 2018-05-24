package com.jimzjy.routercontroller.tools

import android.content.Context
import android.os.Handler
import android.preference.PreferenceManager
import com.jimzjy.dialog.CommandData
import com.jimzjy.routersshutils.common.Connector
import com.jimzjy.routersshutils.common.ConnectorInfo
import com.jimzjy.routersshutils.common.SftpProgress
import com.jimzjy.routersshutils.nvram.NvramConnector
import com.jimzjy.routersshutils.uci.UciConnector
import java.io.File
import java.util.*


class ToolsPresenterImpl(private var mToolsView: ToolsView?, private var ctx: Context?) : ToolsPresenter {
    private var mConnector: Connector? = null
//    private val mDisposable = CompositeDisposable()
//    private var mTimer: Timer? = null

    override fun onStart() {
        startObservable()
    }

    override fun onStop() {
        disposeObservable()
    }

    override fun onDestroyView() {
        disconnectConnector()
        mToolsView = null
        ctx = null
    }

    override fun onClickReconnect() {
        disposeObservable()
        disconnectConnector()

        startObservable()
    }

    override fun isConnected(): Boolean {
        return mConnector?.isConnected == true
    }

    override fun executeCommand(command: String, timeWait: Long, setPty: Boolean): Array<String> {
        if (isConnected()) {
            val outputString = StringBuilder()
            val errorString = StringBuilder()
            mConnector?.executeCommands(command, outputString, errorString, timeWait, setPty)
            return arrayOf(outputString.toString(), errorString.toString())
        }
        return arrayOf("","")
    }

    override fun getCommandList(): MutableList<CommandData> {
        val sharedPreferences = ctx?.getSharedPreferences("data", Context.MODE_PRIVATE)
        val commandSet = sharedPreferences?.getStringSet("commandSet", emptySet())
        val commandList = mutableListOf<CommandData>()
        commandSet?.forEach {
            val tmp = it.split("!FNNDP!")
            if (tmp.size >= 2) {
                commandList.add(CommandData(tmp[0], tmp[1]))
            }
        }
        return commandList
    }

    /**
     * FNNDP: 只有风暴才能击倒大树? FNNDP! - Mr.Quin
     */
    override fun setCommandList(commandList: List<CommandData>) {
        val commandSet = mutableSetOf<String>()
        commandList.forEach {
            commandSet.add("${it.name}!FNNDP!${it.content}")
        }
        val editor = ctx?.getSharedPreferences("data", Context.MODE_PRIVATE)?.edit()
        editor?.putStringSet("commandSet", commandSet)
        editor?.apply()
    }

    override fun setConfig(nameValueMap: HashMap<String, String>, commit: Boolean) {
        if (isConnected()) {
            mConnector?.setConfig(nameValueMap, commit)
        }
    }

    override fun getConfig(nameOrValue: String): HashMap<String, String> {
        val notConnect = hashMapOf(Pair("Not Connect",""))
        if (isConnected()) {
            return mConnector?.getConfig(nameOrValue) ?: notConnect
        }
        return notConnect
    }

    override fun setNumberPassword(number: String, passwordConfig: String, command: String) {
        val editor = ctx?.getSharedPreferences("data", Context.MODE_PRIVATE)?.edit()
        editor?.putString("phoneNumber", number)
        editor?.putString("passwordConfig", passwordConfig)
        editor?.putString("restartNetworkCommand", command)
        editor?.apply()
    }

    override fun getNumberPassword(): Array<String> {
        val sharedPreferences = ctx?.getSharedPreferences("data", Context.MODE_PRIVATE)
        val number = sharedPreferences?.getString("phoneNumber","") ?: ""
        val passwordConfig = sharedPreferences?.getString("passwordConfig", "") ?: ""
        val command = sharedPreferences?.getString("restartNetworkCommand", "") ?: ""
        return arrayOf(number, passwordConfig, command)
    }

    override fun sftpTo(dst: String, file: File, sftpProgress: SftpProgress?, uploadFinishAction: (() -> Unit)?) {
        if (isConnected()) {
            mConnector?.sftpTo(dst, file, sftpProgress, uploadFinishAction)
        }
    }

//    private fun connectorObservable(): Observable<Boolean> {
//        return Observable.create {
//            if (!isConnected()){
//                this.connect()
//            }
//            var count = 0
//            try {
//                if (mTimer == null) mTimer = Timer()
//                mTimer?.schedule(object : TimerTask() {
//                    override fun run() {
//                        if (mConnector?.isConnected == true) {
//                            it.onNext(true)
//                            cancelTimer()
//                        } else {
//                            count++
//                            if (count >= MAX_CONNECT_TIMES) {
//                                it.onNext(false)
//                                cancelTimer()
//                            }
//                        }
//                    }
//                },0, 1000)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }

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

        val tmpPath = preference.getString("pref_key_path", "")
        mConnector?.nvramUciPath = if (tmpPath != "default" && tmpPath.isNotEmpty()) {
            if (tmpPath.last() != '/') {
                "$tmpPath/"
            } else {
                tmpPath
            }
        } else {
            ""
        }
        mConnector?.connect()
    }

    private fun startObservable() {
        if (!isConnected()) {
            Thread(Runnable {
                connect()
            }).start()
        }
//        try {
//            mDisposable.add(connectorObservable()
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe {
//                        if (it) {
//
//                        } else {
//
//                        }
//                    })
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }

    private fun disposeObservable() {
//        cancelTimer()
//        if (!mDisposable.isDisposed) mDisposable.clear()
    }

//    private fun cancelTimer() {
//        mTimer?.cancel()
//        mTimer?.purge()
//        mTimer = null
//    }

    private fun disconnectConnector(reconnect: Boolean = false) {
        val handler = Handler()
        Thread(Runnable {
            if (isConnected()) mConnector?.disconnect()
            mConnector = null

            if (reconnect) handler.post { startObservable() }
        }).start()
    }
}