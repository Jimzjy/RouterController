package com.jimzjy.routercontroller.common

import android.view.View

/**
 *
 */

interface DeviceItemClickListener {
    fun onClick(v: View, position: Int)
}

interface ToolsItemClickListener {
    fun onClick(v: View, position: Int)
}

interface ReconnectClickListener {
    fun onClickReconnect()
}