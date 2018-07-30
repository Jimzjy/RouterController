package com.jimzjy.dialog

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window

/**
 * A simple [Fragment] subclass.
 *
 */
class LoadingDialog : DialogFragment() {
    private var mOnDismiss: (() -> Unit)? = null

    companion object {
        @JvmStatic
        fun newInstance() = LoadingDialog()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window.setBackgroundDrawableResource(R.color.transparent)
        return inflater.inflate(R.layout.fragment_loading_dialog, container, false)
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        mOnDismiss?.invoke()
    }

    fun setDismissAction(onDismiss: (() -> Unit)) {
        this.mOnDismiss = onDismiss
    }
}
