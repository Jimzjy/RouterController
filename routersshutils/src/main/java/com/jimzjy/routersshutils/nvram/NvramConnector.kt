package com.jimzjy.routersshutils.nvram

import com.jimzjy.routersshutils.common.*

/**
 *
 */
class NvramConnector(info: ConnectorInfo) : Connector(info) {
    private val tag = "NvramConnector"

    override fun setPPPoEConfig(username: String, password: String) {
        executeCommands("nvram set wan_pppoe_username=$username; nvram set wan_pppoe_password=$password; nvram commit",
                null, null)
    }

    override fun setPPPoEConfig(password: String) {
        val commands = "nvram set wan_pppoe_password=$password; nvram commit"
        executeCommands(commands, null, null)
    }

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
        getHostName(config, ip,"/tmp/static_ip.inf")

        return config
    }

    override fun getNetworkSpeed(dev: String): FloatArray {
        val outputString = StringBuilder()
        val commands = "cat /proc/net/dev | grep $dev; sleep 1; cat /proc/net/dev | grep $dev"
        executeCommands(commands, outputString, null, 1200)

        if (outputString.toString() == "") return floatArrayOf(0f, 0f)
        val tmp = outputString.toString().split("\n", limit = 2)
        var upload = 0f
        var download = 0f
        tmp.forEach {
            val tmp2 = it.split(" +".toRegex())
            if (tmp2.size > 17) {
                upload = tmp2[10].toFloat() - upload
                download = tmp2[2].toFloat() - download
            } else {
                upload = tmp2[9].toFloat() - upload
                download = tmp2[1].toFloat() - download
            }
        }
        return floatArrayOf(upload, download)
    }

    override fun getConfig(nameOrValue: String): Map<String, String> {
        val outputString = StringBuilder()
        val commands = "nvram show | grep -E $nameOrValue"
        executeCommands(commands, outputString, null)

        if (outputString.toString() == "") return emptyMap()

        val tmp = outputString.toString().split("\n")
        val config: MutableMap<String, String> = mutableMapOf()
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

    override fun setConfig(nameValueMap: Map<String, String>) {
        val commands = StringBuilder()
        for ((K, V) in nameValueMap) {
            commands.append("nvram set $K=$V;")
        }
        executeCommands(commands.toString(), null, null)
    }

    override fun refreshARP() {
        executeCommands("ip neigh flush all", null, null)
    }

    private fun getHostName(config: MutableList<DeviceInfo>, ip: List<String>, file: String) {
        val errorOutput = StringBuilder()
        val outputString = StringBuilder()
        executeCommands("cat $file", outputString, errorOutput)
        if (errorOutput.toString() == "") {
            config.clear()
        } else {
            return
        }

        val tmp = outputString.toString().split("\n")
        tmp.forEach {
            if (it != "") {
                val tmp2 = it.split(",")
                if (tmp2[0] in ip) {
                    config.add(DeviceInfo(tmp2[2], tmp2[0], tmp2[1]))
                }
            }
        }
    }
}