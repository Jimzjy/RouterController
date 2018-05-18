package com.jimzjy.routersshutils.uci

import com.jimzjy.routersshutils.common.Connector
import com.jimzjy.routersshutils.common.ConnectorInfo
import com.jimzjy.routersshutils.common.DeviceInfo
import com.jimzjy.routersshutils.common.OPENWRT_DHCP_FILE

/**
 *
 */
class UciConnector(info: ConnectorInfo) : Connector(info) {

    override fun getConnectingDevices(): List<DeviceInfo> {
        val outputString = StringBuilder()
        val config = mutableListOf<DeviceInfo>()
        val ip = mutableListOf<String>()

        executeCommands("cat /proc/net/arp | grep -v IP", outputString, null)
        if (outputString.toString() == "") return emptyList()
        val tmp = outputString.toString().split("\n")
        tmp.forEach {
            if (it != "") {
                val ipMac = it.split(" +".toRegex())
                if (ipMac.size >= 4) {
                    config.add(DeviceInfo("< ? >",ipMac[0],ipMac[3]))
                    ip.add(ipMac[0])
                }
            }
        }

        if (getHostName(config, ip, OPENWRT_DHCP_FILE)) return config
        return config
    }

    override fun getNetworkSpeed(dev: String): FloatArray {
        val outputString = StringBuilder()
        val commands = "cat /proc/net/dev | grep eth0; sleep 1; cat /proc/net/dev | grep eth0"
        executeCommands(commands, outputString, null)

        val tmp = outputString.toString().split("\n", limit = 2)
        var upload = 0f
        var download = 0f
        var count = 1
        tmp.forEach {
            val tmp2 = it.split(" +".toRegex())
            if (count == 1) {
                upload = tmp2[2].toFloat()
                download = tmp2[10].toFloat()
            } else {
                upload = tmp2[2].toFloat() - upload
                download = tmp2[10].toFloat() - download
            }
            count++
        }
        return floatArrayOf(upload, download)
    }

    override fun getConfig(nameOrValue: String): HashMap<String, String> {
        val outputString = StringBuilder()
        val commands = "uci show | grep -E $nameOrValue"
        executeCommands(commands, outputString, null)

        val tmp = outputString.toString().split("\n")
        val config: HashMap<String, String> = hashMapOf()
        tmp.forEach {
            val tmp2 = it.split("=")
            if (tmp2.size > 1) {
                config[tmp2[0]] = tmp2[1]
            } else if (tmp2[0] != "") {
                config[tmp2[0]] = ""
            }
        }
        return config
    }

    override fun setConfig(nameValueMap: HashMap<String, String>, commit: Boolean) {
        val commands = StringBuilder()
        for ((K,V) in nameValueMap) {
            commands.append("uci set $K='$V';")
        }
        if (commit) commands.append("uci commit")
        executeCommands(commands.toString(), null, null)
    }

    override fun refreshARP() {}
}