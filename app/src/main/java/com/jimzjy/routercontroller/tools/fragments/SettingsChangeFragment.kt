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
class SettingsChangeFragment : Fragment(), ReconnectClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_change, container, false)
    }

    override fun onClickReconnect() {
        println("Reconnect SettingsChange")
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        println("SettingsChange onAttach")
    }

    override fun onStart() {
        super.onStart()
        println("SettingsChange onStart")
    }

    override fun onResume() {
        super.onResume()
        println("SettingsChange onResume")
    }

    override fun onStop() {
        super.onStop()
        println("SettingsChange onStop")
    }

    override fun onPause() {
        super.onPause()
        println("SettingsChange onPause")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        println("SettingsChange onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("SettingsChange onDestroy")
    }

    override fun onDetach() {
        super.onDetach()
        println("SettingsChange onDetach")
    }
}
