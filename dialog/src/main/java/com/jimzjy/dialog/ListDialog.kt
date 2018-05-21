package com.jimzjy.dialog


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView


const val COMMAND_DATA = "command_data"
const val MULTI_COMMAND_DATA = "multi_command_data"
const val EDIT_TO_LIST_UPDATE = 0
const val EDIT_TO_LIST_ADD = 1
/**
 * A simple [Fragment] subclass.
 *
 */
open class ListDialog : DialogFragment() {
    private var mListRecyclerViewAdapter: CommandListAdapter? = null
    private var mCancelButton: ImageView? = null
    private var mDoneButton: ImageView? = null
    private var mBottomLeftButton: TextView? = null
    private var mBottomRightButton: TextView? = null
    private var mCommandList: MutableList<CommandData>? = null
    private var mSaveCommandMethod: ((commandList: List<CommandData>) -> Unit)? = null
    private val mCommandSelected = mutableListOf<Boolean>()

    companion object {
        @JvmStatic
        fun newInstance(commandList: MutableList<CommandData>)
                = ListDialog().apply {
            mCommandList = commandList
            for (i in 1..(mCommandList?.size ?: 1)) {
                mCommandSelected.add(false)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_list_dialog, container, false)

        val listRecyclerView = view.findViewById<RecyclerView>(R.id.list_dialog_rv)
        mListRecyclerViewAdapter = CommandListAdapter(context!!,mCommandList ?: emptyList(), mCommandSelected)
        listRecyclerView.layoutManager = LinearLayoutManager(context)
        listRecyclerView.adapter = mListRecyclerViewAdapter
        listRecyclerView.itemAnimator = DefaultItemAnimator()
        listRecyclerView.setHasFixedSize(true)

        mCancelButton = view.findViewById(R.id.list_dialog_cancel_button)
        mCancelButton?.visibility = View.INVISIBLE
        mDoneButton = view.findViewById(R.id.list_dialog_done_button)
        mDoneButton?.visibility = View.INVISIBLE
        mBottomLeftButton = view.findViewById(R.id.list_dialog_bottom_left)
        mBottomRightButton= view.findViewById(R.id.list_dialog_bottom_right)

        setListener()
        return view
    }

    override fun onStop() {
        super.onStop()
        mSaveCommandMethod?.invoke(mCommandList ?: emptyList())
    }

    override fun onDestroy() {
        super.onDestroy()
        mSaveCommandMethod = null
        mCommandList = null
    }

    private fun setListener() {
        mListRecyclerViewAdapter?.setOnClickItem { _, position ->
            when(mListRecyclerViewAdapter?.multiSelectMode == true) {
                false -> {
                    sendData(position)
                    dismiss()
                }
                true -> {
                    mCommandSelected[position] = !mCommandSelected[position]
                }
            }
        }
        mListRecyclerViewAdapter?.setOnLongClickItem { _, position ->
            if (mListRecyclerViewAdapter?.multiSelectMode != true){
                mListRecyclerViewAdapter?.multiSelectMode = true
                mCommandSelected[position] = true
                mListRecyclerViewAdapter?.notifyDataSetChanged()
                mCancelButton?.visibility = View.VISIBLE
                mDoneButton?.visibility = View.VISIBLE
                mBottomLeftButton?.text = resources.getString(R.string.delete)
                mBottomRightButton?.text = resources.getString(R.string.edit)
            }
        }
        mCancelButton?.setOnClickListener {
            if (mListRecyclerViewAdapter?.multiSelectMode == true) {
                mListRecyclerViewAdapter?.multiSelectMode = false
                for (i in 0 until mCommandSelected.size) {
                    mCommandSelected[i] = false
                }
                mListRecyclerViewAdapter?.notifyDataSetChanged()
                mCancelButton?.visibility = View.INVISIBLE
                mDoneButton?.visibility = View.INVISIBLE
                mBottomLeftButton?.text = resources.getString(R.string.cancel)
                mBottomRightButton?.text = resources.getString(R.string.add)
            }
        }
        mDoneButton?.setOnClickListener {
            if (mListRecyclerViewAdapter?.multiSelectMode == true) {
                val tmp = mutableListOf<Int>()
                for (i in 0 until mCommandSelected.size) {
                    if (mCommandSelected[i]) tmp.add(i)
                }
                sendData(tmp.toIntArray())
                dismiss()
            }
        }
        mBottomLeftButton?.setOnClickListener {
            if (mListRecyclerViewAdapter?.multiSelectMode != true) {
                dismiss()
            } else {
                deleteSelectedData()
            }
        }
        mBottomRightButton?.setOnClickListener {
            if (mListRecyclerViewAdapter?.multiSelectMode != true) {
                val editDialog = EditDialog.newInstance(0)
                editDialog.setTargetFragment(this@ListDialog, EDIT_TO_LIST_ADD)
                editDialog.show(fragmentManager, "EditDialog")
            } else {
                for (i in 0 until mCommandSelected.size){
                    if (mCommandSelected[i]) {
                        val editDialog = EditDialog.newInstance(mCommandList?.get(i)?.name ?: ""
                                , mCommandList?.get(i)?.content ?: "",i)
                        editDialog.setTargetFragment(this@ListDialog, EDIT_TO_LIST_UPDATE)
                        editDialog.show(fragmentManager, "EditDialog")
                        break
                    }
                }
            }
        }
    }

    open fun setSaveCommandsMethod(method:(commandList: List<CommandData>) -> Unit) {
        mSaveCommandMethod = method
    }

//    open fun clearAndUpdateAllData(commandList: List<CommandData>) {
//        mCommandList?.clear()
//        mCommandList?.addAll(commandList)
//        mListRecyclerViewAdapter?.notifyDataSetChanged()
//    }

    open fun updateData(commandData: CommandData, position: Int) {
        mCommandList?.set(position, commandData)
        mListRecyclerViewAdapter?.notifyItemChanged(position)
    }

    open fun addData(commandData: CommandData, position: Int = 0) {
        mCommandList?.add(position, commandData)
        mCommandSelected.add(position, false)
        mListRecyclerViewAdapter?.notifyItemInserted(position)
    }

    open fun deleteSelectedData() {
        val dataList = mutableListOf<CommandData>()
        for (i in 0 until mCommandSelected.size) {
            if (mCommandSelected[i]){
                dataList.add(mCommandList?.get(i) ?: CommandData("",""))
            }
        }
        dataList.forEach {
            if (mCommandList?.contains(it) == true) {
                mCommandList?.remove(it)
            }
        }
        mCommandSelected.clear()
        for (i in 1..(mCommandList?.size ?: 1)) {
            mCommandSelected.add(false)
        }
        mListRecyclerViewAdapter?.notifyDataSetChanged()
    }

    private fun sendData(position: Int) {
        val intent = Intent().putExtra(COMMAND_DATA, position)
        targetFragment?.onActivityResult(0, Activity.RESULT_OK, intent)
    }

    private fun sendData(positionArray: IntArray) {
        val intent = Intent().putExtra(MULTI_COMMAND_DATA, positionArray)
        targetFragment?.onActivityResult(1, Activity.RESULT_OK, intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            EDIT_TO_LIST_UPDATE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val commandDataArray = data.getStringArrayExtra(EDIT_DATA)
                    if (commandDataArray != null) {
                        updateData(CommandData(commandDataArray[0],commandDataArray[1]),
                                data.getIntExtra(EDIT_POSITION, 0))
                    }
                }
            }
            EDIT_TO_LIST_ADD -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val commandDataArray = data.getStringArrayExtra(EDIT_DATA)
                    if (commandDataArray != null) {
                        addData(CommandData(commandDataArray[0],commandDataArray[1]),
                                data.getIntExtra(EDIT_POSITION, 0))
                    }
                }
            }
        }
    }
}