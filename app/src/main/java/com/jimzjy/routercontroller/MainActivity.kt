package com.jimzjy.routercontroller

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.PopupMenu
import com.jimzjy.routercontroller.common.ReconnectClickListener
import com.jimzjy.routercontroller.common.ViewPagerAdapter
import com.jimzjy.routercontroller.settings.Settings
import com.jimzjy.routercontroller.status.Status
import com.jimzjy.routercontroller.tools.Tools
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
        setListener()
        requestPermission()
    }


    private fun init() {
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPagerAdapter.addFragment(Status())
        viewPagerAdapter.addFragment(Tools())
        main_view_pager.adapter = viewPagerAdapter

        main_bottom_navigation.setIconVisibility(false)
        main_bottom_navigation.enableAnimation(false)
        main_bottom_navigation.setTextSize(18f)
        main_bottom_navigation.setupWithViewPager(main_view_pager)
    }

    private fun setListener() {
        main_menu_button.setOnClickListener {
            val popupMenu = PopupMenu(this@MainActivity, main_menu_button)
            popupMenu.menuInflater.inflate(R.menu.menu_appbar_menu_button, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.menu_reconnect -> {
                        val fragmentList = supportFragmentManager.fragments
                        fragmentList.forEach { (it as ReconnectClickListener).onClickReconnect() }
                    }
                    R.id.menu_settings -> {
                        startActivity(Intent(this@MainActivity, Settings::class.java))
                    }
                }
                true
            }
            popupMenu.show()
        }
    }

    private fun requestPermission() {
        val permissions = arrayOf(Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_EXTERNAL_STORAGE)
        val notGetPermissionList = mutableListOf<String>()

        for (p in permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                notGetPermissionList.add(p)
            }
        }

        if (notGetPermissionList.size > 0) {
            ActivityCompat.requestPermissions(this, notGetPermissionList.toTypedArray(), 0)
        }
    }
}
