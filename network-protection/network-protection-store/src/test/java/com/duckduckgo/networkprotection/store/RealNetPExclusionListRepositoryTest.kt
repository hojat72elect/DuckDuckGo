

package com.duckduckgo.networkprotection.store

import com.duckduckgo.networkprotection.store.db.NetPExclusionListDao
import com.duckduckgo.networkprotection.store.db.NetPManuallyExcludedApp
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RealNetPExclusionListRepositoryTest {
    @Mock
    private lateinit var exclusionListDao: NetPExclusionListDao
    private lateinit var testee: RealNetPExclusionListRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        whenever(exclusionListDao.getManualAppExclusionList()).thenReturn(MANUAL_EXCLUSION_LIST)
        testee = RealNetPExclusionListRepository(exclusionListDao)
    }

    @Test
    fun whenGetManualAppExclusionListThenDelegateToNetPExclusionListDao() {
        testee.getManualAppExclusionList()

        verify(exclusionListDao).getManualAppExclusionList()
    }

    @Test
    fun whenGetManualAppExclusionListFlowThenDelegateToNetPExclusionListDao() {
        testee.getManualAppExclusionListFlow()

        verify(exclusionListDao).getManualAppExclusionListFlow()
    }

    @Test
    fun whenGetExcludedAppPackagesThenReturnUnprotectedAppsPackages() {
        assertEquals(
            listOf("com.example.app2", "com.example.app3"),
            testee.getExcludedAppPackages(),
        )
    }

    @Test
    fun whenManuallyExcludeAppThenDelegateToNetPExclusionListDao() {
        testee.manuallyExcludeApp("test")

        verify(exclusionListDao).insertIntoManualAppExclusionList(NetPManuallyExcludedApp(packageId = "test", isProtected = false))
    }

    @Test
    fun whenManuallyExcludeAppsThenDelegateToNetPExclusionListDao() {
        testee.manuallyExcludeApps(
            listOf(
                "test1",
                "test2",
                "test3",
            ),
        )

        verify(exclusionListDao).insertIntoManualAppExclusionList(
            listOf(
                NetPManuallyExcludedApp(packageId = "test1", isProtected = false),
                NetPManuallyExcludedApp(packageId = "test2", isProtected = false),
                NetPManuallyExcludedApp(packageId = "test3", isProtected = false),
            ),
        )
    }

    @Test
    fun whenManuallyEnableAppThenDelegateToNetPExclusionListDao() {
        testee.manuallyEnableApp("test")

        verify(exclusionListDao).insertIntoManualAppExclusionList(NetPManuallyExcludedApp(packageId = "test", isProtected = true))
    }

    @Test
    fun whenRestoreDefaultProtectedListThenDelegateToNetPExclusionListDao() {
        testee.restoreDefaultProtectedList()

        verify(exclusionListDao).deleteManualAppExclusionList()
    }

    companion object {
        private val MANUAL_EXCLUSION_LIST = listOf(
            NetPManuallyExcludedApp("com.example.app1", true),
            NetPManuallyExcludedApp("com.example.app2", false),
            NetPManuallyExcludedApp("com.example.app3", false),
        )
    }
}
