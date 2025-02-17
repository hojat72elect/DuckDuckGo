package com.duckduckgo.autofill.impl.ui.credential.management.importpassword.desktopapp

import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.autofill.impl.pixel.AutofillPixelNames.AUTOFILL_IMPORT_PASSWORDS_USER_JOURNEY_SUCCESSFUL
import com.duckduckgo.autofill.impl.pixel.AutofillPixelNames.AUTOFILL_IMPORT_PASSWORDS_USER_JOURNEY_UNSUCCESSFUL
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

interface UserJourneyEndRecorder {
    suspend fun recordSuccessfulJourney()
    suspend fun recordUnsuccessfulJourney()
}

@ContributesBinding(AppScope::class)
class UserJourneyEndRecorderImpl @Inject constructor(
    private val dataStore: ImportPasswordsViaDesktopSyncDataStore,
    private val pixel: Pixel,
) : UserJourneyEndRecorder {

    override suspend fun recordSuccessfulJourney() {
        clearUserJourneyTimestamp()
        sendUserJourneySuccessfulPixel()
    }

    override suspend fun recordUnsuccessfulJourney() {
        clearUserJourneyTimestamp()
        sendUserJourneyUnsuccessfulPixel()
    }

    private fun sendUserJourneySuccessfulPixel() {
        pixel.fire(AUTOFILL_IMPORT_PASSWORDS_USER_JOURNEY_SUCCESSFUL)
    }

    private fun sendUserJourneyUnsuccessfulPixel() {
        pixel.fire(AUTOFILL_IMPORT_PASSWORDS_USER_JOURNEY_UNSUCCESSFUL)
    }

    private suspend fun clearUserJourneyTimestamp() {
        dataStore.clearUserJourneyStartTime()
    }
}
