package com.jimzjy.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.lottie.LottieAnimationView

/**
 * A simple [Fragment] subclass.
 *
 */
class LoadingDialog : DialogFragment() {
    private var mAnimation: LottieAnimationView? = null
    private var mOnDismiss: (() -> Unit)? = null

    companion object {
        @JvmStatic
        fun newInstance() = LoadingDialog()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_loading_dialog, container, false)

        mAnimation = view.findViewById(R.id.loading_dialog_animation)
        mAnimation?.setAnimation(R.raw.animation_loading)

        return view
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        mOnDismiss?.invoke()
    }

    fun setDismissAction(onDismiss: (() -> Unit)) {
        this.mOnDismiss = onDismiss
    }
}
