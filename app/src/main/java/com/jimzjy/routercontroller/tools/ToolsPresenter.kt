package com.jimzjy.routercontroller.tools

import android.widget.FrameLayout
import android.widget.ImageView

interface ToolsPresenter {
    fun onStart()
    fun onStop()
    fun onDestroyView()
    fun onClickReconnect()
    fun isConnected(): Boolean
    fun executeCommand(command: String): Array<String>
}