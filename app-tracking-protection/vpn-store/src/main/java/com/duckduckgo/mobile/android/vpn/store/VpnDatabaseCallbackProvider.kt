

package com.duckduckgo.mobile.android.vpn.store

import android.content.Context
import androidx.room.RoomDatabase
import com.duckduckgo.common.utils.DefaultDispatcherProvider
import javax.inject.Provider

class VpnDatabaseCallbackProvider constructor(
    private val context: Context,
    private val vpnDatabaseProvider: Provider<VpnDatabase>,
) {
    fun provideCallbacks(): RoomDatabase.Callback {
        return VpnDatabaseCallback(context, vpnDatabaseProvider, DefaultDispatcherProvider())
    }
}
