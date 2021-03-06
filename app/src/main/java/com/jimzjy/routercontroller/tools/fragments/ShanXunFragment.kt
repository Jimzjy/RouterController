package com.jimzjy.routercontroller.tools.fragments


import android.Manifest
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.telephony.SmsManager
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

import com.jimzjy.routercontroller.R
import com.jimzjy.routercontroller.common.utils.*
import com.jimzjy.routercontroller.tools.ToolsPresenter

/**
 * A simple [Fragment] subclass.
 *
 */
const val SEND_SMS_OK = "Send SMS: OK\n"
const val NORMAL_TEXT = "normal"
const val RED_TEXT = "#E57373"
const val GREEN_TEXT = "#4DB6AC"

class ShanXunFragment : Fragment() {
    private var mSmsReader: SmsReader? = null
    private var mToolsPresenter: ToolsPresenter? = null
    private lateinit var mNumberPasswordET: TextInputEditText
    private lateinit var mNumberPasswordLayout: TextInputLayout
    private lateinit var mPasswordET: TextInputEditText
    private lateinit var mCommitButton: Button
    private lateinit var mCommandET: TextInputEditText
    private lateinit var mDisplayText: TextView
    private lateinit var mAutoText: TextView
    private lateinit var mManualText: TextView
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
        mNumberPasswordLayout = view.findViewById(R.id.tools_sx_passwd_number_layout)

        val numberPasswordCommand = mToolsPresenter?.getNumberPassword() ?: arrayOf("", "", "")
        mNumberPasswordET.setText(numberPasswordCommand[0])
        mPasswordET.setText(numberPasswordCommand[1])
        mCommandET.setText(numberPasswordCommand[2])

