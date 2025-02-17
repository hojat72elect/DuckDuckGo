

package com.duckduckgo.app.usage.search

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.app.global.db.AppDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SearchCountDaoTest {

    @get:Rule
    @Suppress("unused")
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var dao: SearchCountDao

    @Before
    fun before() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.searchCountDao()
    }

    @After
    fun after() {
        db.close()
    }

    @Test
    fun whenDbInitialisedThenSearchCountDefaultsTo0() {
        assertEquals(0, dao.getSearchesMade())
    }

    @Test
    fun whenSearchCountIncrementedFrom0ThenNewSearchCountIs1() {
        dao.incrementSearchCount()
        assertEquals(1, dao.getSearchesMade())
    }

    @Test
    fun whenSearchCountIncrementedFrom1ThenNewSearchCountIs2() {
        dao.incrementSearchCount()
        dao.incrementSearchCount()
        assertEquals(2, dao.getSearchesMade())
    }

    @Test
    fun whenInitialisedToArbitraryValueThenThatIsReturnedInSearchCount() {
        dao.initialiseValue(SearchCountEntity(count = 5))
        assertEquals(5, dao.getSearchesMade())
    }
}
