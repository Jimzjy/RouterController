package com.jimzjy.routercontroller.tools.fragments


import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView

import com.jimzjy.routercontroller.R
import com.jimzjy.routercontroller.common.SelectedFilesRecyclerAdapter
import com.jimzjy.routercontroller.tools.LocalFilesActivity
import com.jimzjy.routercontroller.tools.ToolsPresenter
import com.jimzjy.routersshutils.common.SSHUtilsException
import com.jimzjy.routersshutils.common.SftpProgress
import java.io.File

/**
 * A simple [Fragment] subclass.
 *
 */
const val REQUEST_LOCAL_FILE = 1

class FileFragment : Fragment() {
    private var mToolsPresenter: ToolsPresenter? = null
    private var mRVAdapter: SelectedFilesRecyclerAdapter? = null
    private val mFileList = mutableListOf<File>()
    private var mText = ""
    private var mRecyclerView: RecyclerView? = null
    private var mCommitButton: Button? = null
    private var mDestinationET: EditText? = null
    private var mDisplayText: TextView? = null
    private var mSelectFileBT: Button? = null

    companion object {
        @JvmStatic
        fun newInstance(toolsPresenter: ToolsPresenter?)
            = FileFragment().apply {
            this.mToolsPresenter = toolsPresenter
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_file, container, false)

        mRecyclerView = view.findViewById(R.id.tools_file_rv)
        mCommitButton = view.findViewById(R.id.tools_file_commit_BT)
        mDestinationET = view.findViewById(R.id.tools_file_dst_ET)
        mDisplayText = view.findViewById(R.id.tools_file_out_text)
        mSelectFileBT = view.findViewById(R.id.tools_file_select_BT)

        mRVAdapter = SelectedFilesRecyclerAdapter(context!!, mFileList)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        mRecyclerView?.adapter = mRVAdapter
        mRecyclerView?.layoutManager = layoutManager
        mRecyclerView?.setHasFixedSize(true)

        setListener()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mToolsPresenter = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LOCAL_FILE  && resultCode == Activity.RESULT_OK && data != null) {
            val path = data.getStringArrayExtra("path")
            path?.let {
                resetFileList(it)
                mRVAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun setListener() {
        mSelectFileBT?.setOnClickListener {
            startActivityForResult(Intent(context, LocalFilesActivity::class.java), REQUEST_LOCAL_FILE)
        }
        mCommitButton?.setOnClickListener {
            clearDisplayText()
            if (mToolsPresenter?.isConnected() != true) {
                addDisplayText(resources.getString(R.string.not_connect), RED_TEXT)
                return@setOnClickListener
            }
            if (mFileList.isEmpty()) {
                addDisplayText(resources.getString(R.string.no_file_upload), RED_TEXT)
                return@setOnClickListener
            }
            if (mDestinationET?.text.toString().isEmpty()) {
                addDisplayText(resources.getString(R.string.no_dst_path), RED_TEXT)
                return@setOnClickListener
            }
            val handler = Handler()
            Thread(Runnable {
                mFileList.forEach {
                    val file = it
                    mToolsPresenter?.let {
                        if (it.isConnected()) {
                            try {
                                it.sftpTo(mDestinationET?.text.toString(), file, object : SftpProgress {
                                    override fun count(count: Long): Boolean {
                                        handler.post {
                                            countAction(count)
                                        }
                                        return true
                                    }
                                    override fun end() {
                                        handler.post {
                                            endAction()
                                        }
                                    }
                                    override fun init(op: Int, src: String?, dest: String?, max: Long) {
                                        handler.post {
                                            initAction(op, src, dest, max)
                                        }
                                    }
                                }, {
                                    addDisplayText("Upload: Finish!")
                                })
                            } catch (e: SSHUtilsException) {
                                e.printStackTrace()
                                handler.post {
                                    errorAction(e)
                                }
                            }
                        }
                    }
                }
            }).start()
        }
    }

    private fun initAction(op: Int, src: String?, dest: String?, max: Long) {
        addDisplayText("$src -> $dest", GREEN_TEXT)
    }

    private fun countAction(count: Long) {
        setNextDisplayText("Uploading: $count")
    }

    private fun endAction() {
        addDisplayText("Upload: End")
    }

    private fun errorAction(e: Exception) {
        addDisplayText("Upload Error: ${e.message}", RED_TEXT)
    }

    private fun setNextDisplayText(text: String, color: String = NORMAL_TEXT) {
        val tmpText = mText + when(color) {
            NORMAL_TEXT -> {
                "<p>$text</p>"
            }
            else -> {
                "<p style=\"color:$color\">$text</p>"
            }
        }
        mDisplayText?.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(tmpText, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(tmpText)
        }
    }

    private fun addDisplayText(text: String, color: String = NORMAL_TEXT, display: Boolean = true) {
        mText += when(color) {
            NORMAL_TEXT -> {
                "<p>$text</p>"
            }
            else -> {
                "<p><font color=\"$color\">$text</font></p>"
            }
        }
        if (display) {
            mDisplayText?.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(mText, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(mText)
            }
        }
    }

    private fun clearDisplayText() {
        mText = ""
        mDisplayText?.text = mText
    }

    private fun resetFileList(path: Array<String>) {
        mFileList.clear()
        path.forEach {
            mFileList.add(File(it))
        }
    }
}
