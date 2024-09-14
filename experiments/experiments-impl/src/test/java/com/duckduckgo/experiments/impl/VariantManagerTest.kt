

package com.duckduckgo.experiments.impl

import org.junit.Assert
import org.junit.Test

class VariantManagerTest {

    private val variants = listOf(
        Variant("sc", 0.0) { true },
        Variant("se", 0.0) { true },
        Variant("ma", 1.0) { true },
        Variant("mb", 1.0) { false },
    )

    // SERP Experiment(s)

    @Test
    fun serpControlVariantHasExpectedWeightAndNoFeatures() {
        val variant = variants.first { it.key == "sc" }
        assertEqualsDouble(0.0, variant.weight)
    }

    @Test
    fun serpExperimentalVariantHasExpectedWeightAndNoFeatures() {
        val variant = variants.first { it.key == "se" }
        assertEqualsDouble(0.0, variant.weight)
    }

    @Test
    fun verifyNoDuplicateVariantNames() {
        val existingNames = mutableSetOf<String>()
        variants.forEach {
            if (!existingNames.add(it.key)) {
                Assert.fail("Duplicate variant name found: ${it.key}")
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun assertEqualsDouble(
        expected: Double,
        actual: Double,
    ) {
        val comparison = expected.compareTo(actual)
        if (comparison != 0) {
            Assert.fail("Doubles are not equal. Expected $expected but was $actual")
        }
    }
}
