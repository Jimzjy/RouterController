package com.jimzjy.routercontroller.tools

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.jimzjy.dialog.LoadingDialog
import com.jimzjy.routercontroller.R
import com.jimzjy.routercontroller.common.FilesRecyclerAdapter
import com.jimzjy.routercontroller.common.utils.FileSeeker
import kotlinx.android.synthetic.main.activity_local_files.*
import java.io.File


class LocalFilesActivity : AppCompatActivity() {
    private var mRVAdapter: FilesRecyclerAdapter? = null
    private val mFileSeeker = FileSeeker()
    private val mFileList = mutableListOf<File>()
    private var mLoadingDialog: LoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_files)

        mFileSeeker.init()
        mFileList.addAll(mFileSeeker.currentDirFiles ?: emptyArray())
        mRVAdapter = FilesRecyclerAdapter(applicationContext, mFileList)
        tools_lf_rv.layoutManager = LinearLayoutManager(applicationContext)
        tools_lf_rv.adapter = mRVAdapter
        tools_lf_rv.setHasFixedSize(true)

        setListener()
    }

    override fun onBackPressed() {
        if (mFileSeeker.fileStackSize > 1) {
            backToLastFile()
        } else {
            super.onBackPressed()
        }
    }

    private fun setListener() {
        mRVAdapter?.setOnClickItemFile { _, position ->
            val intent = Intent()
            intent.putExtra("path", arrayOf(mFileList[position].absolutePath))
            this.setResult(Activity.RESULT_OK, intent)
            this.finish()
        }
        mRVAdapter?.setOnClickItemFolder { _, position ->
            toFile(mFileList[position].absolutePath)
        }
        mRVAdapter?.setOnLongClickItem { _, _ ->
            mRVAdapter?.notifyDataSetChanged()
            tools_lf_bar_normal.visibility = View.GONE
            tools_lf_bar_multi.visibility = View.VISIBLE
        }
        tools_lf_bar_back.setOnClickListener {
            this.finish()
        }
        tools_lf_bar_cancel.setOnClickListener {
            if (mRVAdapter?.multiSelectMode == true) {
                mRVAdapter?.multiSelectMode = false
                mRVAdapter?.notifyDataSetChanged()
                tools_lf_bar_multi.visibility = View.GONE
                tools_lf_bar_normal.visibility = View.VISIBLE
            }
        }
        tools_lf_bar_done.setOnClickListener {
            mRVAdapter?.let {
                if (it.multiSelectMode) {
                    val path = Array(it.selectList.size){ "" }
                    for (i in 0..(it.selectList.size - 1)) {
                        path[i] = mFileList[it.selectList[i]].absolutePath
                    }
                    val intent = Intent()
                    intent.putExtra("path", path)
                    this.setResult(Activity.RESULT_OK, intent)
                    this.finish()
                }
            }
        }
        tools_lf_search_BT.setOnClickListener {
            val text = tools_lf_search_ET.text.toString()
            if (text.isNotEmpty()) {
                mFileList.clear()
                mRVAdapter?.notifyDataSetChanged()
                mFileSeeker.findFile(text)
            }
        }
        mFileSeeker.setFindFileStartListener {
            mLoadingDialog = LoadingDialog.newInstance()
            mLoadingDialog?.show(supportFragmentManager, "LoadingDialogLocalFiles")
        }
        mFileSeeker.setFindFileFindListener {
            mFileList.add(it)
            mRVAdapter?.notifyItemInserted(mFileList.size - 1)
        }
        mFileSeeker.setFindFileCompleteListener {
            mLoadingDialog?.dismiss()
            mLoadingDialog = null
        }
        mLoadingDialog?.setDismissAction {
            mFileSeeker.disposable?.let {
                if (!it.isDisposed) it.dispose()
            }
        }
    }

    private fun toFile(path: String) {
        mFileSeeker.currentPath = path
        mFileList.clear()
        mFileList.addAll(mFileSeeker.currentDirFiles ?: emptyArray())
        mRVAdapter?.notifyDataSetChanged()
    }

    private fun backToLastFile() {
        mFileSeeker.popFile()
        mFileList.clear()
        mFileList.addAll(mFileSeeker.currentDirFiles ?: emptyArray())
        mRVAdapter?.notifyDataSetChanged()
    }
}
