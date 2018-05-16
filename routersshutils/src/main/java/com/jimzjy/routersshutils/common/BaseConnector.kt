package com.jimzjy.routersshutils.common

import com.jcraft.jsch.*
import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 *
 */
abstract class Connector(private val connectorInfo: ConnectorInfo) {
    private var mSession: Session? = null
    open val sessionTimeout = 10000
    open val channelTimeout = 3000
    open val isConnected get() = mSession?.isConnected == true

    open fun connect() {
        try {
            val jsch = JSch()
            mSession = jsch.getSession(connectorInfo.username, connectorInfo.host, connectorInfo.port)
            mSession?.setPassword(connectorInfo.password)
            mSession?.setConfig("StrictHostKeyChecking", "no")
            mSession?.connect(sessionTimeout)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (mSession == null) throw SSHUtilsException("mSession is null")
    }

    open fun disconnect() {
        mSession?.disconnect()
        mSession = null
    }

    open fun executeCommands(commands: String, outputString: StringBuilder?, errorOutputString: StringBuilder?, timeWait: Long = 1000, setPty: Boolean = false) {
        if (mSession == null) throw SSHUtilsException("mSession is null")
        if (mSession?.isConnected != true) throw SSHUtilsException("mSession is not connected")
        var channel: ChannelExec? = null
        try {
            channel = mSession?.openChannel("exec") as ChannelExec
            channel.setCommand(commands)
            if (setPty) channel.setPty(true)
            channel.inputStream = null
            val input = channel.inputStream
            val error = channel.errStream
            channel.connect(channelTimeout)

            val tmp = ByteArray(1024)
            while (true) {
                while (input.available() > 0) {
                    val i = input.read(tmp, 0, 1024)
                    if (i < 0) break
                    outputString?.append(String(tmp, 0, 1024))
                }
                while (error.available() > 0) {
                    val i = error.read(tmp, 0, 1024)
                    if (i < 0) break
                    errorOutputString?.append(String(tmp, 0, 1024))
                }
                if (channel.isClosed) {
                    if (input.available() > 0 || error.available() > 0) continue
                    break
                }
                Thread.sleep(timeWait)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (channel != null && channel.isConnected) {
                channel.disconnect()
            }
        }
    }

    open fun sftpTo(dst: String, file: File, sftpProgress: SftpProgress? = null, uploadFinishAction: (() -> Unit)?) {
        if (mSession == null) throw SSHUtilsException("Session is null")
        if (mSession?.isConnected != true) throw SSHUtilsException("Session is not connected")
        var channel: ChannelSftp? = null
        var fileStack: LinkedList<File>? = null
        val ogPath = getPrevPath(file.absolutePath)
        val ogDst = if (dst.last() == '/') {
            dst.substring(0..(dst.length - 2))
        } else {
            dst
        }

        try {
            channel = mSession?.openChannel("sftp") as ChannelSftp
            channel.connect()

            if (file.isDirectory) {
                fileStack = LinkedList()
                fileStack.push(file)
                while (fileStack.size > 0) {
                    val tmpFile = fileStack.pop()
                    tmpFile.let {
                        if (it.isDirectory) {
                            var attrs: SftpATTRS? = null
                            val remoteFolderPath = getFolderPathForRemote(ogDst, ogPath, it.absolutePath)
                            try {
                                attrs = channel.stat(remoteFolderPath)
                            } catch (e: SftpException) {
                            }
                            if (attrs == null) {
                                channel.mkdir(remoteFolderPath)
                            }
                            it.listFiles().forEach {
                                fileStack.push(it)
                            }
                        } else {
                            sftpFileTo(channel, "$ogDst${it.absolutePath.split(ogPath)[1]}", it, sftpProgress)
                        }
                    }
                }
                uploadFinishAction?.invoke()
            } else {
                sftpFileTo(channel, "$ogDst/${file.name}", file, sftpProgress)
                uploadFinishAction?.invoke()
            }
        } catch (e: Exception) {
            throw SSHUtilsException(e)
        } finally {
            if (channel != null && channel.isConnected) {
                channel.disconnect()
            }
        }
    }

    private fun sftpFileTo(channel: ChannelSftp, dst: String, file: File, sftpProgress: SftpProgress?) {
        val fis = FileInputStream(file)
        if (sftpProgress != null) {
            channel.put(fis, dst, object : SftpProgressMonitor {
                override fun count(count: Long): Boolean {
                    return sftpProgress.count(count)
                }

                override fun end() {
                    sftpProgress.end()
                }

                override fun init(op: Int, src: String?, dest: String?, max: Long) {
                    sftpProgress.init(op, src, dest, max)
                }
            })
        } else {
            channel.put(fis, dst)
        }
        fis.close()
    }

    private fun getPrevPath(path: String): String {
        val tmpPath1 = path.split('/').toMutableList()
        if (tmpPath1[0].isEmpty()) {
            tmpPath1.removeAt(0)
        }
        tmpPath1.removeAt(tmpPath1.size - 1)
        var tmpPath2 = ""
        tmpPath1.forEach {
            tmpPath2 += "/$it"
        }
        return tmpPath2
    }

    private fun getFolderPathForRemote(dstPath: String, originalPath: String, folderPath: String): String {
        val tmpPath1 = folderPath.split(originalPath)[1]
        return "$dstPath$tmpPath1"
    }

    abstract fun getConnectingDevices(): List<DeviceInfo>
    abstract fun getNetworkSpeed(dev: String): FloatArray
    abstract fun getConfig(nameOrValue: String): HashMap<String, String>
    abstract fun setConfig(nameValueMap: HashMap<String, String>, commit: Boolean)
    abstract fun refreshARP()
}

data class ConnectorInfo(var username: String, var password: String, var host: String, var port: Int)
data class DeviceInfo(val name: String, val ip: String, val mac: String)

interface SftpProgress {
    fun count(count: Long): Boolean
    fun end()
    fun init(op: Int, src: String?, dest: String?, max: Long)
}