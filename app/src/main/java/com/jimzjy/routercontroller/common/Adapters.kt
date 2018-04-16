package com.jimzjy.routercontroller.common

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.jimzjy.routercontroller.R
import com.jimzjy.routercontroller.tools.ToolsRecyclerItem
import com.jimzjy.routersshutils.common.DeviceInfo


class ViewPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {
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

class DeviceRecyclerAdapter(ctx: Context, private val deviceList: List<DeviceInfo>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mInflater = LayoutInflater.from(ctx)
    private var mItemClickListener: DeviceItemClickListener? = null

    inner class ViewHolder(root: View): RecyclerView.ViewHolder(root) {
        val name: TextView = root.findViewById(R.id.item_device_name)
        val ip: TextView = root.findViewById(R.id.item_device_ip)
        val mac:TextView = root.findViewById(R.id.item_device_mac)

        init {
            root.setOnLongClickListener {
                mItemClickListener?.onClick(it, adapterPosition)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(mInflater.inflate(R.layout.item_device, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
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

class ToolsRecyclerAdapter(ctx: Context, private val itemList: List<ToolsRecyclerItem>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mInflater = LayoutInflater.from(ctx)
    private var mOnClickItem: ((v: View, position: Int) -> Unit)? = null

    inner class ViewHolder(root: View): RecyclerView.ViewHolder(root) {
        val name: TextView = root.findViewById(R.id.tools_item_text)
        val icon: ImageView = root.findViewById(R.id.tools_item_icon)

        init {
            root.setOnClickListener { mOnClickItem?.invoke(it, adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(mInflater.inflate(R.layout.item_tools, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
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
