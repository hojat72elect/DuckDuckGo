package com.duckduckgo.verifiedinstallation.certificate

import com.duckduckgo.verifiedinstallation.certificate.VerificationCheckBuildCertificateImpl.Companion.PRODUCTION_SHA_256_HASH
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class VerificationCheckBuildCertificateImplTest {

    private val certHashExtractor: SigningCertificateHashExtractor = mock()
    private val testee = VerificationCheckBuildCertificateImpl(certHashExtractor)

    @Test
    fun whenExtractedHashIsNullThenNotAMatch() {
        whenever(certHashExtractor.sha256Hash()).thenReturn(null)
        assertFalse(testee.builtWithVerifiedCertificate())
    }

    @Test
    fun whenExtractedHashIsNotProductionHashThenNotAMatch() {
        whenever(certHashExtractor.sha256Hash()).thenReturn("ABC-123")
        assertFalse(testee.builtWithVerifiedCertificate())
    }

    @Test
    fun whenExtractedHashIsProductionHashThenIsAMatch() {
        whenever(certHashExtractor.sha256Hash()).thenReturn(PRODUCTION_SHA_256_HASH)
        assertTrue(testee.builtWithVerifiedCertificate())
    }
}
