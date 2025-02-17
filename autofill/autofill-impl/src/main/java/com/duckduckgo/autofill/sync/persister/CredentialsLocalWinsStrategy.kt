

package com.duckduckgo.autofill.sync.persister

import com.duckduckgo.autofill.sync.*
import com.duckduckgo.autofill.sync.isDeleted
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.sync.api.engine.SyncMergeResult
import com.duckduckgo.sync.api.engine.SyncMergeResult.Error
import com.duckduckgo.sync.api.engine.SyncMergeResult.Success
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class CredentialsLocalWinsStrategy(
    private val credentialsSync: CredentialsSync,
    private val credentialsSyncMapper: CredentialsSyncMapper,
    private val dispatchers: DispatcherProvider,
) : CredentialsMergeStrategy {
    override fun processEntries(
        credentials: credentialsSyncEntries,
        clientModifiedSince: String,
    ): SyncMergeResult {
        Timber.d("Sync-autofill-Persist: ======= MERGING LOCALWINS =======")
        return kotlin.runCatching {
            runBlocking(dispatchers.io()) {
                credentials.entries.forEach { entry ->
                    val localCredentials = credentialsSync.getCredentialWithSyncId(entry.id)
                    if (localCredentials == null) {
                        processNewEntry(entry, credentials.last_modified)
                    }
                }
            }
        }.getOrElse {
            Timber.d("Sync-autofill-Persist: merging failed with error $it")
            return Error(reason = "LocalWins merge failed with error $it")
        }.let {
            Timber.d("Sync-autofill-Persist: merging completed")
            Success()
        }
    }

    private suspend fun processNewEntry(entry: CredentialsSyncEntryResponse, clientModifiedSince: String) {
        if (entry.isDeleted()) return
        val updatedCredentials = credentialsSyncMapper.toLoginCredential(entry, null, clientModifiedSince)
        Timber.d("Sync-autofill-Persist: >>> no local, save remote $updatedCredentials")
        credentialsSync.saveCredential(updatedCredentials, entry.id)
    }
}
