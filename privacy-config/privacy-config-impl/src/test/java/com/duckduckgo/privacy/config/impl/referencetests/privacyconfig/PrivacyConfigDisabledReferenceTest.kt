

package com.duckduckgo.privacy.config.impl.referencetests.privacyconfig

import androidx.room.Room
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.common.test.FileUtilities
import com.duckduckgo.common.test.api.InMemorySharedPreferences
import com.duckduckgo.privacy.config.impl.RealPrivacyConfigPersister
import com.duckduckgo.privacy.config.impl.ReferenceTestUtilities
import com.duckduckgo.privacy.config.impl.features.privacyFeatureValueOf
import com.duckduckgo.privacy.config.impl.network.JSONObjectAdapter
import com.duckduckgo.privacy.config.store.PrivacyConfigDatabase
import com.duckduckgo.privacy.config.store.PrivacyFeatureToggles
import com.duckduckgo.privacy.config.store.PrivacyFeatureTogglesRepository
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(ParameterizedRobolectricTestRunner::class)
class PrivacyConfigDisabledReferenceTest(private val testCase: TestCase) {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    lateinit var testee: RealPrivacyConfigPersister
    private val mockTogglesRepository: PrivacyFeatureTogglesRepository = mock()

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
                    PrivacyConfigDisabledReferenceTest::class.java.classLoader!!,
                    "reference_tests/privacyconfig/tests.json",
                ),
            )
            referenceJsonFile = referenceTest?.featuresDisabled?.referenceConfig!!
            return referenceTest.featuresDisabled.tests.filterNot { it.exceptPlatforms.contains("android-browser") }
        }
    }

    @Before
    fun before() {
        prepareDb()
        referenceTestUtilities = ReferenceTestUtilities(db, coroutineRule.testDispatcherProvider)

        testee = RealPrivacyConfigPersister(
            referenceTestUtilities.getPrivacyFeaturePluginPoint(),
            referenceTestUtilities.getVariantManagerPlugin(),
            mockTogglesRepository,
            referenceTestUtilities.unprotectedTemporaryRepository,
            referenceTestUtilities.privacyRepository,
            db,
            InMemorySharedPreferences(),
        )
    }

    @After
    fun after() {
        db.close()
    }

    @Test
    fun whenReferenceTestRunsItReturnsTheExpectedResult() = runTest {
        testee.persistPrivacyConfig(referenceTestUtilities.getJsonPrivacyConfig("reference_tests/privacyconfig/$referenceJsonFile"))

        verify(referenceTestUtilities.privacyFeatureTogglesRepository).insert(
            PrivacyFeatureToggles(
                privacyFeatureValueOf(testCase.featureName)!!.value,
                testCase.expectFeatureEnabled,
                null,
            ),
        )
    }

    private fun prepareDb() {
        db =
            Room.inMemoryDatabaseBuilder(context, PrivacyConfigDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }

    data class TestCase(
        val name: String,
        val featureName: String,
        val siteURL: String,
        val expectFeatureEnabled: Boolean,
        val exceptPlatforms: List<String>,
    )

    data class FeaturesDisabledTest(
        val name: String,
        val desc: String,
        val referenceConfig: String,
        val tests: List<TestCase>,
    )

    data class ReferenceTest(
        val featuresDisabled: FeaturesDisabledTest,
    )
}
