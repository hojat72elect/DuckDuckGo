

package com.duckduckgo.mobile.android.vpn.cohort

import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.IsoFields
import javax.inject.Inject

interface CohortCalculator {
    fun calculateCohortForDate(localDate: LocalDate, now: LocalDate = LocalDate.now()): String
}

@ContributesBinding(AppScope::class)
class RealCohortCalculator @Inject constructor() : CohortCalculator {

    override fun calculateCohortForDate(localDate: LocalDate, now: LocalDate): String {
        val weeksSinceDate = ChronoUnit.WEEKS.between(localDate, now)

        return when {
            weeksSinceDate <= WEEKS_TO_MONTHLY_COHORT -> {
                weeklyCohortName(localDate)
            }
            weeksSinceDate <= WEEKS_TO_QUARTER_COHORT -> {
                monthlyCohortName(localDate)
            }
            weeksSinceDate <= WEEKS_TO_HALF_COHORT -> {
                quarterCohortName(localDate)
            }
            weeksSinceDate <= WEEKS_TO_SINGLE_COHORT -> {
                halfCohortName(localDate)
            }
            else -> {
                singleCohortName()
            }
        }
    }

    private fun weeklyCohortName(localDate: LocalDate): String {
        return "${weekBasedYear(localDate)}-week-${weekOfYear(localDate)}"
    }

    private fun monthlyCohortName(localDate: LocalDate): String {
        return "${localDate.year}-${localDate.month}"
    }

    private fun quarterCohortName(localDate: LocalDate): String {
        return "${localDate.year}-q${localDate.get(IsoFields.QUARTER_OF_YEAR)}"
    }

    private fun halfCohortName(localDate: LocalDate): String {
        val half = if (localDate.monthValue > 6) "2" else "1"
        return "${localDate.year}-h$half"
    }

    private fun singleCohortName(): String {
        return "-"
    }

    private fun weekOfYear(localDate: LocalDate): Int {
        return localDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
    }

    private fun weekBasedYear(localDate: LocalDate): Int {
        return localDate.get(IsoFields.WEEK_BASED_YEAR)
    }

    companion object {
        private const val WEEKS_TO_MONTHLY_COHORT = 4
        private const val WEEKS_TO_QUARTER_COHORT = 13
        private const val WEEKS_TO_HALF_COHORT = 26
        private const val WEEKS_TO_SINGLE_COHORT = 52
    }
}
