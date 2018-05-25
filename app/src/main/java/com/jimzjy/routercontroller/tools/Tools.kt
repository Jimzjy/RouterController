package com.jimzjy.routercontroller.tools


import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

import com.jimzjy.routercontroller.R
import com.jimzjy.routercontroller.common.ReconnectClickListener
import com.jimzjy.routercontroller.common.ToolsRecyclerAdapter
import com.jimzjy.routercontroller.tools.fragments.CommandFragment
import com.jimzjy.routercontroller.tools.fragments.FileFragment
import com.jimzjy.routercontroller.tools.fragments.SettingsChangeFragment
import com.jimzjy.routercontroller.tools.fragments.ShanXunFragment

const val TOOLS_FRAGMENT_TAG_0 = "COMMAND_LINE"
const val TOOLS_FRAGMENT_TAG_1 = "CHANGE_SETTINGS"
const val TOOLS_FRAGMENT_TAG_2 = "UPLOAD_FILES"
const val TOOLS_FRAGMENT_TAG_3 = "SHAN_XUN"
/**
 * A simple [Fragment] subclass.
 *
 */
class Tools : Fragment(), ReconnectClickListener, ToolsView {
    private lateinit var mCloseBar: ConstraintLayout
    private lateinit var mCloseButton: ImageView
    private lateinit var mFrameLayout: FrameLayout
    private lateinit var mToolsPresenter: ToolsPresenter
    private var mFrameLayoutTranslationY = 0f
    private lateinit var mCloseBarText: TextView

    companion object {
        val fragmentTags = arrayOf(TOOLS_FRAGMENT_TAG_0, TOOLS_FRAGMENT_TAG_1, TOOLS_FRAGMENT_TAG_2, TOOLS_FRAGMENT_TAG_3)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tools, container, false)

        mToolsPresenter = ToolsPresenterImpl(this@Tools, context)

        val mToolsRecyclerItem = listOf(
                ToolsRecyclerItem(resources.getString(R.string.command_line),
                        ContextCompat.getDrawable(context!!, R.drawable.vector_drawable_terminal)!!),
                ToolsRecyclerItem(resources.getString(R.string.change_settings),
                        ContextCompat.getDrawable(context!!, R.drawable.vector_drawable_settings)!!),
                ToolsRecyclerItem(resources.getString(R.string.upload_files),
                        ContextCompat.getDrawable(context!!, R.drawable.vector_drawable_files)!!),
                ToolsRecyclerItem(resources.getString(R.string.shanxun),
                        ContextCompat.getDrawable(context!!, R.drawable.vector_drawable_lighting)!!))
        val mToolsRecyclerAdapter = ToolsRecyclerAdapter(context!!, mToolsRecyclerItem)
        val mToolsRecyclerView = view.findViewById<RecyclerView>(R.id.tools_top_widget)
        mToolsRecyclerView.adapter = mToolsRecyclerAdapter
        mToolsRecyclerView.layoutManager = GridLayoutManager(context, 4)
        mToolsRecyclerAdapter.setOnClickItem { v, position -> onClickRecyclerItem(v, position) }

        mFrameLayoutTranslationY = resources.getDimension(R.dimen.tools_top_widget_height)
        mFrameLayout = view.findViewById(R.id.tools_frame_layout)
        mFrameLayout.translationY = mFrameLayoutTranslationY

        mCloseBar = view.findViewById(R.id.tools_close_bar)
        mCloseButton = view.findViewById(R.id.tools_close_bar_icon)
        mCloseBar.setOnClickListener {
            onClickCloseButton(mCloseButton)
        }
        mCloseBarText = view.findViewById(R.id.tools_tool_name_text)

        replaceFragment(CommandFragment.newInstance(mToolsPresenter),
                resources.getString(R.string.command_line), fragmentTags[0], false)
        return view
    }

    override fun onStart() {
        super.onStart()
        mToolsPresenter.onStart()
    }

    override fun onStop() {
        super.onStop()
        mToolsPresenter.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mToolsPresenter.onDestroyView()
    }

    override fun onClickReconnect() {
        mToolsPresenter.onClickReconnect()
    }

    private fun onClickCloseButton(view: ImageView?) {
        frameLayoutTranslation(mFrameLayoutTranslationY)
        changeCloseBarIcon(view)
    }

    private fun changeCloseBarIcon(view: ImageView?) {
        when (mFrameLayout.translationY.compareTo(0f) == 0) {
            true -> {
                view?.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.vector_drawable_ic_remove_white___px))
            }
            false -> {
                view?.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.vector_drawable_ic_add_white___px))
            }
        }
    }

    private fun frameLayoutTranslation(translation: Float) {
        if (mFrameLayout.translationY.compareTo(0f) == 0) {
            mFrameLayout.animate().translationY(translation)
        } else {
            mFrameLayout.animate().translationY(0f)
        }
    }

    private fun onClickRecyclerItem(v: View, position: Int) {
        try {
            when (position) {
                0 -> {
                    replaceFragment(CommandFragment.newInstance(mToolsPresenter), resources.getString(R.string.command_line), fragmentTags[0])
                }
                1 -> {
                    replaceFragment(SettingsChangeFragment.newInstance(mToolsPresenter), resources.getString(R.string.change_settings), fragmentTags[1])
                }
                2 -> {
                    replaceFragment(FileFragment.newInstance(mToolsPresenter), resources.getString(R.string.upload_files), fragmentTags[2])
                }
                3 -> {
                    replaceFragment(ShanXunFragment.newInstance(mToolsPresenter), resources.getString(R.string.shanxun), fragmentTags[3])
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun replaceFragment(fragment: Fragment, name: String, tag: String,isTrans: Boolean = true) {
        val fragmentTransaction = fragmentManager!!.beginTransaction()
//        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        fragmentTransaction.replace(R.id.tools_replace_layout, fragment, tag)
//        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

        mCloseBarText.text = name
        if (isTrans) {
            frameLayoutTranslation(mFrameLayoutTranslationY)
            changeCloseBarIcon(mCloseButton)
        }
    }
}
