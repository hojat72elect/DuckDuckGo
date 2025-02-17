

package com.duckduckgo.app.global.uri

import android.net.Uri
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UriSubdomainRemoverTest {

    @Test
    fun whenRemovingASubdomainWhenTwoAvailableThenOneIsReturned() {
        val converted = Uri.parse("https://a.example.com").removeSubdomain()
        assertEquals("https://example.com", converted)
    }

    @Test
    fun whenRemovingASubdomainWhenFiveAvailableThenFourAreReturned() {
        val converted = Uri.parse("https://a.b.c.d.example.com").removeSubdomain()
        assertEquals("https://b.c.d.example.com", converted)
    }

    @Test
    fun whenRemovingMultipleSubdomainCanKeepCalling() {
        val converted = Uri.parse("https://a.b.c.d.example.com")
            .removeSubdomain()!!
            .toUri().removeSubdomain()!!
            .toUri().removeSubdomain()
        assertEquals("https://d.example.com", converted)
    }

    @Test
    fun whenRemovingASubdomainWhenOnlyOneExistsThenReturnsNull() {
        val converted = Uri.parse("https://example.com").removeSubdomain()
        assertNull(converted)
    }

    @Test
    fun whenRemovingASubdomainWhenOnlyOneExistsButHasMultipartTldCoUkThenReturnsNull() {
        val converted = Uri.parse("https://co.uk").removeSubdomain()
        assertNull(converted)
    }

    @Test
    fun whenRemovingASubdomainWhenOnlyOneExistsButHasMultipartTldCoNzThenReturnsMultipartTld() {
        val converted = Uri.parse("https://co.za").removeSubdomain()
        assertNull(converted)
    }

    @Test
    fun whenRemovingASubdomainWhenOnlyOneExistsButHasRecentTldThenReturnsNull() {
        val converted = Uri.parse("https://example.dev").removeSubdomain()
        assertNull(converted)
    }

    @Test
    fun whenRemovingASubdomainWhenOnlyOneExistsButHasUnknownTldThenReturnsNull() {
        val converted = Uri.parse("https://example.nonexistent").removeSubdomain()
        assertNull(converted)
    }

    @Test
    fun whenRemovingASubdomainWhenUnknownTldThenReturnsNonExistentTld() {
        val converted = Uri.parse("https://foo.example.nonexistent").removeSubdomain()
        assertEquals("https://example.nonexistent", converted)
    }

    @Test
    fun whenRemovingSubdomainWhenUriIpAddressThenReturnsNull() {
        val converted = Uri.parse("127.0.0.1").removeSubdomain()
        assertNull(converted)
    }
}
