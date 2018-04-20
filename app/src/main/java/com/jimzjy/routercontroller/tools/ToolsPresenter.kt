package com.jimzjy.routercontroller.tools

import com.jimzjy.dialog.CommandData

interface ToolsPresenter {
    fun onStart()
    fun onStop()
    fun onDestroyView()
    fun onClickReconnect()
    fun isConnected(): Boolean
    fun executeCommand(command: String): Array<String>
    fun getCommandList(): MutableList<CommandData>
    fun setCommandList(commandList: List<CommandData>)
}