package com.jimzjy.routercontroller.tools

import com.jimzjy.dialog.CommandData
import com.jimzjy.routersshutils.common.SftpProgress
import java.io.File

interface ToolsPresenter {
    fun onStart()
    fun onStop()
    fun onDestroyView()
    fun onClickReconnect()
    fun isConnected(): Boolean
    fun executeCommand(command: String, timeWait: Long = 500, setPty: Boolean = false): Array<String>
    fun getCommandList(): MutableList<CommandData>
    fun setCommandList(commandList: List<CommandData>)
    fun setConfig(nameValueMap: HashMap<String, String>, commit: Boolean)
    fun getConfig(nameOrValue: String): HashMap<String, String>
    fun setNumberPassword(number: String, passwordConfig: String, command: String)
    fun getNumberPassword(): Array<String>
    fun sftpTo(dst: String, file: File, sftpProgress: SftpProgress? = null, uploadFinishAction: (() -> Unit)? = null)
}