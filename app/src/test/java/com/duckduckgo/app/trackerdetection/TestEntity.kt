

package com.duckduckgo.app.trackerdetection

import com.duckduckgo.app.trackerdetection.model.Entity

data class TestEntity(
    override val name: String,
    override val displayName: String,
    override val prevalence: Double,
) : Entity
