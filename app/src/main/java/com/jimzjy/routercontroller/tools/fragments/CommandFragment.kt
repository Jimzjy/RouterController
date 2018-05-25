package com.jimzjy.routercontroller.tools.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.jimzjy.dialog.COMMAND_DATA
import com.jimzjy.dialog.CommandData
import com.jimzjy.dialog.ListDialog
import com.jimzjy.dialog.MULTI_COMMAND_DATA
import com.jimzjy.routercontroller.R
import com.jimzjy.routercontroller.tools.ToolsPresenter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*


const val DIALOG_TO_FRAGMENT_COMMAND = 0
const val DIALOG_TO_FRAGMENT_MULTI_COMMAND = 1
/**
 *
 */
class CommandFragment : Fragment(){
    private var mToolsPresenter: ToolsPresenter? = null
    private lateinit var mCommandEditText: EditText
    private lateinit var mOutputDisplayText: TextView
    private lateinit var mUpdateTimeButton: Button
    private var mCommandListener: (() -> Unit)? = null
    private val mDisposable = CompositeDisposable()
    private val mCommandList = mutableListOf<CommandData>()

    companion object {
        @JvmStatic
        fun newInstance(toolsPresenter: ToolsPresenter?)
                = CommandFragment().apply {
            this.mToolsPresenter = toolsPresenter
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_command, container, false)

        mCommandList.addAll(mToolsPresenter?.getCommandList() ?: mutableListOf())
        val commandBundle = view.findViewById<Button>(R.id.tools_command_bundle)
        commandBundle.setOnClickListener {
            val listDialog = ListDialog.newInstance(mCommandList)
            listDialog.setTargetFragment(this@CommandFragment, DIALOG_TO_FRAGMENT_COMMAND)
            listDialog.setSaveCommandsMethod { mToolsPresenter?.setCommandList(it) }
            listDialog.show(fragmentManager, "ListDialog")
        }

        mCommandEditText = view.findViewById(R.id.tools_command_edit_text)
        mOutputDisplayText = view.findViewById(R.id.tools_command_display_text)
        mUpdateTimeButton = view.findViewById(R.id.tools_command_time)

        return view
    }

    override fun onStart() {
        super.onStart()
        startObservable()
    }

    override fun onStop() {
        super.onStop()
        if (!mDisposable.isDisposed) mDisposable.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mToolsPresenter = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            DIALOG_TO_FRAGMENT_COMMAND -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    mCommandEditText.setText(
                            mCommandList[data.getIntExtra(COMMAND_DATA, 0)].content)
                    mCommandListener?.invoke()
                }
            }
            DIALOG_TO_FRAGMENT_MULTI_COMMAND -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val position = data.getIntArrayExtra(MULTI_COMMAND_DATA)
                    val command = StringBuilder()
                    position.forEach {
                        command.append("${mCommandList[it].content}&&")
                    }
                    mCommandEditText.setText(command.toString())
                    mCommandListener?.invoke()
                }
            }
        }
    }

    private fun commandSendObservable(): Observable<String> {
        return Observable.create {
            val emitter = it
            mCommandEditText.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEND
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && KeyEvent.KEYCODE_ENTER == event.keyCode
                                && KeyEvent.ACTION_DOWN == event.keyCode)){
                    if (v.text.isNotEmpty()) {
                        hideSoftInput()
                        emitter.onNext(v.text.toString())
                        v.text = ""
                        mOutputDisplayText.text = resources.getString(R.string.try_to_get)
                    }
                }
                false
            }
            mCommandListener = {
                if (mCommandEditText.text?.isNotEmpty() == true) {
                    emitter.onNext(mCommandEditText.text.toString())
                    mCommandEditText.setText("")
                    mOutputDisplayText.text = resources.getString(R.string.try_to_get)
                }
            }
            mUpdateTimeButton.setOnClickListener {
                val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date())
                emitter.onNext("date -s \"$time\"")
                mOutputDisplayText.text = resources.getString(R.string.try_to_get)
            }
        }
    }

    private fun startObservable() {
        mDisposable.add(commandSendObservable()
                .observeOn(Schedulers.io())
                .map {
                    val notConnect = arrayOf(resources.getString(R.string.not_connect),"")
                    if (mToolsPresenter?.isConnected() == true) {
                        mToolsPresenter?.executeCommand(it) ?: notConnect
                    } else {
                        notConnect
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    mOutputDisplayText.text = if (it[1].isNotEmpty()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Html.fromHtml("<font color=\"#EF5350\">${it[1]}</font>${it[0]}", Html.FROM_HTML_MODE_COMPACT)
                        } else {
                            Html.fromHtml("<font color=\"#EF5350\">${it[1]}</font>${it[0]}")
                        }
                    } else {
                        it[0]
                    }
                })
    }

    private fun hideSoftInput() {
        (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(mCommandEditText.windowToken, 0)
    }
}
