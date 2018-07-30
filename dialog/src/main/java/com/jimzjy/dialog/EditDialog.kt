package com.jimzjy.dialog


import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView

const val EDIT_DATA = "edit_command_data"
const val EDIT_POSITION = "edit_position"
/**
 * A simple [Fragment] subclass.
 *
 */
open class EditDialog : DialogFragment() {
    private lateinit var mTitle: TextView
    private lateinit var mContent: TextView
    private lateinit var mDoneButton: TextView
    private var mTitleText: String? = null
    private var mContentText: String? = null
    private var mDoneButtonText: String? = null
    private var mTitleEditable: Boolean = true
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
        fun newInstance(title: String, content: String, doneText: String,
                        titleEditable: Boolean = true,position: Int)
                = EditDialog().apply {
            this.position = position
            this.mTitleText = title
            this.mContentText = content
            this.mDoneButtonText = doneText
            this.mTitleEditable = titleEditable
        }

        @JvmStatic
        fun newInstance(position: Int)
            = EditDialog().apply {
            this.position = position
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = inflater.inflate(R.layout.fragment_edit_dialog, container, false)

        mTitle = view.findViewById(R.id.edit_dialog_title)
        mContent = view.findViewById(R.id.edit_dialog_content)
        mDoneButton = view.findViewById(R.id.edit_dialog_button)
        mDoneButton.setOnClickListener {
            sendData()
            dismiss()
        }

        mTitleText?.let {
            if (mTitleEditable) {
                mTitle.text = it
            } else {
                mTitle.text = String.format(resources.getString(R.string.withColon), it)
            }
        }
        mContentText?.let { mContent.text = it }
        mDoneButtonText?.let { mDoneButton.text = it }

        if (!mTitleEditable) {
            mTitle.background = null
            mTitle.isEnabled = false
        }
        return view
    }

    private fun sendData() {
        val intent = Intent()
                .putExtra(EDIT_DATA, arrayOf(mTitle.text.toString()
                        , mContent.text.toString()))
                .putExtra(EDIT_POSITION, position)
        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
    }
}
