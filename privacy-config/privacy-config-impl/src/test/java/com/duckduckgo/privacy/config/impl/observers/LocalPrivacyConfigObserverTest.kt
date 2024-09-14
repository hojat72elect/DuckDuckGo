

package com.duckduckgo.privacy.config.impl.observers

import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.LifecycleOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.common.test.FileUtilities.loadResource
import com.duckduckgo.privacy.config.impl.PrivacyConfigPersister
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class LocalPrivacyConfigObserverTest {

    @get:Rule var coroutineRule = CoroutineTestRule()

    private val mockPrivacyConfigPersister: PrivacyConfigPersister = mock()
    private val mockContext: Context = mock()
    private val lifecycleOwner: LifecycleOwner = mock()
    lateinit var testee: LocalPrivacyConfigObserver

    @Before
    fun before() {
        testee =
            LocalPrivacyConfigObserver(
                mockContext,
                mockPrivacyConfigPersister,
                TestScope(),
                coroutineRule.testDispatcherProvider,
            )
    }

    @Test
    fun whenOnCreateApplicationThenCallPersistPrivacyConfig() =
        runTest {
            givenLocalPrivacyConfigFileExists()

            testee.onCreate(lifecycleOwner)

            verify(mockPrivacyConfigPersister).persistPrivacyConfig(any(), eq(null))
        }

    private fun givenLocalPrivacyConfigFileExists() {
        val resources: Resources = mock()
        whenever(mockContext.resources).thenReturn(resources)
        whenever(resources.openRawResource(any()))
            .thenReturn(loadResource(javaClass.classLoader!!, "json/privacy_config.json"))
    }
}
