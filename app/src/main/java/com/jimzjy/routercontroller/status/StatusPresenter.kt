package com.jimzjy.routercontroller.status


/**
 *
 */
interface StatusPresenter {
    fun onStop()
    fun onStart()
    fun onDestroy()
    fun onClickReconnect()
}