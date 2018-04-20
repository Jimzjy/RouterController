package com.jimzjy.dialog

import android.content.Context
import android.opengl.Visibility
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView

class CommandListAdapter(ctx: Context, private val itemList: List<CommandData>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mInflater = LayoutInflater.from(ctx)
    private var mOnClickItem: ((v: View, position: Int) -> Unit)? = null
    private var mOnLongClickItem: ((v: View, position: Int) -> Unit)? = null
    var multiSelectMode = false

    inner class ViewHolder(root: View): RecyclerView.ViewHolder(root) {
        val name: TextView = root.findViewById(R.id.item_command_name)
        val content: TextView = root.findViewById(R.id.item_command_content)
        val selectButton: RadioButton = root.findViewById(R.id.item_command_radioButton)

        init {
            root.setOnClickListener {
                mOnClickItem?.invoke(it, adapterPosition)
                if (multiSelectMode) {
                    selectButton.isChecked = !selectButton.isChecked
                }
            }
            root.setOnLongClickListener {
                if (!multiSelectMode){
                    selectButton.isChecked = true
                    mOnLongClickItem?.invoke(it, adapterPosition)
                }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(mInflater.inflate(R.layout.layout_dialog_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        viewHolder.name.text = itemList[position].name
        viewHolder.content.text = itemList[position].content
        if (!multiSelectMode) {
            viewHolder.selectButton.visibility = View.INVISIBLE
        } else {
            viewHolder.selectButton.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun setOnClickItem(clickItem: (v: View, position: Int) -> Unit) {
        this.mOnClickItem = clickItem
    }

    fun setOnLongClickItem(clickItem: (v: View, position: Int) -> Unit) {
        this.mOnLongClickItem = clickItem
    }
}