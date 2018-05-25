package com.jimzjy.routercontroller.tools.fragments


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import com.jimzjy.dialog.EDIT_DATA
import com.jimzjy.dialog.EDIT_POSITION
import com.jimzjy.dialog.EditDialog
import com.jimzjy.routercontroller.R
import com.jimzjy.routercontroller.common.SettingsRecyclerAdapter
import com.jimzjy.routercontroller.tools.ToolsPresenter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

const val SETTING_TO_DIALOG = 0
/**
 * A simple [Fragment] subclass.
 *
 */
class SettingsChangeFragment : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance(toolsPresenter: ToolsPresenter?)
                = SettingsChangeFragment().apply {
            this.mToolsPresenter = toolsPresenter
        }
    }
    private var mToolsPresenter: ToolsPresenter? = null
    private lateinit var mSettingEditText: EditText
    private lateinit var mSearchButton: ImageView
    private lateinit var mSettingRecyclerView: RecyclerView
    private lateinit var mRecyclerViewAdapter: SettingsRecyclerAdapter
    private var mCommitLocked = true
    private val mDisposable = CompositeDisposable()
    private val mSettingList = mutableListOf<SettingData>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings_change, container, false)

        mSettingEditText = view.findViewById(R.id.tools_settings_edit_text)
        mSearchButton = view.findViewById(R.id.tools_settings_search)

        mSettingList.add(SettingData(resources.getString(R.string.cant_find_config),""))
        mRecyclerViewAdapter = SettingsRecyclerAdapter(context!!, mSettingList)
        mRecyclerViewAdapter.setOnClickItem { _, position ->
            if (!mCommitLocked) {
                val editDialog = EditDialog.newInstance(mSettingList[position].name,
                        mSettingList[position].value,
                        resources.getString(R.string.commit), false, position)
                editDialog.setTargetFragment(this@SettingsChangeFragment, SETTING_TO_DIALOG)
                editDialog.show(fragmentManager,"EditDialog")
            }
        }
        mSettingRecyclerView = view.findViewById(R.id.tools_settings_rv)
        mSettingRecyclerView.layoutManager = LinearLayoutManager(context)
        mSettingRecyclerView.adapter = mRecyclerViewAdapter
        mSettingRecyclerView.setHasFixedSize(true)

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
            SETTING_TO_DIALOG -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val setting = data.getStringArrayExtra(EDIT_DATA)[1]
                    val position = data.getIntExtra(EDIT_POSITION,0)
                    try {
                        Thread(Runnable { mToolsPresenter?.setConfig(
                                hashMapOf(Pair(mSettingList[position].name, setting)), true)
                        }).start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun changeData(data: HashMap<String, String>) {
        mSettingList.clear()
        for ((K,V) in data) {
            mSettingList.add(SettingData(K,V))
        }
        mRecyclerViewAdapter.notifyDataSetChanged()
        mCommitLocked = false
    }

    private fun settingSendObservable(): Observable<String> {
        return Observable.create {
            val emitter = it
            mSearchButton.setOnClickListener {
                val text: String = mSettingEditText.text.toString()
                if (text.isNotEmpty()) {
                    hideSoftInput()
                    emitter.onNext(text)
                    mSettingEditText.setText("")
                    changeData(hashMapOf(Pair(resources.getString(R.string.try_to_get),"")))
                }
            }
            mSettingEditText.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEND
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && KeyEvent.KEYCODE_ENTER == event.keyCode
                                && KeyEvent.ACTION_DOWN == event.keyCode)){
                    if (v.text.isNotEmpty()) {
                        hideSoftInput()
                        emitter.onNext(v.text.toString())
                        v.text = ""
                        changeData(hashMapOf(Pair(resources.getString(R.string.try_to_get),"")))
                    }
                }
                false
            }
        }
    }

    private fun startObservable() {
        mDisposable.add(settingSendObservable()
                .observeOn(Schedulers.io())
                .map {
                    val notConnect = hashMapOf(Pair(resources.getString(R.string.not_connect),""))
                    if (mToolsPresenter?.isConnected() == true) {
                        mToolsPresenter?.getConfig(it) ?: notConnect
                    } else {
                        notConnect
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    changeData(it)
                })
    }

    private fun hideSoftInput() {
        (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(mSettingEditText.windowToken, 0)
    }
}

data class SettingData(val name: String, val value: String)
