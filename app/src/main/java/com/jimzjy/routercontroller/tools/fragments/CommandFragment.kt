package com.jimzjy.routercontroller.tools.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.jimzjy.dialog.ListDialog

import com.jimzjy.routercontroller.R
import com.jimzjy.routercontroller.common.ReconnectClickListener
import com.jimzjy.routercontroller.tools.ToolsPresenter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 *
 */
class CommandFragment : Fragment(), ReconnectClickListener {
    companion object {
        @JvmStatic
        fun newInstance(toolsPresenter: ToolsPresenter)
                = CommandFragment().apply {
            this.mToolsPresenter = toolsPresenter
        }
    }
    private var mToolsPresenter: ToolsPresenter? = null
    private var mCommandEditText: EditText? = null
    private var mOutputDisplayText: TextView? = null
    private val mDisposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_command, container, false)

        val mCommandBundle = view.findViewById<Button>(R.id.tools_command_bundle)
        mCommandBundle.setOnClickListener {
            ListDialog().show(fragmentManager, "ListDialog")
        }

        mCommandEditText = view.findViewById(R.id.tools_command_edit_text)
        mOutputDisplayText = view.findViewById(R.id.tools_command_display_text)

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

    override fun onClickReconnect() {
        println("Reconnect Command")
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = fragmentManager.beginTransaction()
//        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        fragmentTransaction.replace(R.id.tools_replace_layout, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun commandSendObservable(): Observable<String> {
        return Observable.create {
            mCommandEditText?.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEND
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && KeyEvent.KEYCODE_ENTER == event.keyCode
                                && KeyEvent.ACTION_DOWN == event.keyCode)){
                    println(Thread.currentThread().name)
                    it.onNext(v.text.toString())
                    v.text = ""
                }
                false
            }
        }
    }

    private fun startObservable() {
        mDisposable.add(commandSendObservable()
                .observeOn(Schedulers.io())
                .map {
                    if (mToolsPresenter?.isConnected() == true) {
                        mToolsPresenter?.executeCommand(it) ?: arrayOf("","")
                    } else {
                        arrayOf(resources.getString(R.string.not_connect),"")
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    mOutputDisplayText?.text = if (it[1] != "") {
                        "${it[1]}\n${it[0]}"
                    } else {
                        it[0]
                    }
                })
    }
}
