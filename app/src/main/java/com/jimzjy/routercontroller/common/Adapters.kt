package com.jimzjy.routercontroller.common

import android.content.ContentProvider
import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import com.jimzjy.routercontroller.R
import com.jimzjy.routercontroller.tools.ToolsRecyclerItem
import com.jimzjy.routercontroller.tools.fragments.SettingData
import com.jimzjy.routersshutils.common.DeviceInfo
import java.io.File


class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    private val mFragmentList = arrayListOf<Fragment>()

    override fun getCount(): Int {
        return mFragmentList.size
    }

    override fun getItem(position: Int): Fragment {
        return mFragmentList[position]
    }

    fun addFragment(fragment: Fragment) {
        mFragmentList.add(fragment)
    }
}

class DeviceRecyclerAdapter(ctx: Context, private val deviceList: List<DeviceInfo>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mInflater = LayoutInflater.from(ctx)
    private var mItemClickListener: DeviceItemClickListener? = null

    inner class ViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        val name: TextView = root.findViewById(R.id.item_device_name)
        val ip: TextView = root.findViewById(R.id.item_device_ip)
        val mac: TextView = root.findViewById(R.id.item_device_mac)

        init {
            root.setOnLongClickListener {
                mItemClickListener?.onClick(it, adapterPosition)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(mInflater.inflate(R.layout.item_device, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        viewHolder.name.text = deviceList[position].name
        viewHolder.ip.text = deviceList[position].ip
        viewHolder.mac.text = deviceList[position].mac
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }

    fun setOnItemClickListener(listener: DeviceItemClickListener) {
        this.mItemClickListener = listener
    }
}

class ToolsRecyclerAdapter(ctx: Context, private val itemList: List<ToolsRecyclerItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mInflater = LayoutInflater.from(ctx)
    private var mOnClickItem: ((v: View, position: Int) -> Unit)? = null

    inner class ViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        val name: TextView = root.findViewById(R.id.tools_item_text)
        val icon: ImageView = root.findViewById(R.id.tools_item_icon)

        init {
            root.setOnClickListener { mOnClickItem?.invoke(it, adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(mInflater.inflate(R.layout.item_tools, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        viewHolder.name.text = itemList[position].name
        viewHolder.icon.setImageDrawable(itemList[position].icon)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun setOnClickItem(clickItem: (v: View, position: Int) -> Unit) {
        this.mOnClickItem = clickItem
    }
}

class SettingsRecyclerAdapter(ctx: Context, private val settingList: List<SettingData>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mInflater = LayoutInflater.from(ctx)
    private var mOnClickItem: ((v: View, position: Int) -> Unit)? = null

    inner class ViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        val name: TextView = root.findViewById(R.id.tools_settings_setting_name)
        val value: TextView = root.findViewById(R.id.tools_settings_setting_value)

        init {
            root.setOnClickListener { mOnClickItem?.invoke(it, adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(mInflater.inflate(R.layout.item_setting, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        viewHolder.name.text = settingList[position].name
        viewHolder.value.text = settingList[position].value
    }

    override fun getItemCount(): Int {
        return settingList.size
    }

    fun setOnClickItem(clickItem: (v: View, position: Int) -> Unit) {
        this.mOnClickItem = clickItem
    }
}

class FilesRecyclerAdapter(ctx: Context, private val mFileList: List<File>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mInflater = LayoutInflater.from(ctx)
    private var mOnClickItemFolder: ((v: View, position: Int) -> Unit)? = null
    private var mOnClickItemFile: ((v: View, position: Int) -> Unit)? = null
    private var mOnLongClickItem: ((v: View, position: Int) -> Unit)? = null
    private val mFolderImage = ctx.getDrawable(R.drawable.vector_drawable_folder)
    private val mFileImage = ctx.getDrawable(R.drawable.vector_drawable_file)
    var multiSelectMode = false
    var selectList = mutableListOf<Int>()

    inner class ViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        val icon: ImageView = root.findViewById(R.id.item_file_icon)
        val text: TextView = root.findViewById(R.id.item_file_text)
        val radioBT: RadioButton = root.findViewById(R.id.item_file_radio)

        init {
            root.setOnClickListener {
                if (multiSelectMode) {
                    if (radioBT.isChecked) {
                        radioBT.isChecked = false
                        if (selectList.contains(adapterPosition))
                            selectList.remove(adapterPosition)
                    } else {
                        radioBT.isChecked = true
                        if (!selectList.contains(adapterPosition))
                            selectList.add(adapterPosition)
                    }
                } else {
                    if (mFileList[adapterPosition].isDirectory) {
                        mOnClickItemFolder?.invoke(it, adapterPosition)
                    } else {
                        mOnClickItemFile?.invoke(it, adapterPosition)
                    }
                }
            }
            root.setOnLongClickListener {
                if (!multiSelectMode) {
                    multiSelectMode = true
                    selectList.clear()
                    radioBT.isChecked = true
                    if (!selectList.contains(adapterPosition))
                        selectList.add(adapterPosition)
                    mOnLongClickItem?.invoke(it, adapterPosition)
                }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(mInflater.inflate(R.layout.item_file, parent, false))
    }

    override fun getItemCount(): Int {
        return mFileList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        viewHolder.text.text = mFileList[position].name
        if (selectList.contains(position)) {
            viewHolder.radioBT.isChecked = true
        }
        if (mFileList[position].isDirectory) {
            viewHolder.icon.setImageDrawable(mFolderImage)
        } else {
            viewHolder.icon.setImageDrawable(mFileImage)
        }
        if (!multiSelectMode) {
            viewHolder.radioBT.visibility = View.GONE
        } else {
            viewHolder.radioBT.visibility = View.VISIBLE
        }
    }

    fun setOnClickItemFile(clickItem: (v: View, position: Int) -> Unit) {
        this.mOnClickItemFile = clickItem
    }

    fun setOnClickItemFolder(clickItem: (v: View, position: Int) -> Unit) {
        this.mOnClickItemFolder = clickItem
    }

    fun setOnLongClickItem(clickItem: (v: View, position: Int) -> Unit) {
        this.mOnLongClickItem = clickItem
    }
}

class SelectedFilesRecyclerAdapter(ctx: Context, private val mFileList: List<File>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mInflater = LayoutInflater.from(ctx)
    private val mFolderImage = ctx.getDrawable(R.drawable.vector_drawable_folder)
    private val mFileImage = ctx.getDrawable(R.drawable.vector_drawable_file)

    inner class ViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        val icon: ImageView = root.findViewById(R.id.item_selected_files_icon)
        val name: TextView = root.findViewById(R.id.item_selected_files_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(mInflater.inflate(R.layout.item_selected_files, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        viewHolder.name.text = mFileList[position].name
        if (mFileList[position].isDirectory) {
            viewHolder.icon.setImageDrawable(mFolderImage)
        } else {
            viewHolder.icon.setImageDrawable(mFileImage)
        }
    }

    override fun getItemCount(): Int {
        return mFileList.size
    }
}