        checkPermission()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            val text = mDisplayText.text.toString() +
                    resources.getString(R.string.set_phone_number_to_default)
            mDisplayText.text = text
            mNumberPasswordET.visibility = View.GONE
        }
        setListener()
        return view
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

    override fun onDestroy() {
        super.onDestroy()
        mToolsPresenter = null
    }

    private fun checkPermission() {
        val permissions = arrayOf(Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_SMS)

        for (p in permissions) {
            context?.let {
                if (ContextCompat.checkSelfPermission(it, p) != PackageManager.PERMISSION_GRANTED) {
                    misPermissionGet = false
                    addDisplayText(resources.getString(R.string.no_permission), RED_TEXT)
                    return
                }
            }
        }
        misPermissionGet = true
    }

    private fun setListener() {
        mCommitButton.setOnClickListener {
            clearDisplayText()
            if (misPermissionGet) {
                if (mToolsPresenter?.isConnected() == true) {
                    mPasswordConfig = mPasswordET.text.toString()
                    mCommandRestart = mCommandET.text.toString()
                    if (mPasswordConfig.isNotEmpty() && mCommandRestart.isNotEmpty()) {
                        if (misAutoMode) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                                val number = mNumberPasswordET.text.toString()
                                sendSms(number)
                            } else {
                                sendSms("")
                            }
                        } else {
                            val password = mNumberPasswordET.text.toString()
                            if (password.isNotEmpty()) {
                                doCommitConfig(password)
                            } else {
                                addDisplayText(resources.getString(R.string.empty_password), RED_TEXT)
                            }
                        }
                    } else {
                        addDisplayText(resources.getString(R.string.commit_error), RED_TEXT)
                    }
                    mToolsPresenter?.setNumberPassword(mNumberPasswordET.text.toString(), mPasswordConfig, mCommandRestart)
                } else {
                    addDisplayText(resources.getString(R.string.not_connect), RED_TEXT)
                }
            } else {
                addDisplayText(resources.getString(R.string.no_permission), RED_TEXT)
            }
        }
        mAutoText.setOnClickListener {
            if (!misAutoMode) {
                changeMode()
                misAutoMode = true
            }
        }
        mManualText.setOnClickListener {
            if (misAutoMode) {
                changeMode()
                misAutoMode = false
            }
        }
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
                    addDisplayText(SEND_SMS_OK, GREEN_TEXT)
                } catch (e: Exception) {
                    addDisplayText(resources.getString(R.string.no_permission_for_sms), RED_TEXT)
                }
            }
        }
    }

    private fun doSendSms(number: String, sentIntent: PendingIntent?) {
        val smsSender = SmsSender(context!!, number)
        var text = SEND_SMS_OK
        var exception = false
        try {
            val subscriptionId = smsSender.getSubscriptionId()
            smsSender.sendSms(subscriptionId, sentIntent)
        } catch (e: SmsUtilsException) {
            exception = true
            when (e.message) {
                NO_SUBSCRIPTION_ID_FOUND -> {
                    text = resources.getString(R.string.wrong_phone_number)
                }
                NO_PERMISSION_TO_READ_PHONE_STATE -> {
                    text = resources.getString(R.string.no_permission_phone_state)
                }
                NO_PERMISSION_FOR_SMS -> {
                    text = resources.getString(R.string.no_permission_for_sms)
                }
            }
        }
        if (exception) {
            addDisplayText(text, RED_TEXT)
        } else {
            addDisplayText(text, GREEN_TEXT)
        }
    }

    private fun registerSmsObserver() {
        mSmsReader?.let {
            context?.contentResolver?.registerContentObserver(Uri.parse(SMS_URI), true, it)
        }
    }

    private fun unRegisterSmsObserver() {
        mSmsReader?.let {
            context?.contentResolver?.unregisterContentObserver(it)
        }
    }

    private fun addDisplayText(text: String, color: String = NORMAL_TEXT, display: Boolean = true) {
        mText += when(color) {
            NORMAL_TEXT -> {
                "<p>$text</p>"
            }
            else -> {
                "<p><font color=\"$color\">$text</font></p>"
            }
        }
        if (display) {
            mDisplayText.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(mText, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(mText)
            }
        }
    }

    private fun clearDisplayText() {
        mText = ""
        mDisplayText.text = mText
    }

    private fun doCommitConfig(code: String) {
        addDisplayText("Password: $code", GREEN_TEXT)

        val configMap = hashMapOf<String, String>()
        mPasswordConfig.split(";").forEach {
            if (it.isNotEmpty()) {
                configMap[it] = code
            }
        }

        val handle = Handler()
        Thread(Runnable {
            var output = emptyArray<String>()
            try {
                if (mToolsPresenter?.isConnected() == true) {
                    mToolsPresenter?.setConfig(configMap, true)
                    output = mToolsPresenter?.executeCommand(mCommandRestart) ?: arrayOf("", "")
                }
                handle.post {
                    if (output[1].isEmpty()) {
                        addDisplayText("Commit Password: OK", GREEN_TEXT, false)
                        addDisplayText("Complete! ", GREEN_TEXT)
                    } else {
                        addDisplayText("Commit Password: ${output[1]}", RED_TEXT)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }

    private fun initSmsObserver() {
        val handler = Handler()
        mSmsReader = SmsReader(context!!, handler).setMessageListener {
            doCommitConfig(it)
        }
    }

    private fun changeMode() {
        if (misAutoMode) {
            mNumberPasswordET.setText("")
            //mNumberPasswordET?.setHint(R.string.phone_number_hint_manual)
            mNumberPasswordLayout.hint = resources.getString(R.string.phone_number_hint_manual)
            mAutoText.background = null
            mManualText.background = ContextCompat.getDrawable(context!!, R.drawable.round_corner_background)
        } else {
            mNumberPasswordET.setText("")
            //mNumberPasswordET?.setHint(R.string.phone_number_hint)
            mNumberPasswordLayout.hint = resources.getString(R.string.phone_number_hint)
            mManualText.background = null
            mAutoText.background = ContextCompat.getDrawable(context!!, R.drawable.round_corner_background)
        }
    }
}
