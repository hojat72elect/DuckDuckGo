package com.duckduckgo.app.userstate

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.duckduckgo.app.tabs.model.TabDataRepository
import com.duckduckgo.common.test.CoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class UserStateReporterTest {
    @get:Rule
    @Suppress("unused")
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private val dispatcherProvider = coroutinesTestRule.testDispatcherProvider
    private val repository = mock<TabDataRepository>()
    private val context = mock<Context>()
    private val packageManager = mock<PackageManager>()

    @Before
    fun setUp() {
        whenever(context.packageManager).thenReturn(packageManager)
    }

    @Test
    fun verifyUserIsNewWhenFirstInstallTimeEqualsLastInstallTime() = runTest {
        val packageInfo = PackageInfo().apply {
            firstInstallTime = 1000L
            lastUpdateTime = 1000L
        }
        initializeSut(packageInfo)

        verify(repository).setIsUserNew(true)
    }

    @Test
    fun verifyUserExistingWhenFirstInstallTimeDoesNotEqualLastInstallTime() = runTest {
        val packageInfo = PackageInfo().apply {
            firstInstallTime = 1000L
            lastUpdateTime = 2000L
        }
        initializeSut(packageInfo)

        verify(repository).setIsUserNew(false)
    }

    private fun initializeSut(packageInfo: PackageInfo) {
        whenever(packageManager.getPackageInfo(context.packageName, 0)).thenReturn(packageInfo)

        val userStateReporter =
            UserStateReporter(dispatcherProvider, repository, context, TestScope())

        userStateReporter.onCreate(mock())
    }
}
