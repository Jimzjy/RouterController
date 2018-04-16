package com.jimzjy.routercontroller.settings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.jimzjy.routercontroller.R
import kotlinx.android.synthetic.main.activity_settings.*

class Settings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        fragmentManager.beginTransaction()
                .replace(R.id.settings_content, FragmentSettings())
                .commit()

        settings_toolbar.setNavigationIcon(R.drawable.vector_drawable_ic_keyboard_backspace_white___px)
        settings_toolbar.setTitleTextColor(ContextCompat.getColor(applicationContext, R.color.appBarTitleText))
        settings_toolbar.setNavigationOnClickListener { onBackPressed() }
    }

}
