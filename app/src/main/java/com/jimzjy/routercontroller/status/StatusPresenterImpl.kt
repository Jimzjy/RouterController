package com.jimzjy.routercontroller.status

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import com.jimzjy.routercontroller.R
import com.jimzjy.routersshutils.common.Connector
import com.jimzjy.routersshutils.common.ConnectorInfo
import com.jimzjy.routersshutils.common.DeviceInfo
import com.jimzjy.routersshutils.nvram.NvramConnector
import com.jimzjy.routersshutils.uci.UciConnector
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 *
 */
class StatusPresenterImpl(private var mStatusView: StatusView?, private var ctx: Context?) : StatusPresenter {
    companion object {
        const val TAG = "StatusPresenterImpl"
        private const val MAX_CONNECT_TIMES = 10
    }

    private val mDisposable = CompositeDisposable()
    private val mConnectingText = ctx?.resources?.getString(R.string.connecting) ?: "Connecting..."
    private val mConnectionFailedText = ctx?.resources?.getString(R.string.connection_failed) ?: "Connection Failed"
    private var mSpeedDev = "br0"
    private var mConnector: Connector? = null

    override fun onStart() {
        startObservable()
    }

    override fun onStop() {
        disposeObservable()
    }

    override fun onDestroy() {
        Thread(Runnable {
            mConnector?.disconnect()
            mConnector = null
        }).start()
        mStatusView = null
        ctx = null
    }

    override fun onClickReconnect() {
        disposeObservable()
        Thread(Runnable {
            mConnector?.disconnect()
            mConnector = null
        }).start()
        mStatusView?.updateDevicesList(listOf(DeviceInfo("", mConnectingText, "")))
        mStatusView?.setSpeedArray(floatArrayOf(0f, 0f))
        mStatusView?.updateSpeedBar()
        startObservable()
    }

    private fun networkSpeedObservable(): Observable<FloatArray> {
        return Observable.create {
            try {
                while (mConnector?.isConnected == true) {
                    it.onNext(mConnector?.getNetworkSpeed(mSpeedDev) ?: floatArrayOf(0f, 0f))
                    Thread.sleep(1500)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun deviceListObservable(): Observable<List<DeviceInfo>> {
        return Observable.create {
            try {
                it.onNext(mConnector?.getConnectingDevices() ?: emptyList())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun connectorObservable(): Observable<Boolean> {
        return Observable.create {
            if (mConnector?.isConnected != true){
                this.connect()
            }
            else {
                val preference = PreferenceManager.getDefaultSharedPreferences(ctx)
                mStatusView?.setSpeedReverse(preference.getBoolean("pref_key_speed_reverse", false))
                mSpeedDev = preference.getString("pref_key_speed_dev", "")
            }
            var count = 0
            try {
                for (i in 1..MAX_CONNECT_TIMES) {
                    if (mConnector?.isConnected == true) {
                        it.onNext(true)
                        break
                    }
                    count = i
                    Thread.sleep(1000)
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
        mStatusView?.setSpeedReverse(preference.getBoolean("pref_key_speed_reverse", false))
        mSpeedDev = preference.getString("pref_key_speed_dev", "")

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
                            mDisposable.add(networkSpeedObservable()
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe {
                                        mStatusView?.setSpeedArray(it)
                                        mStatusView?.updateSpeedBar()
                                    })
                            mDisposable.add(deviceListObservable()
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe {
                                        mStatusView?.updateDevicesList(it)
                                    })
                        } else {
                            mStatusView?.updateDevicesList(listOf(DeviceInfo("", mConnectionFailedText, "")))
                        }
                    })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun disposeObservable() {
        if (!mDisposable.isDisposed) mDisposable.clear()
    }
}