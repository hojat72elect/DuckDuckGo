

package com.duckduckgo.privacy.dashboard.impl.di

import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.privacy.dashboard.impl.ui.PrivacyDashboardHybridViewModel.RequestState
import com.duckduckgo.privacy.dashboard.impl.ui.PrivacyDashboardHybridViewModel.RequestState.Allowed
import com.duckduckgo.privacy.dashboard.impl.ui.PrivacyDashboardHybridViewModel.RequestState.Blocked
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.SingleInstanceIn
import javax.inject.Named

@Module
@ContributesTo(AppScope::class)
object JsonModule {

    @Provides
    @SingleInstanceIn(AppScope::class)
    @Named("privacyDashboard")
    fun moshi(moshi: Moshi): Moshi {
        return moshi.newBuilder()
            .add(
                PolymorphicJsonAdapterFactory.of(RequestState::class.java, "state")
                    .withSubtype(Blocked::class.java, "blocked")
                    .withSubtype(Allowed::class.java, "allowed"),
            )
            .build()
    }
}
