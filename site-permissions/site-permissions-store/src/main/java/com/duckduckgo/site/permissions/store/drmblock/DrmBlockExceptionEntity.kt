

package com.duckduckgo.site.permissions.store.drmblock

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.duckduckgo.feature.toggles.api.FeatureExceptions.FeatureException

@Entity(tableName = "drm_block_exceptions")
data class DrmBlockExceptionEntity(@PrimaryKey val domain: String)

fun DrmBlockExceptionEntity.toFeatureException(): FeatureException {
    return FeatureException(domain = this.domain, reason = null)
}
