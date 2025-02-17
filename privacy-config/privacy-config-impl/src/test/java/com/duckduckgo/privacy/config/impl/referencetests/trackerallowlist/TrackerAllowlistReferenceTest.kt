

package com.duckduckgo.privacy.config.impl.referencetests.trackerallowlist

import com.duckduckgo.common.test.FileUtilities
import com.duckduckgo.feature.toggles.api.FeatureToggle
import com.duckduckgo.privacy.config.api.PrivacyFeatureName
import com.duckduckgo.privacy.config.impl.features.trackerallowlist.RealTrackerAllowlist
import com.duckduckgo.privacy.config.store.TrackerAllowlistEntity
import com.duckduckgo.privacy.config.store.features.trackerallowlist.TrackerAllowlistRepository
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.ParameterizedType
import java.util.concurrent.CopyOnWriteArrayList
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class TrackerAllowlistReferenceTest(private val testCase: TestCase) {

    private val mockTrackerAllowlistRepository: TrackerAllowlistRepository = mock()
    private val mockFeatureToggle: FeatureToggle = mock()

    companion object {
        private val moshi = Moshi.Builder().build()
        val type: ParameterizedType =
            Types.newParameterizedType(List::class.java, TestCase::class.java)
        val adapter: JsonAdapter<List<TestCase>> = moshi.adapter(type)

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "Test case: {index} - {0}")
        fun testData(): List<TestCase> {
            return adapter.fromJson(
                FileUtilities.loadText(
                    TrackerAllowlistReferenceTest::class.java.classLoader!!,
                    "reference_tests/trackerallowlist/tracker_allowlist_matching_tests.json",
                ),
            )
                ?: emptyList()
        }
    }

    @Test
    fun whenIsAnExceptionAnFeatureEnableThenReturnCorrectValues() {
        whenever(
            mockFeatureToggle.isFeatureEnabled(
                PrivacyFeatureName.TrackerAllowlistFeatureName.value,
                true,
            ),
        )
            .thenReturn(true)
        mockAllowlist()

        val testee = RealTrackerAllowlist(mockTrackerAllowlistRepository, mockFeatureToggle)

        assertEquals(testCase.isAllowlisted, testee.isAnException(testCase.site, testCase.request))
    }

    @Test
    fun whenIsAnExceptionAnFeatureDisabledThenReturnCorrectValues() {
        whenever(
            mockFeatureToggle.isFeatureEnabled(
                PrivacyFeatureName.TrackerAllowlistFeatureName.value,
                true,
            ),
        )
            .thenReturn(false)
        mockAllowlist()

        val testee = RealTrackerAllowlist(mockTrackerAllowlistRepository, mockFeatureToggle)

        assertEquals(false, testee.isAnException(testCase.site, testCase.request))
    }

    private fun mockAllowlist() {
        val jsonAdapter: JsonAdapter<TrackerAllowlistEntity> =
            moshi.adapter(TrackerAllowlistEntity::class.java)
        val exceptions = CopyOnWriteArrayList<TrackerAllowlistEntity>()
        val jsonObject: JSONObject =
            FileUtilities.getJsonObjectFromFile(
                javaClass.classLoader!!,
                "reference_tests/trackerallowlist/tracker_allowlist_reference.json",
            )

        jsonObject.keys().forEach {
            val allowlistEntity = jsonAdapter.fromJson(jsonObject.get(it).toString())
            exceptions.add(allowlistEntity!!.copy(domain = it))
        }
        whenever(mockTrackerAllowlistRepository.exceptions).thenReturn(exceptions)
    }

    data class TestCase(
        val description: String,
        val site: String,
        val request: String,
        val isAllowlisted: Boolean,
    )
}
