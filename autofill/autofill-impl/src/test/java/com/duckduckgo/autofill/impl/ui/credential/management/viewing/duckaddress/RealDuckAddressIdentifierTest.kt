

package com.duckduckgo.autofill.impl.ui.credential.management.viewing.duckaddress

import org.junit.Assert.*
import org.junit.Test

class RealDuckAddressIdentifierTest {
    private val testee = RealDuckAddressIdentifier()

    @Test
    fun whenInputIsOnlySuffixThenNotAPrivateAddress() {
        assertFalse(testee.isPrivateDuckAddress("@duck.com", MAIN_ADDRESS))
    }

    @Test
    fun whenInputMatchesMainAddressThenNotAPrivateAddress() {
        assertFalse(testee.isPrivateDuckAddress(MAIN_ADDRESS, MAIN_ADDRESS))
    }

    @Test
    fun whenInputMatchesMainAddressGivenWithSuffixThenNotAPrivateAddress() {
        assertFalse(testee.isPrivateDuckAddress(MAIN_ADDRESS, "test@duck.com"))
    }

    @Test
    fun whenInputDoesNotEndWithSuffixThenNotAPrivateAddress() {
        assertFalse(testee.isPrivateDuckAddress("test@gmail.com", MAIN_ADDRESS))
    }

    @Test
    fun whenMatchesMainAddressExceptInputHasFullAddressAndMainAddressMissingSuffixThenNotAPrivateAddress() {
        assertFalse(testee.isPrivateDuckAddress("test@duck.com", MAIN_ADDRESS))
    }

    @Test
    fun whenInputHasDifferentDuckAddressFromMainAddressThenIsAPrivateAddress() {
        assertTrue(testee.isPrivateDuckAddress("foo@duck.com", MAIN_ADDRESS))
    }

    companion object {
        private const val MAIN_ADDRESS = "test"
    }
}
