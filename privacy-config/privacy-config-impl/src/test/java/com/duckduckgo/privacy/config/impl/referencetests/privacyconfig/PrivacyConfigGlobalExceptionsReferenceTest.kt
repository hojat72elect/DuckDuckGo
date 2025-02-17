

package com.duckduckgo.privacy.config.impl.referencetests.privacyconfig

import androidx.room.Room
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.common.test.FileUtilities
import com.duckduckgo.common.test.api.InMemorySharedPreferences
import com.duckduckgo.feature.toggles.api.FeatureToggle
import com.duckduckgo.privacy.config.impl.RealPrivacyConfigPersister
import com.duckduckgo.privacy.config.impl.ReferenceTestUtilities
import com.duckduckgo.privacy.config.impl.features.contentblocking.RealContentBlocking
import com.duckduckgo.privacy.config.impl.features.unprotectedtemporary.RealUnprotectedTemporary
import com.duckduckgo.privacy.config.impl.network.JSONObjectAdapter
import com.duckduckgo.privacy.config.store.PrivacyConfigDatabase
import com.duckduckgo.privacy.config.store.PrivacyFeatureTogglesRepository
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(ParameterizedRobolectricTestRunner::class)
class PrivacyConfigGlobalExceptionsReferenceTest(private val testCase: TestCase) {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private lateinit var testee: RealContentBlocking
    private lateinit var privacyConfigPersister: RealPrivacyConfigPersister
    private val mockTogglesRepository: PrivacyFeatureTogglesRepository = mock()
    private val mockFeatureToggle: FeatureToggle = mock()

    private lateinit var db: PrivacyConfigDatabase
    private lateinit var referenceTestUtilities: ReferenceTestUtilities

    private val context = RuntimeEnvironment.getApplication()

    companion object {
        private val moshi = Moshi.Builder().add(JSONObjectAdapter()).build()
        val adapter: JsonAdapter<ReferenceTest> = moshi.adapter(ReferenceTest::class.java)
        private lateinit var referenceJsonFile: String

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "Test case: {index} - {0}")
        fun testData(): List<TestCase> {
            val referenceTest = adapter.fromJson(
                FileUtilities.loadText(
                    PrivacyConfigGlobalExceptionsReferenceTest::class.java.classLoader!!,
                    "reference_tests/privacyconfig/tests.json",
                ),
            )
            referenceJsonFile = referenceTest?.globalExceptions?.referenceConfig!!
            return referenceTest.globalExceptions.tests.filterNot { it.exceptPlatforms.contains("android-browser") }
        }
    }

    @Before
    fun before() {
        prepareDb()
        loadPrivacyConfig()
    }

    @After
    fun after() {
        db.close()
    }

    @Test
    fun whenReferenceTestRunsItReturnsTheExpectedResult() = runTest {
        givenFeatureToggleIsEnabled()
        when (testCase.featureName) {
            "contentBlocking" -> {
                testFeatureEnabledForContentBlocking()
            }
        }
    }

    private fun testFeatureEnabledForContentBlocking() {
        val unprotectedTemporary = RealUnprotectedTemporary(referenceTestUtilities.unprotectedTemporaryRepository)
        testee = RealContentBlocking(referenceTestUtilities.contentBlockingRepository, mockFeatureToggle, unprotectedTemporary)
        val isFeatureEnabled = !testee.isAnException(testCase.siteURL)

        assertEquals(testCase.expectFeatureEnabled, isFeatureEnabled)
    }

    private fun prepareDb() {
        db =
            Room.inMemoryDatabaseBuilder(context, PrivacyConfigDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }

    private fun loadPrivacyConfig() = runTest {
        referenceTestUtilities = ReferenceTestUtilities(db, coroutineRule.testDispatcherProvider)
        privacyConfigPersister = RealPrivacyConfigPersister(
            referenceTestUtilities.getPrivacyFeaturePluginPoint(),
            referenceTestUtilities.getVariantManagerPlugin(),
            mockTogglesRepository,
            referenceTestUtilities.unprotectedTemporaryRepository,
            referenceTestUtilities.privacyRepository,
            db,
            InMemorySharedPreferences(),
        )
        privacyConfigPersister.persistPrivacyConfig(
            referenceTestUtilities.getJsonPrivacyConfig(
                "reference_tests/privacyconfig/$referenceJsonFile",
            ),
        )
    }

    private fun givenFeatureToggleIsEnabled() {
        whenever(mockFeatureToggle.isFeatureEnabled(any(), any())).thenReturn(true)
    }

    data class TestCase(
        val name: String,
        val featureName: String,
        val siteURL: String,
        val expectFeatureEnabled: Boolean,
        val exceptPlatforms: List<String>,
    )

    data class GlobalExceptionsTest(
        val name: String,
        val desc: String,
        val referenceConfig: String,
        val tests: List<TestCase>,
    )

    data class ReferenceTest(
        val globalExceptions: GlobalExceptionsTest,
    )
}
