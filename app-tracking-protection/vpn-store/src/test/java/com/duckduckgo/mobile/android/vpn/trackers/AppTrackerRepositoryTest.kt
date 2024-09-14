

package com.duckduckgo.mobile.android.vpn.trackers

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.mobile.android.vpn.store.VpnDatabase
import com.duckduckgo.mobile.android.vpn.store.VpnDatabaseCallback
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppTrackerRepositoryTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private lateinit var appTrackerRepository: RealAppTrackerRepository
    private lateinit var vpnDatabase: VpnDatabase

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

        vpnDatabase = Room.inMemoryDatabaseBuilder(
            context,
            VpnDatabase::class.java,
        ).allowMainThreadQueries().build().apply {
            VpnDatabaseCallback(context, { this }, coroutineRule.testDispatcherProvider).prepopulateAppTrackerBlockingList()
        }
        appTrackerRepository = RealAppTrackerRepository(vpnDatabase.vpnAppTrackerBlockingDao(), vpnDatabase.vpnSystemAppsOverridesDao())
    }

    @Test
    fun whenHostnameIsTrackerThenReturnTracker() {
        assertTrackerTypeFound(appTrackerRepository.findTracker("g.doubleclick.net", ""))
    }

    @Test
    fun whenSubdomainIsTrackerThenReturnTracker() {
        assertTrackerTypeFound(appTrackerRepository.findTracker("foo.g.doubleclick.net", ""))
    }

    @Test
    fun whenHostnameIsNotTrackerThenReturnNull() {
        assertNotTrackerType(appTrackerRepository.findTracker("not.tracker.net", ""))
    }

    private fun assertTrackerTypeFound(tracker: AppTrackerType) {
        assertFalse(tracker is AppTrackerType.NotTracker)
    }

    private fun assertNotTrackerType(tracker: AppTrackerType) {
        assertTrue(tracker is AppTrackerType.NotTracker)
    }
}
