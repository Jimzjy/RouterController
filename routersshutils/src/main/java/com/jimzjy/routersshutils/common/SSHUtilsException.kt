package com.jimzjy.routersshutils.common

/**
 * Created by 94347 on 2018/3/5.
 */
class SSHUtilsException: Exception {
    constructor() : super()
    constructor(s: String) : super(s)
    constructor(s: String, cause: Throwable): super(s, cause)
}