package com.duckduckgo.privacyprotectionspopup.impl.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.privacyprotectionspopup.impl.PrivacyProtectionsPopupExperimentVariant.CONTROL
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class PrivacyProtectionsPopupDataStoreTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    private val testDataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = coroutineRule.testScope,
            produceFile = { context.preferencesDataStoreFile("privacy_protections_popup") },
        )

    private val subject: PrivacyProtectionsPopupDataStore =
        PrivacyProtectionsPopupDataStoreImpl(testDataStore)

    @Test
    fun whenDatabaseIsEmptyThenReturnsNullDismissTimestamp() = runTest {
        assertNull(subject.getToggleUsageTimestamp())
    }

    @Test
    fun whenDismissTimeIsStoredThenQueryReturnsCorrectValue() = runTest {
        val timestamp = Instant.parse("2023-11-29T10:15:30.000Z")

        subject.setToggleUsageTimestamp(timestamp)
        val storedTimestamp = subject.getToggleUsageTimestamp()
        assertEquals(timestamp, storedTimestamp)
    }

    @Test
    fun whenDismissTimeIsSetMultipleTimesThenReturnsMostRecentlyStoredValue() = runTest {
        subject.setToggleUsageTimestamp(Instant.parse("2023-11-28T10:15:30.000Z"))
        subject.setToggleUsageTimestamp(Instant.parse("2023-11-29T10:15:30.000Z"))
        subject.setToggleUsageTimestamp(Instant.parse("2023-11-10T10:15:30.000Z"))

        val storedTimestamp = subject.getToggleUsageTimestamp()
        assertEquals(Instant.parse("2023-11-10T10:15:30.000Z"), storedTimestamp)
    }

    @Test
    fun whenPopupTriggerCountIsNotInitializedThenReturnsZero() = runTest {
        assertEquals(0, subject.getPopupTriggerCount())
    }

    @Test
    fun whenPopupTriggerCountIsStoredThenReturnsCorrectValue() = runTest {
        val count = 123
        subject.setPopupTriggerCount(count)
        val storedCount = subject.getPopupTriggerCount()
        assertEquals(count, storedCount)
    }

    @Test
    fun whenDoNotShowAgainIsNotInitializedThenReturnsFalse() = runTest {
        assertFalse(subject.getDoNotShowAgainClicked())
    }

    @Test
    fun whenDoNotShowAgainIsStoredThenReturnsCorrectValue() = runTest {
        subject.setDoNotShowAgainClicked(clicked = true)
        assertTrue(subject.getDoNotShowAgainClicked())
    }

    @Test
    fun whenExperimentVariantIsNotInitializedThenReturnsNull() = runTest {
        assertNull(subject.getExperimentVariant())
    }

    @Test
    fun whenExperimentVariantIsStoredThenReturnsCorrectValue() = runTest {
        subject.setExperimentVariant(CONTROL)
        assertEquals(CONTROL, subject.getExperimentVariant())
    }
}
