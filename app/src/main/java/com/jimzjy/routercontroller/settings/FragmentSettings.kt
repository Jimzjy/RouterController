package com.jimzjy.routercontroller.settings


import android.content.SharedPreferences
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.support.v4.app.Fragment

import com.jimzjy.routercontroller.R




/**
 * A simple [Fragment] subclass.
 */
class FragmentSettings : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.preferences)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        for((k,v) in sharedPreferences.all) {
            if (k != "pref_key_speed_reverse") {
                findPreference(k).summary = v.toString()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key != "pref_key_speed_reverse") {
            val preferences = findPreference(key)
            preferences.summary = sharedPreferences?.getString(key,"")
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}
