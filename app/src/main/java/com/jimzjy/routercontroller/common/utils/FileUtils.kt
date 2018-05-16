package com.jimzjy.routercontroller.common.utils

import android.os.Environment
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.*
import java.util.regex.Pattern

open class FileSeeker(path: String) {
    private var findFileComplete: (() -> Unit)? = null
    private var findFileStart: (() -> Unit)? = null
    private var findFileFind: ((File) -> Unit)? = null

    private val fileStack = LinkedList<File>()
    val fileStackSize get() = fileStack.size
    private var mPath = path
    var currentPath get() = mPath
        set(value) {
            val tmpFile = File(value)
            setFileDir(tmpFile, true)
        }
    var currentFile: File? = null
        private set
    var currentDirFiles: Array<File>? = null
        private set
    var disposable: Disposable? = null

    constructor() : this(Environment.getExternalStorageDirectory().path)

    open fun init() {
        val tmpFile = File(mPath)
        setFileDir(tmpFile, true)
    }

    open fun findFile(name: String, searchAt: String = mPath) {
        findFileObservable(name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<File> {
                    override fun onComplete() {
                        if (disposable?.isDisposed == false) disposable?.dispose()
                        findFileComplete?.invoke()
                    }
                    override fun onSubscribe(d: Disposable) {
                        disposable = d
                        findFileStart?.invoke()
                    }
                    override fun onNext(t: File) {
                        findFileFind?.invoke(t)
                    }
                    override fun onError(e: Throwable) {
                        if (disposable?.isDisposed == false) disposable?.dispose()
                    }
                })
    }

    open fun setFindFileStartListener(findFileStart: (() -> Unit)) {
        this.findFileStart = findFileStart
    }

    open fun setFindFileCompleteListener(findFileComplete: (() -> Unit)) {
        this.findFileComplete = findFileComplete
    }

    open fun setFindFileFindListener(findFileFind: ((File) -> Unit)) {
        this.findFileFind = findFileFind
    }

    open fun popFile() {
        if (fileStackSize > 1) {
            fileStack.pop()
            val tmpFile = fileStack.peek()
            mPath = tmpFile.absolutePath
            setFileDir(tmpFile, false)
        }
    }

    private fun setFileDir(tmpFile: File, pushFile: Boolean) {
        currentFile = if (tmpFile.exists() && tmpFile.canRead()) {
            tmpFile
        } else {
            null
        }
        currentDirFiles = if (tmpFile.isDirectory) {
            if (pushFile) {
                fileStack.push(tmpFile)
            }
            tmpFile.listFiles()
        } else {
            null
        }
    }

    private fun findFileObservable(name: String): Observable<File> {
        return Observable.create {
            val emitter = it
            val pattern = Pattern.compile("\\w*$name\\w*", Pattern.CASE_INSENSITIVE)
            val tmpFileStack = LinkedList<File>()
            tmpFileStack.push(currentFile)

            while (tmpFileStack.size > 0) {
                val tmpFile = tmpFileStack.pop()
                tmpFile.let {
                    if (pattern.matcher(it.name).matches()) {
                        emitter.onNext(it)
                    }
                    if (it.isDirectory) {
                        it.listFiles().forEach {
                            tmpFileStack.push(it)
                        }
                    }
                }
            }
            if (tmpFileStack.size == 0) emitter.onComplete()
        }
    }
}