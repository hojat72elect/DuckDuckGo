

package com.duckduckgo.autofill.impl.ui.credential.management.sorting

import com.duckduckgo.autofill.api.domain.app.LoginCredentials
import com.duckduckgo.autofill.impl.ui.credential.management.AutofillManagementRecyclerAdapter.ListItem
import com.duckduckgo.autofill.impl.ui.credential.management.AutofillManagementRecyclerAdapter.ListItem.CredentialListItem
import java.util.*
import javax.inject.Inject

class CredentialGrouper @Inject constructor(
    private val initialExtractor: InitialExtractor,
    private val sorter: CredentialListSorter,
) {

    fun group(unsortedCredentials: List<LoginCredentials>): List<ListItem> {
        val unsortedGroups = buildGroups(unsortedCredentials)
        val sortedGroups = sortGroups(unsortedGroups)
        return buildFlatList(sortedGroups)
    }

    private fun buildFlatList(sortedGroups: SortedMap<String, MutableList<LoginCredentials>>): MutableList<ListItem> {
        val flattenedList = mutableListOf<ListItem>()

        sortedGroups.forEach { (key, value) ->
            flattenedList.add(ListItem.GroupHeading(key))

            sorter.sort(value).forEach {
                flattenedList.add(CredentialListItem.Credential(it))
            }
        }

        return flattenedList
    }

    private fun sortGroups(groups: MutableMap<String, MutableList<LoginCredentials>>): SortedMap<String, MutableList<LoginCredentials>> {
        return groups.toSortedMap(sorter.comparator())
    }

    private fun buildGroups(unsortedCredentials: List<LoginCredentials>): MutableMap<String, MutableList<LoginCredentials>> {
        val groups = mutableMapOf<String, MutableList<LoginCredentials>>()
        unsortedCredentials.forEach { credential ->
            val initial = initialExtractor.extractInitial(credential)
            val list = groups.getOrPut(initial) { mutableListOf() }
            list.add(credential)
        }
        return groups
    }
}
