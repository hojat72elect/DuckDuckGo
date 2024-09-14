

package com.duckduckgo.httpsupgrade.store

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.common.test.CoroutineTestRule
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HttpsFalsePositivesDaoTest {

    @get:Rule
    @Suppress("unused")
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private lateinit var db: HttpsUpgradeDatabase
    private lateinit var dao: HttpsFalsePositivesDao

    @Before
    fun before() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, HttpsUpgradeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.httpsFalsePositivesDao()
    }

    @After
    fun after() {
        db.close()
    }

    @Test
    fun whenModelIsEmptyThenCountIsZero() = runTest {
        assertEquals(0, dao.count())
    }

    @Test
    fun whenModelIsEmptyThenContainsDomainIsFalse() = runTest {
        assertFalse(dao.contains(domain))
    }

    @Test
    fun whenDomainInsertedThenContainsDomainIsTrue() = runTest {
        dao.insertAll(listOf(HttpsFalsePositiveDomain(domain)))
        assertTrue(dao.contains(domain))
    }

    @Test
    fun whenDomainInsertedThenCountIsOne() = runTest {
        dao.insertAll(listOf(HttpsFalsePositiveDomain(domain)))
        assertEquals(1, dao.count())
    }

    @Test
    fun whenSecondUniqueDomainInsertedThenCountIsTwo() = runTest {
        dao.insertAll(listOf(HttpsFalsePositiveDomain(domain)))
        dao.insertAll(listOf(HttpsFalsePositiveDomain(anotherDomain)))
        assertEquals(2, dao.count())
    }

    @Test
    fun whenSecondDuplicateDomainInsertedThenCountIsOne() = runTest {
        dao.insertAll(listOf(HttpsFalsePositiveDomain(domain)))
        dao.insertAll(listOf(HttpsFalsePositiveDomain(domain)))
        assertEquals(1, dao.count())
    }

    @Test
    fun whenAllUpdatedThenPreviousValuesAreReplaced() = runTest {
        dao.insertAll(listOf(HttpsFalsePositiveDomain(domain)))
        dao.updateAll(listOf(HttpsFalsePositiveDomain(anotherDomain)))
        assertEquals(1, dao.count())
        assertTrue(dao.contains(anotherDomain))
    }

    @Test
    fun whenAllDeletedThenContainsDomainIsFalse() = runTest {
        dao.insertAll(listOf(HttpsFalsePositiveDomain(domain)))
        dao.deleteAll()
        assertFalse(dao.contains(domain))
    }

    companion object {
        var domain = "domain.com"
        var anotherDomain = "another.com"
    }
}
