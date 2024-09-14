

package com.duckduckgo.app.fire.fireproofwebsite.data

import org.junit.Assert.*
import org.junit.Test

class FireproofWebsiteEntityKtTest {

    @Test
    fun whenDomainStartsWithWWWThenDropPrefix() {
        val fireproofWebsiteEntity = FireproofWebsiteEntity("www.example.com")
        val website = fireproofWebsiteEntity.website()
        assertEquals("example.com", website)
    }

    @Test
    fun whenDomainStartsWithWWWUppercaseThenDropPrefix() {
        val fireproofWebsiteEntity = FireproofWebsiteEntity("WWW.example.com")
        val website = fireproofWebsiteEntity.website()
        assertEquals("example.com", website)
    }

    @Test
    fun whenDomainDoesNotStartWithWWWThenDomainUnchanged() {
        val fireproofWebsiteEntity = FireproofWebsiteEntity("mobile.example.com")
        val website = fireproofWebsiteEntity.website()
        assertEquals("mobile.example.com", website)
    }
}
