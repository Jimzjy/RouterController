package com.jimzjy.dialog


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

const val EDIT_COMMAND_DATA = "edit_command_data"
const val EDIT_POSITION = "edit_position"
/**
 * A simple [Fragment] subclass.
 *
 */
open class EditDialog : DialogFragment() {
    private var mTitle: TextView? = null
    private var mContent: TextView? = null
    private var mDoneButton: Button? = null
    private var mTitleText: String? = null
    private var mContentText: String? = null
    var position = 0
        private set(value) { field = value }

    companion object {
        @JvmStatic
        fun newInstance(title: String, content: String, position: Int)
            = EditDialog().apply {
            this.position = position
            this.mTitleText = title
            this.mContentText = content
        }

        @JvmStatic
        fun newInstance(position: Int)
            = EditDialog().apply {
            this.position = position
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_edit_dialog, container, false)

        mTitle = view.findViewById(R.id.edit_dialog_title)
        mContent = view.findViewById(R.id.edit_dialog_content)
        setTitleText(mTitleText ?: resources.getString(R.string.title))
        setContentText(mContentText ?: resources.getString(R.string.content))

        mDoneButton = view.findViewById(R.id.edit_dialog_button)
        mDoneButton?.setOnClickListener {
            sendData()
            dismiss()
        }
        return view
    }

    open fun setTitleText(title: String) {
        mTitle?.text = title
    }

    open fun setContentText(content: String) {
        mContent?.text = content
    }

    open fun setDoneButtonText(text: String) {
        mDoneButton?.text = text
    }

    private fun sendData() {
        val intent = Intent()
                .putExtra(EDIT_COMMAND_DATA, arrayOf(mTitle?.text.toString()
                        , mContent?.text.toString()))
                .putExtra(EDIT_POSITION, position)
        targetFragment.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
    }
}
