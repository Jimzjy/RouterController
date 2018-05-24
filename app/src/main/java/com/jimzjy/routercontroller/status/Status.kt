package com.jimzjy.routercontroller.status

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jimzjy.dialog.EDIT_DATA
import com.jimzjy.dialog.EditDialog
import com.jimzjy.networkspeedview.NetworkSpeedView
import com.jimzjy.routercontroller.common.DeviceRecyclerAdapter
import com.jimzjy.routercontroller.R
import com.jimzjy.routercontroller.common.ReconnectClickListener
import com.jimzjy.routersshutils.common.DeviceInfo
import java.util.*


/**
 *
 */
class Status : Fragment(), StatusView, ReconnectClickListener {
    private var mDeviceList: ArrayList<DeviceInfo> = arrayListOf()
    private var mRecyclerAdapter: DeviceRecyclerAdapter? = null
    private var mNetworkSpeedView: NetworkSpeedView? = null
    private var mCoordinatorLayout: StatusCoordinatorLayout? = null
    private var mSpeedBar: ConstraintLayout? = null
    private var mSpeedBarLeftText: TextView? = null
    private var mSpeedBarRightText: TextView? = null
    private var mStatusPresenter: StatusPresenter? = null
    private var mDevText: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_status, container, false)

        mStatusPresenter = StatusPresenterImpl(this@Status, context)

        mRecyclerAdapter = DeviceRecyclerAdapter(context!!, mDeviceList)
        val recyclerView = view.findViewById<RecyclerView>(R.id.status_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mRecyclerAdapter
        recyclerView.setHasFixedSize(true)

        mNetworkSpeedView = view.findViewById(R.id.status_network_speed_view)
        mSpeedBar = view.findViewById(R.id.status_constraint_layout)
        mSpeedBarLeftText = view.findViewById(R.id.status_constraint_left_textView)
        mSpeedBarRightText = view.findViewById(R.id.status_constraint_right_textView)

        mCoordinatorLayout = view.findViewById(R.id.status_CoordinatorLayout)
        mCoordinatorLayout?.setShowText { mSpeedBar?.alpha = it }

        mDevText = view.findViewById(R.id.status_dev_text)
        mDevText?.setOnClickListener {
            val editDialog = EditDialog.newInstance(
                    "${resources.getString(R.string.select_dev)}${mStatusPresenter?.getDevArray()?.joinToString(" ")}",
                    mStatusPresenter?.getDev() ?: "", resources.getString(R.string.commit), false, 0)
            editDialog.setTargetFragment(this@Status, 0)
            editDialog.show(fragmentManager, "EditDialog")
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        mStatusPresenter?.onStart()
        mDevText?.text = String.format(resources.getString(R.string.DeviceWithColon), mStatusPresenter?.getDev())
    }

    override fun onStop() {
        super.onStop()
        mStatusPresenter?.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mStatusPresenter?.onDestroyView()
    }

    override fun updateDevicesList(deviceList: List<DeviceInfo>) {
        this.mDeviceList.clear()
        this.mDeviceList.addAll(deviceList)
        mRecyclerAdapter?.notifyDataSetChanged()
    }

    override fun setSpeedArray(speedArray: FloatArray) {
        mNetworkSpeedView?.setSpeedArray(speedArray)
    }

    override fun setSpeedReverse(isReverse: Boolean) {
        mNetworkSpeedView?.isReverseSpeed = isReverse
    }

    override fun updateSpeedBar() {
        val speedArray = mNetworkSpeedView?.speedArray ?: arrayOf("▲ 0.0 B/s", "▼ 0.0 B/s")
        mSpeedBarLeftText?.text = speedArray[1]
        mSpeedBarRightText?.text = speedArray[0]
    }

    override fun onClickReconnect() {
        mStatusPresenter?.onClickReconnect()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            val dev = data.getStringArrayExtra(EDIT_DATA)[1]
            mStatusPresenter?.setDev(dev)
            mDevText?.text = dev
        }
    }
}
