package com.jimzjy.routercontroller.status


/**
 *
 */
interface StatusPresenter {
    fun onStop()
    fun onStart()
    fun onDestroyView()
    fun onClickReconnect()
    fun getDev(): String
    fun setDev(dev: String)
    fun getDevArray(): Array<String>
}