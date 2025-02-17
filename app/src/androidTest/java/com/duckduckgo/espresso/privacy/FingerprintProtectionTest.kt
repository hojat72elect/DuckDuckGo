

package com.duckduckgo.espresso.privacy

import android.webkit.WebView
import androidx.test.core.app.*
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.model.Atoms.script
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.Locator.ID
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.app.browser.BrowserActivity
import com.duckduckgo.app.browser.R
import com.duckduckgo.espresso.*
import com.duckduckgo.privacy.config.impl.network.JSONObjectAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.hamcrest.CoreMatchers.containsString
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FingerprintProtectionTest {

    @get:Rule
    var activityScenarioRule = activityScenarioRule<BrowserActivity>()

    @Test @PrivacyTest
    fun whenProtectionsAreFingerprintProtected() {
        preparationsForPrivacyTest()

        var webView: WebView? = null
        val scenario = ActivityScenario.launch<BrowserActivity>(
            BrowserActivity.intent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                "https://privacy-test-pages.site/privacy-protections/fingerprinting/?disable_tests=navigator.requestMediaKeySystemAccess",
            ),
        )
        scenario.onActivity {
            webView = it.findViewById(R.id.browserWebView)
        }

        val idlingResourceForDisableProtections = WebViewIdlingResource(webView!!)
        IdlingRegistry.getInstance().register(idlingResourceForDisableProtections)

        onWebView()
            .withElement(findElement(ID, "start"))
            .check(webMatches(getText(), containsString("Start the test")))
            .perform(webClick())

        val idlingResourceForScript = WebViewIdlingResource(webView!!)
        IdlingRegistry.getInstance().register(idlingResourceForScript)

        val results = onWebView()
            .perform(script(SCRIPT))
            .get()

        val testJson: TestJson? = getTestJson(results.toJSONString())
        testJson?.value?.map {
            if (compatibleIds.contains(it.id)) {
                val expected = compatibleIds[it.id]!!
                val actual = it.value.toString()
                assertEquals(sortProperties(expected), sortProperties(actual))
            }
        }
        IdlingRegistry.getInstance().unregister(idlingResourceForDisableProtections, idlingResourceForScript)
    }

    private fun getTestJson(jsonString: String): TestJson? {
        val moshi = Moshi.Builder().add(JSONObjectAdapter()).build()
        val jsonAdapter: JsonAdapter<TestJson> = moshi.adapter(TestJson::class.java)
        return jsonAdapter.fromJson(jsonString)
    }

    private fun sortProperties(value: String): String {
        return if (value.startsWith("{") && value.endsWith("}")) {
            value.trim('{', '}')
                .split(", ")
                .sorted()
                .joinToString(prefix = "{", postfix = "}", separator = ", ")
        } else {
            value
        }
    }

    companion object {
        const val SCRIPT = "return results.results;"
        val compatibleIds = mapOf(
            Pair("navigator.deviceMemory", "4.0"),
            Pair("navigator.hardwareConcurrency", "8.0"),
            Pair("navigator.getBattery()", "{level=1.0, chargingTime=0.0, charging=true}"),
            Pair("navigator.webkitTemporaryStorage.queryUsageAndQuota", "{quota=4.294967296E9, usage=0.0}"),
            Pair("screen.colorDepth", "24.0"),
            Pair("screen.pixelDepth", "24.0"),
            Pair("screen.availLeft", "0.0"),
            Pair("screen.availTop", "0.0"),
        )
    }

    data class TestJson(val status: Int, val value: List<FingerProtectionTest>)
    data class FingerProtectionTest(val id: String, val value: Any)
}
