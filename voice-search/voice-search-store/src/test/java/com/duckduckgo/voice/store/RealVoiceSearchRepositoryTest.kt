

package com.duckduckgo.voice.store

import com.duckduckgo.voice.api.VoiceSearchStatusListener
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RealVoiceSearchRepositoryTest {

    private lateinit var testee: RealVoiceSearchRepository
    private var dataSource = FakeVoiceSearchDataStore()
    private var voiceSearchStatusListener = FakeVoiceSearchStatusListener()

    @Before
    fun setUp() {
        testee = RealVoiceSearchRepository(dataSource, voiceSearchStatusListener)
    }

    @Test
    fun whenRationalDialogIsAcceptedThenGetHasAcceptedRationaleDialogShouldBeTrue() {
        assertFalse(testee.getHasAcceptedRationaleDialog())

        testee.acceptRationaleDialog()

        assertTrue(testee.getHasAcceptedRationaleDialog())
    }

    @Test
    fun whenPermissionDeclinedForeverThenGetHasPermissionDeclinedForeverShouldBeTrue() {
        assertFalse(testee.getHasPermissionDeclinedForever())

        testee.declinePermissionForever()

        assertTrue(testee.getHasPermissionDeclinedForever())
    }

    @Test
    fun whenAvailabilityIsLoggedThengetHasLoggedAvailabilityShouldBeTrue() {
        assertFalse(testee.getHasLoggedAvailability())

        testee.saveLoggedAvailability()

        assertTrue(testee.getHasLoggedAvailability())
    }

    @Test
    fun whenSetVoiceSearchEnabledThenIsVoiceSearchEnabledShouldBeTrue() {
        assertFalse(testee.isVoiceSearchUserEnabled(false))

        testee.setVoiceSearchUserEnabled(true)

        assertTrue(testee.isVoiceSearchUserEnabled(false))
    }

    @Test
    fun whenSetVoiceSearchEnabledThenListenerShouldBeCalled() {
        testee.setVoiceSearchUserEnabled(true)

        assertTrue(voiceSearchStatusListener.statusChanged)
    }

    @Test
    fun whenDismissVoiceSearchThenCountVoiceSearchDismissedValueShouldIncrease() {
        assertEquals(0, testee.countVoiceSearchDismissed())

        testee.dismissVoiceSearch()

        assertEquals(1, testee.countVoiceSearchDismissed())
    }
}

class FakeVoiceSearchDataStore : VoiceSearchDataStore {
    override var permissionDeclinedForever: Boolean = false
    override var userAcceptedRationaleDialog: Boolean = false
    override var availabilityLogged: Boolean = false
    override var countVoiceSearchDismissed: Int = 0

    private var _voiceSearchEnabled = false

    override fun isVoiceSearchEnabled(default: Boolean): Boolean {
        return _voiceSearchEnabled
    }

    override fun setVoiceSearchEnabled(value: Boolean) {
        _voiceSearchEnabled = value
    }
}

class FakeVoiceSearchStatusListener : VoiceSearchStatusListener {

    var statusChanged: Boolean = false
        private set
    override fun voiceSearchStatusChanged() {
        statusChanged = true
    }
}
