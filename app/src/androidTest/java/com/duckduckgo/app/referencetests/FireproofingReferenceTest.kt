

package com.duckduckgo.app.referencetests

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.core.net.toUri
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.app.fire.WebViewDatabaseLocator
import com.duckduckgo.app.fire.fireproofwebsite.data.FireproofWebsiteEntity
import com.duckduckgo.app.fire.fireproofwebsite.data.FireproofWebsiteRepositoryImpl
import com.duckduckgo.app.global.db.AppDatabase
import com.duckduckgo.common.test.FileUtilities
import com.duckduckgo.common.utils.DefaultDispatcherProvider
import com.duckduckgo.common.utils.domain
import com.duckduckgo.cookies.impl.CookieManagerRemover
import com.duckduckgo.cookies.impl.DefaultCookieManagerProvider
import com.duckduckgo.cookies.impl.RemoveCookies
import com.duckduckgo.cookies.impl.SQLCookieRemover
import com.duckduckgo.cookies.impl.WebViewCookieManager
import com.duckduckgo.privacy.config.impl.network.JSONObjectAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.mock

@RunWith(Parameterized::class)
@SuppressLint("NoHardcodedCoroutineDispatcher")
class FireproofingReferenceTest(private val testCase: TestCase) {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    private val cookieManagerProvider = DefaultCookieManagerProvider()
    private val cookieManager = cookieManagerProvider.get()!!
    private val fireproofWebsiteDao = db.fireproofWebsiteDao()
    private val webViewDatabaseLocator = WebViewDatabaseLocator(context)
    private val fireproofWebsiteRepositoryImpl = FireproofWebsiteRepositoryImpl(fireproofWebsiteDao, DefaultDispatcherProvider(), mock())
    private lateinit var testee: WebViewCookieManager

    companion object {
        private lateinit var fireproofedSites: List<String>
        private val moshi = Moshi.Builder().add(JSONObjectAdapter()).build()
        val adapter: JsonAdapter<ReferenceTest> = moshi.adapter(ReferenceTest::class.java)

        @JvmStatic
        @Parameterized.Parameters(name = "Test case: {index} - {0}")
        fun testData(): List<TestCase> {
            val referenceTest = adapter.fromJson(
                FileUtilities.loadText(
                    FireproofingReferenceTest::class.java.classLoader!!,
                    "reference_tests/fireproofing/tests.json",
                ),
            )
            fireproofedSites = referenceTest?.fireButtonFireproofing?.fireproofedSites.orEmpty()
            return referenceTest?.fireButtonFireproofing?.tests?.filterNot { it.exceptPlatforms.contains("android-browser") } ?: emptyList()
        }
    }

    @Before
    fun before() {
        val sqlCookieRemover = SQLCookieRemover(
            webViewDatabaseLocator,
            fireproofWebsiteRepositoryImpl,
            mock(),
            DefaultDispatcherProvider(),
        )

        val removeCookiesStrategy = RemoveCookies(CookieManagerRemover(cookieManagerProvider), sqlCookieRemover)

        testee = WebViewCookieManager(cookieManagerProvider, removeCookiesStrategy, DefaultDispatcherProvider())

        fireproofedSites.map { url ->
            val domain = url.toUri().domain() ?: url
            fireproofWebsiteDao.insert(FireproofWebsiteEntity(domain))
        }
    }

    @After
    fun after() = runTest {
        removeExistingCookies()
        db.close()
    }

    @Test
    fun whenReferenceTestRunsItReturnsTheExpectedResult() = runTest {
        if (Build.VERSION.SDK_INT == 28) {
            // these tests fail on API 28 due to WAL. This effectively skips these tests on 28.
            return@runTest
        }

        withContext(Dispatchers.Main) {
            givenDatabaseWithCookies(testCase.cookieDomain, testCase.cookieName)

            testee.removeExternalCookies()

            openReadableDatabase(webViewDatabaseLocator.getDatabasePath())?.apply {
                use {
                    rawQuery("SELECT * FROM cookies WHERE host_key='${testCase.cookieDomain}' LIMIT 1", null).use { cursor ->
                        cursor.moveToFirst()
                        if (testCase.expectCookieRemoved) {
                            assertTrue(cursor.count == 0)
                        } else {
                            assertTrue(cursor.count == 1)
                        }
                    }
                }
            }
        }
    }

    private fun openReadableDatabase(databasePath: String): SQLiteDatabase? {
        return try {
            SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READWRITE, null)
        } catch (exception: Exception) {
            null
        }
    }

    private fun givenDatabaseWithCookies(domain: String, name: String) {
        cookieManager.setCookie(domain, "$name=test")
        cookieManager.flush()
    }

    private suspend fun removeExistingCookies() {
        withContext(Dispatchers.Main) {
            suspendCoroutine<Unit> { continuation ->
                cookieManager.removeAllCookies { continuation.resume(Unit) }
            }
        }
    }

    data class TestCase(
        val name: String,
        val cookieDomain: String,
        val cookieName: String,
        val expectCookieRemoved: Boolean,
        val exceptPlatforms: List<String>,
    )

    data class FireproofTest(
        val name: String,
        val desc: String,
        val fireproofedSites: List<String>,
        val tests: List<TestCase>,
    )

    data class ReferenceTest(
        val fireButtonFireproofing: FireproofTest,
    )
}
