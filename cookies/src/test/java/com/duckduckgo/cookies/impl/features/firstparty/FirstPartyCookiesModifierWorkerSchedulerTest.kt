package com.duckduckgo.cookies.impl.features.firstparty

import androidx.lifecycle.LifecycleOwner
import androidx.work.ExistingPeriodicWorkPolicy.KEEP
import androidx.work.ExistingPeriodicWorkPolicy.REPLACE
import androidx.work.WorkManager
import com.duckduckgo.cookies.api.CookiesFeatureName
import com.duckduckgo.feature.toggles.api.FeatureToggle
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FirstPartyCookiesModifierWorkerSchedulerTest {

    private val mockToggle: FeatureToggle = mock()
    private val mockWorkManager: WorkManager = mock()
    private val mockOwner: LifecycleOwner = mock()

    lateinit var firstPartyCookiesModifierWorkerScheduler: FirstPartyCookiesModifierWorkerScheduler

    @Before
    fun before() {
        firstPartyCookiesModifierWorkerScheduler =
            FirstPartyCookiesModifierWorkerScheduler(mockWorkManager, mockToggle)
    }

    @Test
    fun whenOnStopIfFeatureEnabledThenEnqueueWorkWithReplacePolicy() {
        whenever(mockToggle.isFeatureEnabled(CookiesFeatureName.Cookie.value)).thenReturn(true)

        firstPartyCookiesModifierWorkerScheduler.onStop(mockOwner)

        verify(mockWorkManager).enqueueUniquePeriodicWork(any(), eq(REPLACE), any())
    }

    @Test
    fun whenOnStopIfFeatureNotEnabledThenDeleteTag() {
        whenever(mockToggle.isFeatureEnabled(CookiesFeatureName.Cookie.value)).thenReturn(false)

        firstPartyCookiesModifierWorkerScheduler.onStop(mockOwner)

        verify(mockWorkManager).cancelAllWorkByTag(any())
    }

    @Test
    fun whenOnStartIfFeatureEnabledThenEnqueueWorkWithKeepPolicy() {
        whenever(mockToggle.isFeatureEnabled(CookiesFeatureName.Cookie.value)).thenReturn(true)

        firstPartyCookiesModifierWorkerScheduler.onStart(mockOwner)

        verify(mockWorkManager).enqueueUniquePeriodicWork(any(), eq(KEEP), any())
    }

    @Test
    fun whenOnStartIfFeatureNotEnabledThenDeleteTag() {
        whenever(mockToggle.isFeatureEnabled(CookiesFeatureName.Cookie.value)).thenReturn(false)

        firstPartyCookiesModifierWorkerScheduler.onStart(mockOwner)

        verify(mockWorkManager).cancelAllWorkByTag(any())
    }
}
