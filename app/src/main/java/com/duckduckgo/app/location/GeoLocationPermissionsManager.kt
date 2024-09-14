

package com.duckduckgo.app.location

import android.content.Context
import android.location.LocationManager
import android.webkit.GeolocationPermissions
import androidx.core.location.LocationManagerCompat
import com.duckduckgo.app.fire.fireproofwebsite.data.FireproofWebsiteRepository
import com.duckduckgo.app.location.data.LocationPermissionEntity
import com.duckduckgo.app.location.data.LocationPermissionsRepository
import com.duckduckgo.common.utils.DispatcherProvider
import javax.inject.Inject
import kotlinx.coroutines.withContext

interface GeoLocationPermissions {
    fun isDeviceLocationEnabled(): Boolean
    fun allow(domain: String)
    fun clear(domain: String)
    suspend fun undoClearAll(locationPermissions: List<LocationPermissionEntity>)
    suspend fun clearAll()
    suspend fun clearAllButFireproofed()
}

class GeoLocationPermissionsManager @Inject constructor(
    private val context: Context,
    private val permissionsRepository: LocationPermissionsRepository,
    private val fireproofWebsiteRepository: FireproofWebsiteRepository,
    private val dispatchers: DispatcherProvider,
) : GeoLocationPermissions {

    override fun isDeviceLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    override fun allow(domain: String) {
        val geolocationPermissions = GeolocationPermissions.getInstance()
        geolocationPermissions.allow(domain)
    }

    override fun clear(domain: String) {
        val geolocationPermissions = GeolocationPermissions.getInstance()
        geolocationPermissions.clear(domain)
    }

    override suspend fun undoClearAll(locationPermissions: List<LocationPermissionEntity>) {
        locationPermissions.forEach { entity ->
            permissionsRepository.savePermissionEntity(entity)
        }
    }

    override suspend fun clearAll() {
        withContext(dispatchers.io()) {
            val geolocationPermissions = GeolocationPermissions.getInstance()
            val permissions = permissionsRepository.getLocationPermissionsSync()
            permissions.forEach {
                geolocationPermissions.clear(it.domain)
                permissionsRepository.deletePermission(it.domain)
            }
        }
    }

    override suspend fun clearAllButFireproofed() {
        val geolocationPermissions = GeolocationPermissions.getInstance()
        val permissions = permissionsRepository.getLocationPermissionsSync()
        permissions.forEach {
            if (!fireproofWebsiteRepository.isDomainFireproofed(it.domain)) {
                geolocationPermissions.clear(it.domain)
                permissionsRepository.deletePermission(it.domain)
            }
        }
    }
}
