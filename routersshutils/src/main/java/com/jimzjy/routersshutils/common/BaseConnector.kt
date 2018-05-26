package com.jimzjy.routersshutils.common

import com.jcraft.jsch.*
import java.io.File
import java.io.FileInputStream
import java.util.*

const val PADAVAN_DHCP_FILE = "/tmp/static_ip.inf"
const val OPENWRT_DHCP_FILE = "/tmp/dhcp.leases"
const val ASUSWRT_DHCP_FILE = "/tmp/var/lib/misc/dnsmasq.leases"

/**
 *  SSH 连接器
 */
abstract class Connector(private val connectorInfo: ConnectorInfo) {
    private var mSession: Session? = null
    var nvramUciPath = ""
    open val sessionTimeout = 10000
    open val channelTimeout = 3000
    open val isConnected get() = mSession?.isConnected == true

    /**
     * 进行连接
     */
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

    /**
     * 取消连接
     */
    open fun disconnect() {
        mSession?.disconnect()
        mSession = null
    }

    /**
     * 执行命令
     *
     * @param commands 命令内容
     * @param outputString 返回普通信息
     * @param errorOutputString 返回的错误信息
     * @param timeWait 循环等待返回信息的时间
     * @param setPty 是否设置虚拟终端
     */
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
                    val i = input.read(tmp)
                    if (i < 0) break
                    outputString?.append(String(tmp, 0, i))
                }
                while (error.available() > 0) {
                    val i = error.read(tmp)
                    if (i < 0) break
                    errorOutputString?.append(String(tmp, 0, i))
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

    /**
     * 用 SFTP 上传文件到指定目录
     *
     * @param dst 目标目录
     * @param file 上传的文件
     * @param sftpProgress 反馈进度 [SftpProgress]
     */
    open fun sftpTo(dst: String, file: File, sftpProgress: SftpProgress? = null) {
        if (mSession == null) throw SSHUtilsException("Session is null")
        if (mSession?.isConnected != true) throw SSHUtilsException("Session is not connected")
        var channel: ChannelSftp? = null
        val fileStack: LinkedList<File>
        val ogPath = getPrevPath(file.absolutePath)
        val ogDst = removePathSuffix(dst)

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
            } else {
                sftpFileTo(channel, "$ogDst/${file.name}", file, sftpProgress)
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
        try {
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
        } catch (e: SftpException) {
            throw e
        } finally {
            fis.close()
        }
    }

    private fun removePathSuffix(path: String): String {
        if (path.last() == '/') {
            return path.substring(0, path.length - 1)
        }
        return path
    }

    /**
     * @param path 路径, 结尾不能有'/'
     */
    private fun getPrevPath(path: String): String {
        val tmpPath = removePathSuffix(path)
        val index = tmpPath.lastIndexOf('/')
        return tmpPath.substring(0, index)
    }

    private fun getFolderPathForRemote(dstPath: String, originalPath: String, folderPath: String): String {
        val tmpPath1 = folderPath.split(originalPath)[1]
        return "$dstPath$tmpPath1"
    }

    /**
     * 将得到的配置添加到传入的 [config] 里
     *
     * @param config 得到的 config 将会添加到这个列表
     * @param ip IP列表, 用于比对是否添加到 [config] 里面
     * @param file 设备信息存储文件路径
     * @return true 如果成功取得设备信息
     */
    open fun getHostName(config: MutableList<DeviceInfo>, ip: List<String>, file: String): Boolean {
        val errorOutput = StringBuilder()
        val outputString = StringBuilder()
        executeCommands("cat $file", outputString, errorOutput)
        if (errorOutput.isEmpty()) {
            config.clear()
        } else {
            return false
        }

        when (file) {
            PADAVAN_DHCP_FILE -> {
                config.addAll(getDeviceInfo(2, 0, 1, 3, ",", outputString, ip))
                return true
            }
            OPENWRT_DHCP_FILE -> {
                config.addAll(getDeviceInfo(3, 2, 1, 4, " ", outputString, ip))
                return true
            }
            ASUSWRT_DHCP_FILE -> {
                config.addAll(getDeviceInfo(3, 2, 1, 4, " ", outputString, ip))
                return true
            }
        }
        return false
    }

    private fun getDeviceInfo(namePos: Int, ipPos: Int, macPos: Int, minSize: Int, delimiter: String, outputString: StringBuilder, ip: List<String>): List<DeviceInfo> {
        val tmp = outputString.toString().split("\n")
        val deviceList = mutableListOf<DeviceInfo>()
        tmp.forEach {
            if (it.isNotEmpty()) {
                val tmp2 = it.split(delimiter)
                if (tmp2.size >= minSize) {
                    if (tmp2[ipPos] in ip) {
                        deviceList.add(DeviceInfo(tmp2[namePos], tmp2[ipPos], tmp2[macPos]))
                    }
                }
            }
        }
        return deviceList
    }

    /**
     * 获取用于取得网速的 device
     *
     * @return device 列表
     */
    open fun getDevArray(): Array<String> {
        val outputString = StringBuilder()
        val devList = mutableListOf<String>()
        executeCommands("cat /proc/net/dev | grep -vE \"(Receive|bytes)\"", outputString, null)
        if (outputString.isEmpty()) return emptyArray()
        outputString.toString().split("\n").forEach {
            val tmp = it.split(" +".toRegex())
            if (tmp.size >= 3) {
                try {
                    if (tmp[0].isEmpty() && tmp[2].toLong() > 0) {
                        devList.add(tmp[1].substring(0, tmp[1].length - 1))
                    } else if (tmp[0].isNotEmpty() && tmp[1].toLong() > 0){
                        devList.add(tmp[0].substring(0, tmp[0].length - 1))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return devList.toTypedArray()
    }

    /**
     * 取得连接设备信息
     *
     * @return 连接设备列表 [DeviceInfo]
     */
    abstract fun getConnectingDevices(): List<DeviceInfo>

    /**
     * 获得当前传输速度信息
     *
     * @return 上下行速度数组
     */
    abstract fun getNetworkSpeed(dev: String): FloatArray

    /**
     * 获得配置信息
     *
     * @return 名称和值的数组
     */
    abstract fun getConfig(nameOrValue: String): HashMap<String, String>

    /**
     * 设置配置信息
     *
     * @param nameValueMap 配置名称和值的数组
     */
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