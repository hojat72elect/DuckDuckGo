

package com.duckduckgo.espresso.privacy

import android.webkit.WebView
import androidx.test.core.app.*
import androidx.test.espresso.*
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.web.model.Atoms.script
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.app.browser.BrowserActivity
import com.duckduckgo.app.browser.R
import com.duckduckgo.espresso.*
import com.duckduckgo.privacy.config.impl.network.JSONObjectAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SurrogatesTest {

    @get:Rule
    var activityScenarioRule = activityScenarioRule<BrowserActivity>()

    @Test @PrivacyTest
    fun whenProtectionsAreEnabledSurrogatesAreLoaded() {
        preparationsForPrivacyTest()

        var webView: WebView? = null

        val scenario = ActivityScenario.launch<BrowserActivity>(
            BrowserActivity.intent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                "https://privacy-test-pages.site/privacy-protections/surrogates/",
            ),
        )
        scenario.onActivity {
            webView = it.findViewById(R.id.browserWebView)
        }

        val idlingResourceForDisableProtections = WebViewIdlingResource(webView!!)
        IdlingRegistry.getInstance().register(idlingResourceForDisableProtections)

        val results = onWebView()
            .perform(script(SCRIPT))
            .get()

        val testJson: TestJson? = getTestJson(results.toJSONString())

        testJson?.value?.map {
            if (compatibleIds.contains(it.id)) {
                assertTrue("Loaded for ${it.id} should be loaded and is ${it.loaded}", it.loaded)
            }
        }
    }

    @Test @PrivacyTest
    fun whenProtectionsAreDisabledSurrogatesAreNotLoaded() {
        preparationsForPrivacyTest()

        var webView: WebView? = null

        val scenario = ActivityScenario.launch<BrowserActivity>(
            BrowserActivity.intent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                "https://privacy-test-pages.site/privacy-protections/surrogates/",
            ),
        )
        scenario.onActivity {
            webView = it.findViewById(R.id.browserWebView)
        }

        val idlingResourceForDisableProtections = WebViewIdlingResource(webView!!)
        IdlingRegistry.getInstance().register(idlingResourceForDisableProtections)

        onView(withId(R.id.browserMenu)).perform(ViewActions.click())
        onView(isRoot()).perform(waitForView(withId(R.id.privacyProtectionMenuItem)))
        onView(withId(R.id.privacyProtectionMenuItem)).perform(ViewActions.click())

        val idlingResourceForScript: IdlingResource = WebViewIdlingResource(webView!!)
        IdlingRegistry.getInstance().register(idlingResourceForScript)

        val results = onWebView()
            .perform(script(SCRIPT))
            .get()

        val testJson: TestJson? = getTestJson(results.toJSONString())

        testJson?.value?.map {
            if (compatibleIds.contains(it.id)) {
                assertFalse("Loaded for ${it.id} should not be loaded and is ${it.loaded}", it.loaded)
            }
        }

        IdlingRegistry.getInstance().unregister(idlingResourceForDisableProtections, idlingResourceForScript)
    }

    private fun getTestJson(jsonString: String): TestJson? {
        val moshi = Moshi.Builder().add(JSONObjectAdapter()).build()
        val jsonAdapter: JsonAdapter<TestJson> = moshi.adapter(TestJson::class.java)
        return jsonAdapter.fromJson(jsonString)
    }

    companion object {
        const val SCRIPT = "return results.results;"
        val compatibleIds = listOf("main-frame", "sub-frame")
    }

    data class TestJson(val status: Int, val value: List<SurrogatesTest>)
    data class SurrogatesTest(val id: String, val loaded: Boolean)
}
