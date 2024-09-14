

package com.duckduckgo.autofill.impl.securestorage.di

import android.content.Context
import com.duckduckgo.autofill.impl.securestorage.DerivedKeySecretFactory
import com.duckduckgo.autofill.impl.securestorage.RealDerivedKeySecretFactory
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.securestorage.store.RealSecureStorageKeyRepository
import com.duckduckgo.securestorage.store.SecureStorageKeyRepository
import com.duckduckgo.securestorage.store.keys.RealSecureStorageKeyStore
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import dagger.SingleInstanceIn
import javax.inject.Named

@Module
@ContributesTo(AppScope::class)
object SecureStorageModule {

    @Provides
    @SingleInstanceIn(AppScope::class)
    fun providesSecureStorageKeyStore(context: Context): SecureStorageKeyRepository =
        RealSecureStorageKeyRepository(RealSecureStorageKeyStore(context))
}

@Module
@ContributesTo(AppScope::class)
object SecureStorageKeyModule {
    @Provides
    @Named("DerivedKeySecretFactoryFor26Up")
    fun provideDerivedKeySecretFactoryFor26Up(): DerivedKeySecretFactory = RealDerivedKeySecretFactory()
}
