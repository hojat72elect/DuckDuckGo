

package com.duckduckgo.app.surrogates

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ResourceSurrogatesTest {

    private lateinit var testee: ResourceSurrogates

    @Before
    fun setup() {
        testee = ResourceSurrogatesImpl()
    }

    @Test
    fun whenInitialisedThenHasNoSurrogatesLoaded() {
        assertEquals(0, testee.getAll().size)
    }

    @Test
    fun whenOneSurrogateLoadedThenOneReturnedFromFullList() {
        val surrogate = SurrogateResponse()
        testee.loadSurrogates(listOf(surrogate))
        assertEquals(1, testee.getAll().size)
    }

    @Test
    fun whenMultipleSurrogatesLoadedThenAllReturnedFromFullList() {
        val surrogate = SurrogateResponse()
        testee.loadSurrogates(listOf(surrogate, surrogate, surrogate))
        assertEquals(3, testee.getAll().size)
    }

    @Test
    fun whenSearchingForExactMatchingExistingSurrogateThenCanFindByScriptId() {
        val surrogate = SurrogateResponse(scriptId = "fooId", name = "foo")
        testee.loadSurrogates(listOf(surrogate))
        val retrieved = testee.get("fooId")
        assertTrue(retrieved.responseAvailable)
    }

    @Test
    fun whenSearchingByNonExistentScriptIdThenResponseUnavailableSurrogateResultReturned() {
        val surrogate = SurrogateResponse(scriptId = "fooId", name = "foo")
        testee.loadSurrogates(listOf(surrogate))
        val retrieved = testee.get("bar")
        assertFalse(retrieved.responseAvailable)
    }
}
