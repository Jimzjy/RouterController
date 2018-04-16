package com.jimzjy.routercontroller.status

import com.jimzjy.routersshutils.common.DeviceInfo

/**
 *
 */
interface StatusView {
    fun setSpeedArray(speedArray: FloatArray)
    fun setSpeedReverse(isReverse: Boolean)
    fun updateDevicesList(deviceList: List<DeviceInfo>)
    fun updateSpeedBar()
}