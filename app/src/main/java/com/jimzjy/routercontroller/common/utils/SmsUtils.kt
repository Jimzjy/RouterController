package com.jimzjy.routercontroller.common.utils

import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.telephony.SubscriptionManager
import java.util.*
import java.util.regex.Pattern

const val ADDRESS_NUMBER_0 = "+86106593005"
const val ADDRESS_NUMBER_1 = "106593005"
const val ADDRESS_NUMBER_2 = "86106593005"
const val SMS_INBOX_URI = "content://sms/inbox"
const val SMS_RAW_URI = "content://sms/raw"
const val SMS_URI = "content://sms"
const val SMS_CONTENT = "MM"
const val SMS_READ_ORDER = "date desc"
const val CAN_NOT_GET_CODE = "Can not get the code"
const val NO_SUBSCRIPTION_ID_FOUND = "No Subscription Id be found"
const val NO_PERMISSION_TO_READ_PHONE_STATE = "No permission to read phone state"
const val NO_PERMISSION_FOR_SMS = "No permission for sms"

@TargetApi(22)
class SmsSender(ctx: Context, private val number: String) {
    private val subscriptionManager = ctx.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

    fun getSubscriptionId(number: String): Int {
        var phoneNumber = number
        if (phoneNumber.isEmpty()) {
            return -1
        }
        if (phoneNumber.substring(0..2) != "+86") {
            phoneNumber = "+86$phoneNumber"
        }
        try {
            val subscriptionList = subscriptionManager.activeSubscriptionInfoList
            subscriptionList.forEach {
                if (it.number == phoneNumber) return it.subscriptionId
            }
        } catch (e: Exception) {
            throw SmsUtilsException(NO_PERMISSION_TO_READ_PHONE_STATE, e)
        }
        throw SmsUtilsException(NO_SUBSCRIPTION_ID_FOUND)
    }

    fun sendSms(subscriptionId: Int, sentIntent: PendingIntent?) {
        try {
            if (subscriptionId == -1) {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(ADDRESS_NUMBER_0, null, SMS_CONTENT, sentIntent, null)
            } else {
                val smsManager = SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
                smsManager.sendTextMessage(ADDRESS_NUMBER_0, null, SMS_CONTENT, sentIntent, null)
            }
        } catch (e: Exception) {
            throw SmsUtilsException(NO_PERMISSION_FOR_SMS)
        }
    }
}

class SmsReceiver : BroadcastReceiver() {
    private var mMessageListener: ((String) -> Unit)? = null
    private val mPattern = Pattern.compile("\\w*[a-z0-9A-Z]+\\w*")

    override fun onReceive(context: Context?, intent: Intent?) {
        val bundle = intent?.extras
        val pdus = (bundle?.get("pdus") as Array<ByteArray>)
        val message = mutableListOf<SmsMessage>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            message.addAll(getMessage(bundle, pdus))
        } else {
            pdus.forEach {
                message.add(SmsMessage.createFromPdu(it))
            }
        }

        val address = message[0].originatingAddress
        if (address == ADDRESS_NUMBER_0 || address == ADDRESS_NUMBER_1 || address == ADDRESS_NUMBER_2) {
            var messageContent = ""
            message.forEach {
                messageContent += it.messageBody
            }
            mMessageListener?.invoke(getCode(messageContent))
        }

        //abortBroadcast()
    }

    @TargetApi(23)
    private fun getMessage(bundle: Bundle, pdus: Array<ByteArray>): MutableList<SmsMessage> {
        val format = bundle.getString("format")
        val message = mutableListOf<SmsMessage>()
        pdus.forEach {
            message.add(SmsMessage.createFromPdu(it, format))
        }
        return message
    }

    private fun getCode(message: String): String {
        val matcher = mPattern.matcher(message)
        if (matcher.find()) {
            return matcher.group()
        }
        return CAN_NOT_GET_CODE
    }

    fun setMessageListener(onGetMessage: (code: String) -> Unit): SmsReceiver {
        mMessageListener = onGetMessage
        return this@SmsReceiver
    }
}

class SmsReader(private val mContext: Context, mHandler: Handler) : ContentObserver(mHandler) {
    private var mMessageListener: ((String) -> Unit)? = null
    private val mPattern = Pattern.compile("\\w*[a-z0-9A-Z]+\\w*")
    private var uriBefore = ""

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        if (uri.toString() == SMS_RAW_URI || uri.toString() == uriBefore) {
            return
        }
        uriBefore = uri.toString()
        val cursor = mContext.contentResolver.query(Uri.parse(SMS_INBOX_URI),
                arrayOf("body", "date"),
                "address in (?,?,?)",
                arrayOf(ADDRESS_NUMBER_0, ADDRESS_NUMBER_1, ADDRESS_NUMBER_2),
                SMS_READ_ORDER)
        cursor.moveToFirst()

        val messageDate = cursor.getString(1).toLong()
        val date = Date().time
        if ((date - messageDate) in (0..3000)) {
            val message = cursor.getString(0)
            cursor.close()
            mMessageListener?.invoke(getCode(message))
        }

        if (!cursor.isClosed) cursor.close()
    }

    private fun getCode(message: String): String {
        val matcher = mPattern.matcher(message)
        if (matcher.find()) {
            return matcher.group()
        }
        return CAN_NOT_GET_CODE
    }

    fun setMessageListener(onGetMessage: (code: String) -> Unit): SmsReader {
        mMessageListener = onGetMessage
        return this@SmsReader
    }
}

class SmsUtilsException: Exception {
    constructor(): super()
    constructor(s: String): super(s)
    constructor(s: String, cause: Throwable): super(s, cause)
    constructor(cause: Throwable): super(cause)
}



