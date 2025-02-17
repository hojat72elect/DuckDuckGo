

package com.duckduckgo.app.location.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.app.blockingObserve
import com.duckduckgo.app.browser.favicon.FaviconManager
import com.duckduckgo.app.global.db.AppDatabase
import com.duckduckgo.common.test.CoroutineTestRule
import dagger.Lazy
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class LocationPermissionsRepositoryImplTest {

    @get:Rule
    @Suppress("unused")
    val coroutineRule = CoroutineTestRule()

    @get:Rule
    @Suppress("unused")
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var dao: LocationPermissionsDao
    private lateinit var repository: LocationPermissionsRepositoryImpl
    private val mockFaviconManager: FaviconManager = mock()
    private val lazyFaviconManager = Lazy { mockFaviconManager }

    private val domain = "domain.com"

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.locationPermissionsDao()
        repository = LocationPermissionsRepositoryImpl(db.locationPermissionsDao(), lazyFaviconManager, coroutineRule.testDispatcherProvider)
    }

    @After
    fun after() {
        db.close()
    }

    @Test
    fun whenPermissionIsValidThenItIsStored() = runTest {
        val permissionType = LocationPermissionType.ALLOW_ALWAYS
        val permission = LocationPermissionEntity(domain, permissionType)
        val permissionStored = repository.savePermission(domain, LocationPermissionType.ALLOW_ALWAYS)
        assertEquals(permission, permissionStored)
    }

    @Test
    fun whenGetAllPermissionsAsyncThenReturnLiveDataWithAllItemsFromDatabase() = runTest {
        givenPermissionStored("example.com", "example2.com")
        val entities = repository.getLocationPermissionsAsync().blockingObserve()!!
        assertEquals(entities.size, 2)
    }

    @Test
    fun whenGetAllPermissionsSyncThenReturnListWithAllItemsFromDatabase() = runTest {
        givenPermissionStored("example.com", "example2.com")
        val entities = repository.getLocationPermissionsSync()
        assertEquals(entities.size, 2)
    }

    @Test
    fun whenGetAllFireproofWebsitesThenReturnLiveDataWithAllItemsFromDatabase() = runTest {
        givenPermissionStored("example.com", "example2.com")
        val entities = repository.getLocationPermissionsAsync().blockingObserve()!!
        assertEquals(entities.size, 2)
    }

    @Test
    fun whenDeletePermissionStoredThenItemRemovedFromDatabase() = runTest {
        givenPermissionStored("example.com", "example2.com")

        assertEquals(dao.allPermissions().size, 2)

        repository.deletePermission("example.com")

        assertEquals(dao.allPermissions().size, 1)
    }

    @Test
    fun whenDeletePermissionNotStoredThenNotingIsRemovedFromDatabase() = runTest {
        givenPermissionStored("example.com", "example2.com")

        assertEquals(dao.allPermissions().size, 2)

        repository.deletePermission("example3.com")

        assertEquals(dao.allPermissions().size, 2)
    }

    @Test
    fun whenRetrievingStoredPermissionThenItCanBeRetrieved() = runTest {
        givenPermissionStored("example.com", "example2.com")

        val retrieved = repository.getDomainPermission("example2.com")!!

        assertEquals(retrieved.domain, "example2.com")
    }

    @Test
    fun whenRetrievingNotStoredPermissionThenNothingCanBeRetrieved() = runTest {
        givenPermissionStored("example.com", "example2.com")

        val retrieved = repository.getDomainPermission("exampl3.com")

        assertNull(retrieved)
    }

    @Test
    fun whenDeletePermissionStoredThenDeletePersistedFavicon() = runTest {
        givenPermissionStored("example.com")

        repository.deletePermission("example.com")

        verify(mockFaviconManager).deletePersistedFavicon("example.com")
    }

    @Test
    fun whenPermissionEntitiesCountByDomainAndNoWebsitesMatchThenReturnZero() = runTest {
        givenPermissionStored("example.com")

        val count = repository.permissionEntitiesCountByDomain("test.com")

        assertEquals(0, count)
    }

    @Test
    fun whenPermissionEntitiesCountByDomainAndWebsitesMatchThenReturnCount() = runTest {
        val query = "%example%"
        givenPermissionStored("example.com")

        val count = repository.permissionEntitiesCountByDomain(query)

        assertEquals(1, count)
    }

    private fun givenPermissionStored(vararg domains: String) {
        domains.forEach {
            dao.insert(LocationPermissionEntity(domain = it, permission = LocationPermissionType.ALLOW_ALWAYS))
        }
    }
}
