

package com.duckduckgo.app.global

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class ListExtensionKtTest(private val testCase: TestListTestCase) {

    @Test
    fun testFilterBlankItems() {
        with(testCase) {
            assertEquals(expected, inputList.filterBlankItems())
        }
    }

    companion object {

        @JvmStatic
        @Parameters(name = "Test case: {index} - {0}")
        fun data(): Array<TestListTestCase> = arrayOf(
            TestListTestCase(listOf(""), emptyList()),
            TestListTestCase(listOf(" "), emptyList()),
            TestListTestCase(listOf("foo.bar"), listOf("foo.bar")),
            TestListTestCase(listOf("foo.bar", "ddg.com"), listOf("foo.bar", "ddg.com")),
            TestListTestCase(listOf("foo.bar", ""), listOf("foo.bar")),
            TestListTestCase(listOf("foo.bar", " "), listOf("foo.bar")),
            TestListTestCase(listOf("", "foo.bar"), listOf("foo.bar")),
            TestListTestCase(listOf(" ", "foo.bar"), listOf("foo.bar")),
            TestListTestCase(listOf("foo.bar", "ddg.com"), listOf("foo.bar", "ddg.com")),
            TestListTestCase(listOf("foo.bar", "", "ddg.com"), listOf("foo.bar", "ddg.com")),
            TestListTestCase(listOf("foo.bar", " ", "ddg.com"), listOf("foo.bar", "ddg.com")),
            TestListTestCase(listOf("foo.bar", "ddg.com", " "), listOf("foo.bar", "ddg.com")),
            TestListTestCase(listOf("foo.bar", "ddg.com", ""), listOf("foo.bar", "ddg.com")),
            TestListTestCase(listOf("foo.bar", "ddg.com"), listOf("foo.bar", "ddg.com")),
            TestListTestCase(listOf("", "foo.bar", "ddg.com"), listOf("foo.bar", "ddg.com")),
            TestListTestCase(listOf(" ", "foo.bar", "ddg.com"), listOf("foo.bar", "ddg.com")),
        )
    }

    data class TestListTestCase(
        val inputList: List<String>,
        val expected: List<String>,
    )
}
