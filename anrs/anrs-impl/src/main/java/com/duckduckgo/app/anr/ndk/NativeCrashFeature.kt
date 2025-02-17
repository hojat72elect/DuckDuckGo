package com.duckduckgo.app.anr.ndk

import android.content.SharedPreferences
import androidx.core.content.edit
import com.duckduckgo.anvil.annotations.ContributesRemoteFeature
import com.duckduckgo.app.di.AppCoroutineScope
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.RemoteFeatureStoreNamed
import com.duckduckgo.feature.toggles.api.Toggle
import com.duckduckgo.feature.toggles.api.Toggle.State
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.SingleInstanceIn
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@ContributesRemoteFeature(
    scope = AppScope::class,
    featureName = "androidNativeCrash",
    toggleStore = NativeCrashFeatureMultiProcessStore::class,
)
interface NativeCrashFeature {
    @Toggle.DefaultValue(true)
    @Toggle.InternalAlwaysEnabled
    fun self(): Toggle

    @Toggle.DefaultValue(true)
    fun nativeCrashHandling(): Toggle

    @Toggle.DefaultValue(true)
    fun nativeCrashHandlingSecondaryProcess(): Toggle
}

@ContributesBinding(AppScope::class)
@SingleInstanceIn(AppScope::class)
@RemoteFeatureStoreNamed(NativeCrashFeature::class)
class NativeCrashFeatureMultiProcessStore @Inject constructor(
    @AppCoroutineScope private val coroutineScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val sharedPreferencesProvider: com.duckduckgo.data.store.api.SharedPreferencesProvider,
    moshi: Moshi,
) : Toggle.Store {

    private val preferences: SharedPreferences by lazy {
        sharedPreferencesProvider.getSharedPreferences(
            PREFS_FILENAME,
            multiprocess = true,
            migrate = false
        )
    }

    private val stateAdapter: JsonAdapter<State> by lazy {
        moshi.newBuilder().add(KotlinJsonAdapterFactory()).build().adapter(State::class.java)
    }

    override fun set(key: String, state: State) {
        coroutineScope.launch(dispatcherProvider.io()) {
            preferences.edit(commit = true) { putString(key, stateAdapter.toJson(state)) }
        }
    }

    override fun get(key: String): State? {
        return preferences.getString(key, null)?.let {
            stateAdapter.fromJson(it)
        }
    }

    companion object {
        private const val PREFS_FILENAME = "com.duckduckgo.app.androidNativeCrash.feature.v1"
    }
}
