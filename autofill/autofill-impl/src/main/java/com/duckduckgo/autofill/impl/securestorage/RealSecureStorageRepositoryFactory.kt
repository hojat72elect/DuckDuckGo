

package com.duckduckgo.autofill.impl.securestorage

import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.securestorage.store.RealSecureStorageRepository
import com.duckduckgo.securestorage.store.SecureStorageRepository
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class RealSecureStorageRepositoryFactory @Inject constructor(
    private val secureStorageDatabaseFactory: SecureStorageDatabaseFactory,
) : SecureStorageRepository.Factory {
    override fun get(): SecureStorageRepository? {
        val db = secureStorageDatabaseFactory.getDatabase()
        return if (db != null) {
            RealSecureStorageRepository(db.websiteLoginCredentialsDao(), db.neverSavedSitesDao())
        } else {
            null
        }
    }
}
