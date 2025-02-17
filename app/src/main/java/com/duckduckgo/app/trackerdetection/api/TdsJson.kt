

package com.duckduckgo.app.trackerdetection.api

import com.duckduckgo.app.browser.Domain
import com.duckduckgo.app.trackerdetection.model.*
import com.duckduckgo.app.trackerdetection.model.Action.UNSUPPORTED
import com.squareup.moshi.FromJson
import java.util.*

class TdsJson {

    lateinit var entities: Map<String, TdsJsonEntity>
    lateinit var domains: Map<String, String?>
    lateinit var trackers: Map<String, TdsJsonTracker>
    lateinit var cnames: Map<String, String?>

    fun jsonToEntities(): List<TdsEntity> {
        return entities.mapNotNull { (key, value) ->
            TdsEntity(key, value.displayName.takeIf { !it.isNullOrBlank() } ?: key, value.prevalence)
        }
    }

    fun jsonToDomainEntities(): List<TdsDomainEntity> {
        return domains.mapNotNull { (key, value) ->
            if (value == null) {
                null
            } else {
                TdsDomainEntity(key, value)
            }
        }
    }

    fun jsonToTrackers(): Map<String, TdsTracker> {
        return trackers.mapNotNull { (key, value) ->
            val domain = value.domain ?: return@mapNotNull null
            val default = value.default ?: return@mapNotNull null
            val owner = value.owner ?: return@mapNotNull null
            key to TdsTracker(Domain(domain), default, owner.name, value.categories ?: emptyList(), value.rules ?: emptyList())
        }.toMap()
    }

    fun jsonToCnameEntities(): List<TdsCnameEntity> {
        return cnames.mapNotNull { (key, value) ->
            if (value == null) {
                null
            } else {
                TdsCnameEntity(key, value)
            }
        }
    }
}

class TdsJsonEntity(
    val displayName: String?,
    val prevalence: Double,
)

data class TdsJsonTracker(
    val domain: String?,
    val default: Action?,
    val owner: TdsJsonOwner?,
    val categories: List<String>?,
    val rules: List<Rule>?,
)

data class TdsJsonOwner(
    val name: String,
)

class ActionJsonAdapter {

    @FromJson
    fun fromJson(actionName: String): Action {
        // If action not null but not supported, return unsupported.
        // Unsupported actions are always ignored.
        return Action.values().firstOrNull { it.name == actionName.uppercase(Locale.ROOT) } ?: UNSUPPORTED
    }
}
