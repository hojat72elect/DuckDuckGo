

package com.duckduckgo.app.location.data

import com.duckduckgo.common.utils.extensions.asLocationPermissionOrigin
import org.junit.Assert
import org.junit.Test

class LocationPermissionEntityTest {

    @Test
    fun whenDomainStartsWithHttpsThenDropPrefix() {
        val locationPermissionEntity = LocationPermissionEntity("https://www.example.com/", LocationPermissionType.ALLOW_ONCE)
        val host = locationPermissionEntity.forFireproofing()
        Assert.assertEquals("www.example.com", host)
    }

    @Test
    fun whenDomainStartsWithHttpsUppercaseThenDropPrefix() {
        val locationPermissionEntity = LocationPermissionEntity("HTTPS://www.example.com/", LocationPermissionType.ALLOW_ONCE)
        val host = locationPermissionEntity.forFireproofing()
        Assert.assertEquals("www.example.com", host)
    }

    @Test
    fun whenDomainDoesNotStartWithHttpsThenDomainUnchanged() {
        val locationPermissionEntity = LocationPermissionEntity("mobile.example.com/", LocationPermissionType.ALLOW_ONCE)
        val host = locationPermissionEntity.forFireproofing()
        Assert.assertEquals("mobile.example.com/", host)
    }

    @Test
    fun whenDomainIsReturnedAsPermissionOriginThenDomainMatches() {
        val domain = "www.example.com"
        val host = domain.asLocationPermissionOrigin()
        Assert.assertEquals("https://www.example.com/", host)
    }
}
