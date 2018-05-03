package com.jimzjy.routercontroller.tools.fragments


import android.Manifest
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.telephony.SmsManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import com.jimzjy.routercontroller.R
import com.jimzjy.routercontroller.common.*
import com.jimzjy.routercontroller.tools.ToolsPresenter

/**
 * A simple [Fragment] subclass.
 *
 */
const val REQUEST_PERMISSION = 0
const val SEND_SMS_OK = "Send SMS: OK\n"

class ShanXunFragment : Fragment(), ReconnectClickListener {
    private var mSmsReader: SmsReader? = null
    private var mToolsPresenter: ToolsPresenter? = null
    private var mNumberPasswordET: EditText? = null
    private var mPasswordET: EditText? = null
    private var mCommitButton: Button? = null
    private var mCommandET: EditText? = null
    private var mDisplayText: TextView? = null
    private var mAutoText: TextView? = null
    private var mManualText: TextView? = null
    private var mText = ""
    private var mPasswordConfig = ""
    private var mCommandRestart = ""
    private var misAutoMode = true
    private var misPermissionGet = false

    companion object {
        @JvmStatic
        fun newInstance(toolsPresenter: ToolsPresenter?) = ShanXunFragment().apply {
            this.mToolsPresenter = toolsPresenter
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_shan_xun, container, false)

        mNumberPasswordET = view.findViewById(R.id.tools_sx_passwd_number_edit)
        mPasswordET = view.findViewById(R.id.tools_sx_passwd_edit)
        mCommitButton = view.findViewById(R.id.tools_sx_commit_button)
        mCommandET = view.findViewById(R.id.tools_sx_restart_command)
        mDisplayText = view.findViewById(R.id.tools_sx_display_text)
        mAutoText = view.findViewById(R.id.tools_sx_auto_text)
        mManualText = view.findViewById(R.id.tools_sx_manual_text)

        val numberPasswordCommand = mToolsPresenter?.getNumberPassword() ?: arrayOf("", "", "")
        mNumberPasswordET?.setText(numberPasswordCommand[0])
        mPasswordET?.setText(numberPasswordCommand[1])
        mCommandET?.setText(numberPasswordCommand[2])

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission()
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            val text = mDisplayText?.text.toString() +
                    resources.getString(R.string.set_phone_number_to_default)
            mDisplayText?.text = text
            mNumberPasswordET?.visibility = View.GONE
        }
        setListener()
        return view
    }

    override fun onClickReconnect() {

    }

    override fun onResume() {
        super.onResume()
//        initSmsReceiver()
//        registerSmsReceiver()
        if (misPermissionGet) {
            initSmsObserver()
            registerSmsObserver()
        }
    }

    override fun onPause() {
        super.onPause()
//        unRegisterSmsReceiver()
        unRegisterSmsObserver()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mToolsPresenter = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            REQUEST_PERMISSION -> {
                Log.e("MIUISB", "callback")
                if (grantResults[0] == 0 && grantResults[1] == 0 && grantResults[2] == 0) {
                    misPermissionGet = true
                    registerSmsObserver()
                } else {
                    setDisplayText(resources.getString(R.string.no_permission))
                }
            }
        }
    }

    private fun setListener() {
        mCommitButton?.setOnClickListener {
            if (mToolsPresenter?.isConnected() == true) {
                mPasswordConfig = mPasswordET?.text.toString()
                mCommandRestart = mCommandET?.text.toString()
                if (mPasswordConfig.isNotEmpty() && mCommandRestart.isNotEmpty()) {
                    if (misAutoMode) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                            val number = mNumberPasswordET?.text.toString()
                            sendSms(number)
                            setDisplayText(mText)
                        } else {
                            sendSms("")
                            setDisplayText(mText)
                        }
                    } else {
                        val password = mNumberPasswordET?.text.toString()
                        mText = ""
                        if (password.isNotEmpty()) {
                            doCommitConfig(password)
                        } else {
                            setDisplayText(resources.getString(R.string.empty_password))
                        }
                    }
                } else {
                    setDisplayText(resources.getString(R.string.commit_error))
                }
                mToolsPresenter?.setNumberPassword(mNumberPasswordET?.text.toString(), mPasswordConfig, mCommandRestart)
            } else {
                setDisplayText(resources.getString(R.string.not_connect))
            }
        }
        mAutoText?.setOnClickListener {
            if (!misAutoMode) {
                changeMode()
                misAutoMode = true
            }
        }
        mManualText?.setOnClickListener {
            if (misAutoMode) {
                changeMode()
                misAutoMode = false
            }
        }
    }

    private fun requestPermission() {
        val permissions = arrayOf(Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_SMS)
        for (p in permissions) {
            if (ContextCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, permissions, REQUEST_PERMISSION)
                return
            }
        }
        misPermissionGet = true
    }

    private fun sendSms(number: String) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 -> {
                doSendSms(number, null)
            }
            else -> {
                try {
                    val smsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(ADDRESS_NUMBER_0, null, SMS_CONTENT, null, null)
                    mText = SEND_SMS_OK
                } catch (e: Exception) {
                    mDisplayText?.text = resources.getString(R.string.no_permission_for_sms)
                }
            }
        }
    }

    private fun doSendSms(number: String, sentIntent: PendingIntent?) {
        val smsSender = SmsSender(context, number)
        mText = SEND_SMS_OK
        try {
            val subscriptionId = smsSender.getSubscriptionId(number)
            smsSender.sendSms(subscriptionId, sentIntent)
        } catch (e: SmsUtilsException) {
            when (e.message) {
                NO_SUBSCRIPTION_ID_FOUND -> {
                    mText = resources.getString(R.string.wrong_phone_number)
                }
                NO_PERMISSION_TO_READ_PHONE_STATE -> {
                    mText = resources.getString(R.string.no_permission_phone_state)
                }
                NO_PERMISSION_FOR_SMS -> {
                    mText = resources.getString(R.string.no_permission_for_sms)
                }
            }
        }
    }

    private fun registerSmsObserver() {
        mSmsReader?.let {
            context.contentResolver.registerContentObserver(Uri.parse(SMS_URI), true, it)
        }
    }

    private fun unRegisterSmsObserver() {
        mSmsReader?.let {
            context.contentResolver.unregisterContentObserver(it)
        }
    }

