package com.duckduckgo.app.tabs.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tabs",
    foreignKeys = [
        ForeignKey(
            entity = TabEntity::class,
            parentColumns = ["tabId"],
            childColumns = ["sourceTabId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("tabId"),
    ],
)
data class TabEntity(
    @PrimaryKey var tabId: String,
    var url: String? = null,
    var title: String? = null,
    var skipHome: Boolean = false,
    var viewed: Boolean = true,
    var position: Int,
    var tabPreviewFile: String? = null,
    var sourceTabId: String? = null,
    var deletable: Boolean = false,
)

val TabEntity.isBlank: Boolean
    get() = title == null && url == null
