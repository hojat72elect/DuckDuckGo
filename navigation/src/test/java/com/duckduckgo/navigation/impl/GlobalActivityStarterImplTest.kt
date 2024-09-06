package com.duckduckgo.navigation.impl

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.navigation.api.GlobalActivityStarter
import com.duckduckgo.navigation.api.GlobalActivityStarter.ActivityParams
import com.duckduckgo.navigation.api.GlobalActivityStarter.DeeplinkActivityParams
import com.duckduckgo.navigation.api.getActivityParams
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class GlobalActivityStarterImplTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val globalActivityStarter = GlobalActivityStarterImpl(
        setOf(
            TestActivityWithParamsMapper(),
            TestActivityNoParamsMapper()
        )
    )

    @Test
    fun whenStartIntentNotFoundActivityThenReturnNull() {
        assertNull(globalActivityStarter.startIntent(context, NotFoundParams))
    }

    @Test
    fun whenStartIntentNotFoundDeeplinkActivityThenReturnNull() {
        assertNull(globalActivityStarter.startIntent(context, notFoundDeeplinkParams))
    }

    @Test
    fun whenStartIntentWithParamsFindsActivityThenReturnIntent() {
        val intent = globalActivityStarter.startIntent(context, TestParams("test"))

        assertNotNull(intent)
        assertEquals("test", intent?.getActivityParams(TestParams::class.java)?.value)
    }

    @Test
    fun whenStartIntentWithDeeplinkNoParamsFindsActivityThenReturnIntent() {
        val intent =
            globalActivityStarter.startIntent(context, DeeplinkActivityParams("screenTest"))

        assertNotNull(intent)
        assertNotNull(intent?.getActivityParams(TestNoParams::class.java))
    }

    @Test
    fun whenStartIntentWithDeeplinkParamsFindsActivityThenReturnIntent() {
        val intent = globalActivityStarter.startIntent(
            context,
            DeeplinkActivityParams("screenTest", jsonArguments = "{\"value\": \"test\"}")
        )

        assertNotNull(intent)
        assertEquals("test", intent?.getActivityParams(TestParams::class.java)?.value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun whenStartNotFoundActivityThenThrow() {
        globalActivityStarter.start(context, NotFoundParams)
    }

    @Test(expected = IllegalArgumentException::class)
    fun whenStartNotFoundDeeplinkActivityThenThrow() {
        globalActivityStarter.start(context, notFoundDeeplinkParams)
    }

    @Test
    fun whenStartWithParamsFindsActivityThenSucceeds() {
        val context: Context = mock()
        globalActivityStarter.start(context, TestParams("test"))

        verify(context).startActivity(any(), anyOrNull())
    }

    @Test
    fun whenStartWithDeeplinkParamsArgumentsFindsActivityThenSucceeds() {
        val context: Context = mock()
        globalActivityStarter.start(
            context,
            DeeplinkActivityParams("screenTest", jsonArguments = "{\"value\": \"test\"}")
        )

        verify(context).startActivity(any(), anyOrNull())
    }

    @Test
    fun whenStartWithDeeplinkNoParamsArgumentsFindsActivityThenSucceeds() {
        val context: Context = mock()
        globalActivityStarter.start(context, DeeplinkActivityParams("screenTest"))

        verify(context).startActivity(any(), anyOrNull())
    }
}

private class TestActivity : AppCompatActivity()
private data class TestParams(val value: String) : ActivityParams
private object NotFoundParams : ActivityParams

private val notFoundDeeplinkParams = DeeplinkActivityParams("notFoundScreen")

private object TestNoParams : ActivityParams

private class TestActivityNoParamsMapper : GlobalActivityStarter.ParamToActivityMapper {
    override fun map(activityParams: ActivityParams): Class<out AppCompatActivity>? {
        return if (activityParams is TestNoParams) {
            TestActivity::class.java
        } else {
            null
        }
    }

    override fun map(deeplinkActivityParams: DeeplinkActivityParams): ActivityParams? {
        val screenName =
            deeplinkActivityParams.screenName.takeUnless { it.isEmpty() } ?: return null
        return if (screenName == "screenTest") {
            TestNoParams
        } else {
            null
        }
    }
}

private class TestActivityWithParamsMapper : GlobalActivityStarter.ParamToActivityMapper {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    override fun map(activityParams: ActivityParams): Class<out AppCompatActivity>? {
        return if (activityParams is TestParams) {
            TestActivity::class.java
        } else {
            null
        }
    }

    override fun map(deeplinkActivityParams: DeeplinkActivityParams): ActivityParams? {
        val screenName =
            deeplinkActivityParams.screenName.takeUnless { it.isEmpty() } ?: return null
        return if (screenName == "screenTest") {
            if (deeplinkActivityParams.jsonArguments.isEmpty()) {
                val instance = tryCreateObjectInstance(TestParams::class.java)
                if (instance != null) {
                    return instance
                }
            }
            tryCreateActivityParams(TestParams::class.java, deeplinkActivityParams.jsonArguments)
        } else {
            null
        }
    }

    private fun tryCreateObjectInstance(clazz: Class<out ActivityParams>): ActivityParams? {
        return kotlin.runCatching {
            Types.getRawType(clazz).kotlin.objectInstance as ActivityParams
        }.getOrNull()
    }

    private fun tryCreateActivityParams(
        clazz: Class<out ActivityParams>,
        jsonArguments: String,
    ): ActivityParams? {
        return kotlin.runCatching {
            moshi.adapter(clazz).fromJson(jsonArguments)
        }.getOrNull()
    }
}
