package com.duckduckgo.app.tabs.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.duckduckgo.app.global.model.Site
import com.duckduckgo.app.tabs.model.TabSwitcherData.LayoutType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface TabRepository {

    /**
     * @return the tabs that are NOT marked as deletable in the DB
     */
    val liveTabs: LiveData<List<TabEntity>>

    val flowTabs: Flow<List<TabEntity>>

    val childClosedTabs: SharedFlow<String>

    /**
     * @return the tabs that are marked as "deletable" in the DB
     */
    val flowDeletableTabs: Flow<List<TabEntity>>

    val liveSelectedTab: LiveData<TabEntity>

    val tabSwitcherData: Flow<TabSwitcherData>

    /**
     * @return tabId of new record
     */
    suspend fun add(
        url: String? = null,
        skipHome: Boolean = false,
    ): String

    suspend fun addDefaultTab(): String

    suspend fun addFromSourceTab(
        url: String? = null,
        skipHome: Boolean = false,
        sourceTabId: String,
    ): String

    suspend fun addNewTabAfterExistingTab(
        url: String? = null,
        tabId: String,
    )

    suspend fun update(
        tabId: String,
        site: Site?,
    )

    suspend fun updateTabPosition(from: Int, to: Int)

    /**
     * @return record if it exists, otherwise a new one
     */
    fun retrieveSiteData(tabId: String): MutableLiveData<Site>

    suspend fun delete(tab: TabEntity)

    suspend fun markDeletable(tab: TabEntity)

    suspend fun undoDeletable(tab: TabEntity)

    /**
     * Deletes from the DB all tabs that are marked as "deletable"
     */
    suspend fun purgeDeletableTabs()

    suspend fun getDeletableTabIds(): List<String>

    suspend fun deleteTabAndSelectSource(tabId: String)

    suspend fun deleteAll()

    suspend fun select(tabId: String)

    fun updateTabPreviewImage(
        tabId: String,
        fileName: String?,
    )

    fun updateTabFavicon(
        tabId: String,
        fileName: String?,
    )

    suspend fun selectByUrlOrNewTab(url: String)

    suspend fun setIsUserNew(isUserNew: Boolean)

    suspend fun setWasAnnouncementDismissed(wasDismissed: Boolean)

    suspend fun setAnnouncementDisplayCount(displayCount: Int)

    suspend fun setTabLayoutType(layoutType: LayoutType)
}
