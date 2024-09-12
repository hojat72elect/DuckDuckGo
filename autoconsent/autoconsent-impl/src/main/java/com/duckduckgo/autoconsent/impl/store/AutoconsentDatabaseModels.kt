package com.duckduckgo.autoconsent.impl.store

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.duckduckgo.feature.toggles.api.FeatureExceptions.FeatureException

@Entity(tableName = "autoconsent_exceptions")
data class AutoconsentExceptionEntity(
    @PrimaryKey val domain: String,
    val reason: String,
)

fun AutoconsentExceptionEntity.toFeatureException(): FeatureException {
    return FeatureException(domain = this.domain, reason = this.reason)
}

@Entity(tableName = "autoconsent_disabled_cmps")
data class DisabledCmpsEntity(
    @PrimaryKey val name: String,
)