//    private fun registerSmsReceiver() {
//        if (mSmsReceiver != null) {
//            val intentFilter = IntentFilter()
//            intentFilter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
//            intentFilter.priority = IntentFilter.SYSTEM_HIGH_PRIORITY
//            context.registerReceiver(mSmsReceiver, intentFilter)
//        }
//    }
//
//    private fun unRegisterSmsReceiver() {
//        if (mSmsReceiver != null) {
//            context.unregisterReceiver(mSmsReceiver)
//        }
//    }

    private fun setDisplayText(s: String) {
        mDisplayText?.text = s
    }

//    private fun initSmsReceiver() {
//        mSmsReceiver = SmsReceiver().setMessageListener {
//            mText += "Password: $it\n"
//            val password = it
//            setDisplayText(mText)
//            val handle = Handler()
//            Thread(Runnable {
//                try {
//                    if (mToolsPresenter?.isConnected() == true) {
//                        mToolsPresenter?.setConfig(hashMapOf(Pair(mPasswordConfig, password)),true)
//                    }
//                    handle.post {
//                        mText += "Commit Password: OK\n"
//                        setDisplayText(mText)
//                    }
//                } catch (e: Exception){
//                    e.printStackTrace()
//                }
//            }).start()
//        }
//    }

    private fun doCommitConfig(code: String) {
        mText += "Password: $code\n"
        setDisplayText(mText)

        val configMap = hashMapOf<String, String>()
        mPasswordConfig.split(";").forEach {
            if (it.isNotEmpty()) {
                configMap[it] = code
            }
        }

        val handle = Handler()
        Thread(Runnable {
            try {
                if (mToolsPresenter?.isConnected() == true) {
                    mToolsPresenter?.setConfig(configMap, true)
                    mToolsPresenter?.executeCommand(mCommandRestart)
                }
                handle.post {
                    mText += "Commit Password: OK\n"
                    setDisplayText(mText)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }

    private fun initSmsObserver() {
        val handler = Handler()
        mSmsReader = SmsReader(context, handler).setMessageListener {
            doCommitConfig(it)
        }
    }

    private fun changeMode() {
        if (misAutoMode) {
            mNumberPasswordET?.setText("")
            mNumberPasswordET?.setHint(R.string.phone_number_hint_manual)
            mAutoText?.background = null
            mManualText?.background = ContextCompat.getDrawable(context, R.drawable.round_corner_background)
        } else {
            mNumberPasswordET?.setText("")
            mNumberPasswordET?.setHint(R.string.phone_number_hint)
            mManualText?.background = null
            mAutoText?.background = ContextCompat.getDrawable(context, R.drawable.round_corner_background)
        }
    }
}
