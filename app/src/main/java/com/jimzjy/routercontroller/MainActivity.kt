package com.jimzjy.routercontroller

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.widget.PopupMenu
import com.jimzjy.routercontroller.about.AboutActivity
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
        viewPagerAdapter.addFragment(Status(), resources.getString(R.string.status))
        viewPagerAdapter.addFragment(Tools(), resources.getString(R.string.tools))
        main_view_pager.adapter = viewPagerAdapter

        main_tab_layout.setupWithViewPager(main_view_pager)
    }

    private fun setListener() {
        main_menu_button.setOnClickListener {
            main_drawer_layout.openDrawer(GravityCompat.START)
        }

        main_navigation_view.setNavigationItemSelectedListener {
            main_drawer_layout.closeDrawers()
            when(it.itemId) {
                R.id.menu_reconnect -> {
                    val fragmentList = supportFragmentManager.fragments
                    fragmentList.forEach {
                        if (it.tag !in Tools.fragmentTags) {
                            (it as ReconnectClickListener).onClickReconnect()
                        }
                    }
                }
                R.id.menu_settings -> {
                    startActivity(Intent(this@MainActivity, Settings::class.java))
                }
                R.id.menu_help -> {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Jimzjy/RouterController/wiki")))
                }
                R.id.menu_about -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                }
            }
            true
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
