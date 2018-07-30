package com.jimzjy.routercontroller.settings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.jimzjy.routercontroller.R
import kotlinx.android.synthetic.main.activity_settings.*

class Settings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        fragmentManager.beginTransaction()
                .replace(R.id.settings_content, FragmentSettings())
                .commit()

        settings_toolbar.setNavigationIcon(R.drawable.vector_drawable_baseline_keyboard_arrow_left___px)
        settings_toolbar.setTitleTextColor(ContextCompat.getColor(applicationContext, R.color.appBarTitleText))
        settings_toolbar.setNavigationOnClickListener { onBackPressed() }
    }

}
