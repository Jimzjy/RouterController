package com.jimzjy.routersshutils.common

import android.util.Log
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session

/**
 *
 */
abstract class Connector(private val connectorInfo: ConnectorInfo) {
    private val tag = "BaseConnector"
    private var session: Session? = null
    open val sessionTimeout = 10000
    open val channelTimeout = 3000
    open val isConnected get() = session?.isConnected == true

    open fun connect() {
        try {
            val jsch = JSch()
            session = jsch.getSession(connectorInfo.username, connectorInfo.host, connectorInfo.port)
            session?.setPassword(connectorInfo.password)
            session?.setConfig("StrictHostKeyChecking", "no")
            session?.connect(sessionTimeout)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (session == null) throw SSHUtilsException("session is null")
    }

    open fun disconnect() {
        session?.disconnect()
        session = null
    }

    open fun executeCommands(commands: String, outputString: StringBuilder?, errorOutputString: StringBuilder?, timeWait: Long = 1000) {
        if (session == null) throw SSHUtilsException("session is null")
        if (session?.isConnected != true) throw SSHUtilsException("session is not connected")
        try {
            val channel = session?.openChannel("exec") as ChannelExec
            channel.setCommand(commands)
            channel.inputStream = null
            val input = channel.inputStream
            val error = channel.errStream
            channel.connect(channelTimeout)

            val tmp = ByteArray(1024)
            while (true) {
                while (input.available() > 0) {
                    val i = input.read(tmp, 0, 1024)
                    if (i < 0) break
                    outputString?.append(String(tmp,0,1024))
                }
                while (error.available() > 0) {
                    val i = error.read(tmp, 0, 1024)
                    if (i < 0) break
                    errorOutputString?.append(String(tmp,0,1024))
                }
                if (channel.isClosed) {
                    if (input.available() > 0 || error.available() > 0) continue
                    break
                }
                try {
                    Thread.sleep(timeWait)
                } catch (ee: Exception) {
                    ee.printStackTrace()
                }
            }
            channel.disconnect()
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    abstract fun getConnectingDevices(): List<DeviceInfo>
    abstract fun getNetworkSpeed(dev: String): FloatArray
    abstract fun getConfig(nameOrValue: String): HashMap<String, String>
    abstract fun setConfig(nameValueMap: HashMap<String, String>, commit: Boolean)
    abstract fun refreshARP()
}

data class ConnectorInfo(var username: String, var password: String, var host: String, var port: Int)
data class DeviceInfo(val name: String, val ip: String, val mac: String)