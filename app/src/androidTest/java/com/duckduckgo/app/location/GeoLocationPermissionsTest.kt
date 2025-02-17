

package com.duckduckgo.app.location

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.app.browser.favicon.FaviconManager
import com.duckduckgo.app.fire.fireproofwebsite.data.FireproofWebsiteDao
import com.duckduckgo.app.fire.fireproofwebsite.data.FireproofWebsiteEntity
import com.duckduckgo.app.fire.fireproofwebsite.data.FireproofWebsiteRepositoryImpl
import com.duckduckgo.app.global.db.AppDatabase
import com.duckduckgo.app.location.data.LocationPermissionEntity
import com.duckduckgo.app.location.data.LocationPermissionType
import com.duckduckgo.app.location.data.LocationPermissionsDao
import com.duckduckgo.app.location.data.LocationPermissionsRepositoryImpl
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.common.test.InstantSchedulersRule
import dagger.Lazy
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

class GeoLocationPermissionsTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val schedulers = InstantSchedulersRule()

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private lateinit var locationPermissionsDao: LocationPermissionsDao

    private lateinit var fireproofWebsiteDao: FireproofWebsiteDao

    private lateinit var db: AppDatabase

    private lateinit var geoLocationPermissions: GeoLocationPermissions

    private val mockFaviconManager: FaviconManager = mock()
    private val lazyFaviconManager = Lazy { mockFaviconManager }

    @Before
    fun before() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        locationPermissionsDao = db.locationPermissionsDao()
        fireproofWebsiteDao = db.fireproofWebsiteDao()

        geoLocationPermissions = GeoLocationPermissionsManager(
            InstrumentationRegistry.getInstrumentation().targetContext,
            LocationPermissionsRepositoryImpl(locationPermissionsDao, lazyFaviconManager, coroutineRule.testDispatcherProvider),
            FireproofWebsiteRepositoryImpl(fireproofWebsiteDao, coroutineRule.testDispatcherProvider, lazyFaviconManager),
            coroutineRule.testDispatcherProvider,
        )
    }

    @After
    fun after() {
        db.close()
    }

    @Test
    fun whenClearingAllPermissionsButFireproofedThenOnlyNonFireproofedSitesAreDeleted() = runTest {
        givenFireproofWebsiteDomain("anotherdomain.com")
        givenLocationPermissionsDomain("https://domain.com")

        val oldFireproofWebsites = fireproofWebsiteDao.fireproofWebsitesSync()
        assertEquals(oldFireproofWebsites.size, 1)

        val oldLocationPermissions = locationPermissionsDao.allPermissions()
        assertEquals(oldLocationPermissions.size, 1)

        geoLocationPermissions.clearAllButFireproofed()

        val newFireproofWebsites = fireproofWebsiteDao.fireproofWebsitesSync()
        assertEquals(newFireproofWebsites.size, 1)

        val newLocationPermissions = locationPermissionsDao.allPermissions()
        assertEquals(newLocationPermissions.size, 0)
    }

    @Test
    fun whenClearingAllPermissionsButFireproofedAndNoFireproofedSitesThenAllSitePermissionsAreDeleted() = runTest {
        givenLocationPermissionsDomain("https://domain.com")

        val oldLocationPermissions = locationPermissionsDao.allPermissions()
        assertEquals(oldLocationPermissions.size, 1)

        geoLocationPermissions.clearAllButFireproofed()

        val newLocationPermissions = locationPermissionsDao.allPermissions()
        assertEquals(newLocationPermissions.size, 0)
    }

    @Test
    fun whenClearingAllPermissionsButFireproofedAndSiteIsFireproofedThenNothingIsDeleted() = runTest {
        givenFireproofWebsiteDomain("domain.com")
        givenLocationPermissionsDomain("https://domain.com")

        val oldFireproofWebsites = fireproofWebsiteDao.fireproofWebsitesSync()
        assertEquals(oldFireproofWebsites.size, 1)

        val oldLocationPermissions = locationPermissionsDao.allPermissions()
        assertEquals(oldLocationPermissions.size, 1)

        geoLocationPermissions.clearAllButFireproofed()

        val newFireproofWebsites = fireproofWebsiteDao.fireproofWebsitesSync()
        assertEquals(newFireproofWebsites.size, 1)

        val newLocationPermissions = locationPermissionsDao.allPermissions()
        assertEquals(newLocationPermissions.size, 1)
    }

    @Test
    fun whenClearingAllPermissionsThenAllPermissionsAreDeleted() = runTest {
        givenLocationPermissionsDomain("https://domain.com")

        val oldLocationPermissions = locationPermissionsDao.allPermissions()
        assertEquals(oldLocationPermissions.size, 1)

        geoLocationPermissions.clearAll()

        val newLocationPermissions = locationPermissionsDao.allPermissions()
        assertEquals(newLocationPermissions.size, 0)
    }

    private fun givenFireproofWebsiteDomain(vararg fireproofWebsitesDomain: String) {
        fireproofWebsitesDomain.forEach {
            fireproofWebsiteDao.insert(FireproofWebsiteEntity(domain = it))
        }
    }

    private fun givenLocationPermissionsDomain(
        domain: String,
        permissionType: LocationPermissionType = LocationPermissionType.ALLOW_ALWAYS,
    ) {
        locationPermissionsDao.insert(LocationPermissionEntity(domain = domain, permission = permissionType))
    }
}
