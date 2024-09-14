

package com.duckduckgo.autofill.sync

import com.duckduckgo.autofill.sync.CredentialsFixtures.toLoginCredentialEntryResponse
import com.duckduckgo.autofill.sync.CredentialsFixtures.twitterCredentials
import com.duckduckgo.common.utils.formatters.time.DatabaseDateFormatter
import com.duckduckgo.sync.api.SyncCrypto
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

internal class CredentialsSyncMapperTest {

    @Test
    fun whenMapRemoteLoginCredentialThenLoginCredentials() {
        val syncCrypto = FakeCrypto()
        val credentialsSyncMapper = CredentialsSyncMapper(syncCrypto)
        val toLoginCredentialEntryResponse = twitterCredentials.toLoginCredentialEntryResponse()

        val loginCredential = credentialsSyncMapper.toLoginCredential(
            remoteEntry = toLoginCredentialEntryResponse,
            localId = 1L,
            lastModified = "2021-01-01T00:00:00.000Z",
        )

        val timeInMillis = DatabaseDateFormatter.parseIso8601ToMillis("2021-01-01T00:00:00.000Z")

        assertEquals(1L, loginCredential.id)
        assertEquals(twitterCredentials.domain, loginCredential.domain)
        assertEquals(twitterCredentials.username, loginCredential.username)
        assertEquals(twitterCredentials.password, loginCredential.password)
        assertEquals(twitterCredentials.domainTitle, loginCredential.domainTitle)
        assertEquals(twitterCredentials.notes, loginCredential.notes)
        assertEquals(timeInMillis, loginCredential.lastUpdatedMillis)
    }

    @Test
    fun whenMapRemoteLoginCredentialThenEnsureFieldsDecrypted() {
        val syncCrypto = mock<SyncCrypto>()
        val credentialsSyncMapper = CredentialsSyncMapper(syncCrypto)
        val toLoginCredentialEntryResponse = twitterCredentials.toLoginCredentialEntryResponse()

        credentialsSyncMapper.toLoginCredential(
            remoteEntry = toLoginCredentialEntryResponse,
            localId = 1L,
            lastModified = "2021-01-01T00:00:00.000Z",
        )

        verify(syncCrypto, times(5)).decrypt(anyString())
    }
}
