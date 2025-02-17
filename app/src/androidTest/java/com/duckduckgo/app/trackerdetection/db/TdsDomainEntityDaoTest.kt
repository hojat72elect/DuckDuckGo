

package com.duckduckgo.app.trackerdetection.db

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.app.global.db.AppDatabase
import com.duckduckgo.app.trackerdetection.model.TdsDomainEntity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TdsDomainEntityDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: TdsDomainEntityDao

    @Before
    fun before() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java).build()
        dao = db.tdsDomainEntityDao()
    }

    @After
    fun after() {
        db.close()
    }

    @Test
    fun whenModelIsEmptyThenCountIsZero() {
        assertEquals(0, dao.count())
    }

    @Test
    fun whenEntityInsertedThenCountIsOne() {
        dao.insertAll(listOf(TdsDomainEntity("domain.com", "Owner")))
        assertEquals(1, dao.count())
    }

    @Test
    fun whenEntityInsertedThenContainsEntity() {
        val entity = TdsDomainEntity("domain.com", "Owner")
        dao.insertAll(listOf(entity))
        assertTrue(dao.getAll().contains(entity))
    }

    @Test
    fun whenSecondUniqueEntityInsertedThenCountIsTwo() {
        dao.insertAll(listOf(TdsDomainEntity("domain.com", "Owner")))
        dao.insertAll(listOf(TdsDomainEntity("anotherDomain.com", "Owner")))
        assertEquals(2, dao.count())
    }

    @Test
    fun whenSecondDuplicateEntityInsertedThenCountIsOne() {
        dao.insertAll(listOf(TdsDomainEntity("domain.com", "Owner")))
        dao.insertAll(listOf(TdsDomainEntity("domain.com", "Owner")))
        assertEquals(1, dao.count())
    }

    @Test
    fun whenAllUpdatedThenPreviousValuesAreReplaced() {
        val initialDomain = TdsDomainEntity("domain.com", "Owner")
        val replacementDomain = TdsDomainEntity("domain.com", "New Owner")

        dao.insertAll(listOf(initialDomain))
        dao.updateAll(listOf(replacementDomain))
        assertEquals(1, dao.count())
        assertTrue(dao.getAll().contains(replacementDomain))
    }

    @Test
    fun whenAllDeletedThenCountIsZero() {
        val domain = TdsDomainEntity("domain.com", "Owner")
        dao.insertAll(listOf(domain))
        dao.deleteAll()
        assertEquals(0, dao.count())
    }
}
