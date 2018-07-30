package com.jimzjy.routercontroller.about

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.artitk.licensefragment.model.CustomUI
import com.artitk.licensefragment.model.License
import com.artitk.licensefragment.model.LicenseType
import com.artitk.licensefragment.support.v4.RecyclerViewLicenseFragment
import com.jimzjy.routercontroller.R
import kotlinx.android.synthetic.main.activity_about.*

/**
 *
 */
class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        about_toolbar.setNavigationIcon(R.drawable.vector_drawable_baseline_keyboard_arrow_left___px)
        about_toolbar.setTitleTextColor(ContextCompat.getColor(applicationContext, R.color.appBarTitleText))
        about_toolbar.setNavigationOnClickListener { onBackPressed() }

        val version = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0).versionName
        about_version.text = String.format(resources.getString(R.string.version), version)

        about_open_source_license.setOnClickListener {
            val fragment = RecyclerViewLicenseFragment.newInstance()
            fragment.addCustomLicense(arrayListOf(
                    License(this, "JSch", LicenseType.BSD_3_CLAUSE, "2002-2015", "Atsuhiko Yamanaka, JCraft,Inc"),
                    License(this, "RxJava", LicenseType.APACHE_LICENSE_20, "2016-present", "RxJava Contributors"),
                    License(this, "RxAndorid", LicenseType.APACHE_LICENSE_20, "2015", "The RxAndroid authors"),
                    License(this, "LoadingDrawable", LicenseType.APACHE_LICENSE_20, "2015-2019", "dinus")))
            fragment.setCustomUI(CustomUI().setLicenseBackgroundColor(ContextCompat.getColor(this, R.color.background)))

            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.about_replace_layout, fragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
    }
}
