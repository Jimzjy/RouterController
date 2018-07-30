package com.jimzjy.routercontroller.settings


import android.content.SharedPreferences
import android.os.Bundle
import android.preference.EditTextPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment

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
            if (k != "pref_key_speed_reverse" && k != "pref_key_password") {
                findPreference(k).summary = v.toString()
            }
            if (k == "pref_key_password") {
                val preference = findPreference(k) as EditTextPreference
                val edit = preference.editText
                preference.summary = edit.transformationMethod.getTransformation(preference.text, edit)
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key != "pref_key_speed_reverse" && key != "pref_key_password") {
            val preference = findPreference(key)
            preference.summary = sharedPreferences?.getString(key, "")
        }
        if (key == "pref_key_password") {
            val preference = findPreference(key) as EditTextPreference
            val edit = preference.editText
            preference.summary = edit.transformationMethod.getTransformation(preference.text, edit)
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
