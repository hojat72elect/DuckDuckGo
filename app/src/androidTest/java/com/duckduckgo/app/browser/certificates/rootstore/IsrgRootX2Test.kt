

package com.duckduckgo.app.browser.certificates.rootstore

import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.app.browser.certificates.CertificateType
import okio.ByteString.Companion.toByteString
import org.junit.Assert.assertEquals
import org.junit.Test

class IsrgRootX2Test {
    private val expectedCertificate = """
        -----BEGIN CERTIFICATE-----
        MIICGzCCAaGgAwIBAgIQQdKd0XLq7qeAwSxs6S+HUjAKBggqhkjOPQQDAzBPMQsw
        CQYDVQQGEwJVUzEpMCcGA1UEChMgSW50ZXJuZXQgU2VjdXJpdHkgUmVzZWFyY2gg
        R3JvdXAxFTATBgNVBAMTDElTUkcgUm9vdCBYMjAeFw0yMDA5MDQwMDAwMDBaFw00
        MDA5MTcxNjAwMDBaME8xCzAJBgNVBAYTAlVTMSkwJwYDVQQKEyBJbnRlcm5ldCBT
        ZWN1cml0eSBSZXNlYXJjaCBHcm91cDEVMBMGA1UEAxMMSVNSRyBSb290IFgyMHYw
        EAYHKoZIzj0CAQYFK4EEACIDYgAEzZvVn4CDCuwJSvMWSj5cz3es3mcFDR0HttwW
        +1qLFNvicWDEukWVEYmO6gbf9yoWHKS5xcUy4APgHoIYOIvXRdgKam7mAHf7AlF9
        ItgKbppbd9/w+kHsOdx1ymgHDB/qo0IwQDAOBgNVHQ8BAf8EBAMCAQYwDwYDVR0T
        AQH/BAUwAwEB/zAdBgNVHQ4EFgQUfEKWrt5LSDv6kviejM9ti6lyN5UwCgYIKoZI
        zj0EAwMDaAAwZQIwe3lORlCEwkSHRhtFcP9Ymd70/aTSVaYgLXTWNLxBo1BfASdW
        tL4ndQavEi51mI38AjEAi/V3bNTIZargCyzuFJ0nN6T5U6VR5CmD1/iQMVtCnwr1
        /q4AaOeMSQ+2b1tbFfLn
        -----END CERTIFICATE-----
    """.trimIndent().parsePemCertificate()

    private val certificatePlugin = IsrgRootX2(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext)

    @Test
    fun whenIsrgRootX1TypeThenReturnRoot() {
        assertEquals(certificatePlugin.type(), CertificateType.Root)
    }

    @Test
    fun whenGetCertificateThenCertificateIsCorrect() {
        assertEquals(certificatePlugin.certificate().encoded.toByteString(), expectedCertificate)
    }
}
