package com.jimzjy.routercontroller

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
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
}
