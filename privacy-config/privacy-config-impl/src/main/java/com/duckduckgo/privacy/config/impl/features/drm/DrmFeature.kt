

package com.duckduckgo.privacy.config.impl.features.drm

import com.duckduckgo.feature.toggles.api.FeatureExceptions.FeatureException

data class DrmFeature(
    val state: String,
    val minSupportedVersion: Int?,
    val exceptions: List<FeatureException>,
)
