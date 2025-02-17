

package com.duckduckgo.app.browser.history

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.duckduckgo.app.browser.favicon.FaviconManager
import com.duckduckgo.common.ui.view.listitem.OneLineListItem
import com.duckduckgo.mobile.android.databinding.RowOneLineListItemBinding
import kotlinx.coroutines.launch

class NavigationHistoryAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val faviconManager: FaviconManager,
    private val tabId: String,
    private val listener: NavigationHistoryListener,
) : RecyclerView.Adapter<NavigationViewHolder>() {

    interface NavigationHistoryListener {
        fun historicalPageSelected(stackIndex: Int)
    }

    private var navigationHistory: List<NavigationHistoryEntry> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): NavigationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RowOneLineListItemBinding.inflate(inflater, parent, false)
        return NavigationViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: NavigationViewHolder,
        position: Int,
    ) {
        val entry = navigationHistory[position]
        val listItem = holder.binding.root

        loadFavicon(entry, holder.binding.root)
        listItem.setPrimaryText(entry.title.orEmpty())
        listItem.setOnClickListener { listener.historicalPageSelected(position) }
    }

    override fun getItemCount(): Int {
        return navigationHistory.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateNavigationHistory(navigationHistory: List<NavigationHistoryEntry>) {
        this.navigationHistory = navigationHistory
        notifyDataSetChanged()
    }

    private fun loadFavicon(
        historyEntry: NavigationHistoryEntry,
        oneListItem: OneLineListItem,
    ) {
        lifecycleOwner.lifecycleScope.launch {
            faviconManager.loadToViewFromLocalWithPlaceholder(url = historyEntry.url, tabId = tabId, view = oneListItem.leadingIcon())
        }
    }
}

data class NavigationViewHolder(val binding: RowOneLineListItemBinding) :
    RecyclerView.ViewHolder(binding.root)

data class NavigationHistoryEntry(
    val title: String? = null,
    val url: String,
)
