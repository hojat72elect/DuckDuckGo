

package com.duckduckgo.sync.store

import android.content.Context
import android.content.SharedPreferences

class TestSharedPrefsProvider(val context: Context) : SharedPrefsProvider {
    override fun getEncryptedSharedPrefs(fileName: String): SharedPreferences? {
        return context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
    }

    override fun getSharedPrefs(fileName: String): SharedPreferences {
        return context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
    }
}
