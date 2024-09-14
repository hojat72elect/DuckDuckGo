

package com.duckduckgo.autofill.impl.internal

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.appbuildconfig.api.BuildFlavor.INTERNAL
import com.duckduckgo.appbuildconfig.api.BuildFlavor.PLAY
import com.duckduckgo.autofill.store.InternalTestUserStore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class RealInternalTestUserCheckerTest {
    @Mock
    private lateinit var appBuildConfig: AppBuildConfig
    private lateinit var internalTestUserStore: InternalTestUserStore
    private lateinit var testee: RealInternalTestUserChecker

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        internalTestUserStore = FakeInternalTestUserStore()
        testee = RealInternalTestUserChecker(internalTestUserStore, appBuildConfig)

        whenever(appBuildConfig.flavor).thenReturn(PLAY)
    }

    @Test
    fun whenErrorReceivedForInvalidUrlThenisNotInternalTestUser() {
        testee.verifyVerificationErrorReceived(INVALID_TEST_URL)
        testee.verifyVerificationCompleted(INVALID_TEST_URL)

        assertFalse(testee.isInternalTestUser)
    }

    @Test
    fun whenCompletedForInvalidUrlThenisNotInternalTestUser() {
        testee.verifyVerificationCompleted(INVALID_TEST_URL)

        assertFalse(testee.isInternalTestUser)
    }

    @Test
    fun whenErrorForValidButHttpUrlThenisNotInternalTestUser() {
        testee.verifyVerificationErrorReceived(VALID_TEST_HTTP_URL)
        testee.verifyVerificationCompleted(VALID_TEST_HTTPS_URL)

        assertFalse(testee.isInternalTestUser)
    }

    @Test
    fun whenErrorReceivedForValidUrlThenisNotInternalTestUser() {
        testee.verifyVerificationErrorReceived(VALID_TEST_HTTPS_URL)
        testee.verifyVerificationCompleted(VALID_TEST_HTTPS_URL)

        assertFalse(testee.isInternalTestUser)
    }

    @Test
    fun whenCompletedForValidUrlThenisInternalTestUser() {
        testee.verifyVerificationCompleted(VALID_TEST_HTTPS_URL)

        assertTrue(testee.isInternalTestUser)
    }

    @Test
    fun whenCompletedForValidButHttpUrlThenisInternalTestUser() {
        testee.verifyVerificationCompleted(VALID_TEST_HTTP_URL)

        assertTrue(testee.isInternalTestUser)
    }

    @Test
    fun whenUserBuildIsInternalNoValidationThenIsInternalTestUser() {
        whenever(appBuildConfig.flavor).thenReturn(INTERNAL)

        assertTrue(testee.isInternalTestUser)
    }

    @Test
    fun whenUserBuildIsInternalAndErrorReceivedWhenValidatingThenIsInternalTestUser() {
        whenever(appBuildConfig.flavor).thenReturn(INTERNAL)

        testee.verifyVerificationErrorReceived(VALID_TEST_HTTPS_URL)
        testee.verifyVerificationCompleted(VALID_TEST_HTTPS_URL)

        assertTrue(testee.isInternalTestUser)
    }

    class FakeInternalTestUserStore : InternalTestUserStore {
        private var _value: Boolean = false
        override var isVerifiedInternalTestUser: Boolean
            get() = _value
            set(value) {
                _value = value
            }
    }

    companion object {
        private const val VALID_TEST_HTTP_URL = "http://use-login.duckduckgo.com/patestsucceeded"
        private const val VALID_TEST_HTTPS_URL = "https://use-login.duckduckgo.com/patestsucceeded"
        private const val INVALID_TEST_URL = "https://invalid.com"
    }
}
