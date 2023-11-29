package com.ngxqt.classmanagementmvvm.utils

import android.content.SharedPreferences

object ClassPreference {
        private val mPref: SharedPreferences? = null

        fun setString(key: String?, value: String?) {
            if (key == null) {
                return
            }

            val edit: SharedPreferences.Editor? = mPref?.edit()
            edit?.putString(key, value)
            edit?.commit()
        }

        fun getString(key: String?): String? {
            if (mPref == null || !mPref.contains(key)) {
                val edit: SharedPreferences.Editor? = mPref?.edit()
                edit?.putString(key, null)
                edit?.commit()
            }
            return mPref?.getString(key, null)
        }
}