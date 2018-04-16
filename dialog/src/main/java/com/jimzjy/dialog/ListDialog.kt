package com.jimzjy.dialog


import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window

/**
 * A simple [Fragment] subclass.
 *
 */
class ListDialog : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = inflater.inflate(R.layout.fragment_list_dialog, container, false)

        val commandList = listOf(CommandData("Test","cat /adsadd"),
                CommandData("Test2","cat /wwww"))
        val listRecyclerView = view.findViewById<RecyclerView>(R.id.list_dialog_rv)
        listRecyclerView.layoutManager = LinearLayoutManager(context)
        listRecyclerView.adapter = CommandListAdapter(context,commandList)

        return view
    }
}
