// ktlint-disable filename

package com.duckduckgo.autofill.store

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.duckduckgo.feature.toggles.api.FeatureExceptions.FeatureException

@Entity(tableName = "autofill_exceptions")
data class AutofillExceptionEntity(
    @PrimaryKey val domain: String,
    val reason: String,
)

fun AutofillExceptionEntity.toFeatureException(): FeatureException {
    return FeatureException(domain = this.domain, reason = this.reason)
}
