package com.jimzjy.routercontroller.tools.fragments


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.jimzjy.routercontroller.R
import com.jimzjy.routercontroller.common.ReconnectClickListener

/**
 * A simple [Fragment] subclass.
 *
 */
class ShanXunFragment : Fragment(), ReconnectClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shan_xun, container, false)
    }

    override fun onClickReconnect() {
        println("Reconnect Shanxun")
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        println("ShanXun onAttach")
    }

    override fun onStart() {
        super.onStart()
        println("ShanXun onStart")
    }

    override fun onResume() {
        super.onResume()
        println("ShanXun onResume")
    }

    override fun onStop() {
        super.onStop()
        println("ShanXun onStop")
    }

    override fun onPause() {
        super.onPause()
        println("ShanXun onPause")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        println("ShanXun onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("ShanXun onDestroy")
    }

    override fun onDetach() {
        super.onDetach()
        println("ShanXun onDetach")
    }
}
