

package com.duckduckgo.app.downloads

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.duckduckgo.anvil.annotations.ContributeToActivityStarter
import com.duckduckgo.anvil.annotations.InjectWith
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.browser.databinding.ActivityDownloadsBinding
import com.duckduckgo.app.downloads.DownloadsScreens.DownloadsScreenNoParams
import com.duckduckgo.app.downloads.DownloadsViewModel.Command
import com.duckduckgo.app.downloads.DownloadsViewModel.Command.*
import com.duckduckgo.app.downloads.DownloadsViewModel.ViewState
import com.duckduckgo.common.ui.DuckDuckGoActivity
import com.duckduckgo.common.ui.view.SearchBar
import com.duckduckgo.common.ui.view.gone
import com.duckduckgo.common.ui.view.hideKeyboard
import com.duckduckgo.common.ui.view.show
import com.duckduckgo.common.ui.view.showKeyboard
import com.duckduckgo.common.ui.viewbinding.viewBinding
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.downloads.api.DownloadsFileActions
import com.duckduckgo.downloads.api.model.DownloadItem
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@InjectWith(ActivityScope::class)
@ContributeToActivityStarter(DownloadsScreenNoParams::class)
class DownloadsActivity : DuckDuckGoActivity() {

    private val viewModel: DownloadsViewModel by bindViewModel()
    private val binding: ActivityDownloadsBinding by viewBinding()

    @Inject
    lateinit var downloadsAdapter: DownloadsAdapter

    @Inject
    lateinit var downloadsFileActions: DownloadsFileActions

    private val toolbar
        get() = binding.toolbar

    private val searchBar
        get() = binding.searchBar

    private var searchMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupToolbar(toolbar)
        setupRecyclerView()

        lifecycleScope.launch {
            viewModel.viewState
                .flowWithLifecycle(lifecycle, STARTED)
                .collectLatest { render(it) }
        }

        lifecycleScope.launch {
            viewModel.commands()
                .flowWithLifecycle(lifecycle, STARTED)
                .collectLatest { processCommands(it) }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.downloads_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.downloads_delete_all -> viewModel.deleteAllDownloadedItems()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        searchMenuItem = menu.findItem(R.id.action_search)
        initializeSearchBar()
        return super.onPrepareOptionsMenu(menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (searchBar.isVisible) {
            hideSearchBar()
        } else {
            super.onBackPressed()
        }
    }

    private fun processCommands(command: Command) {
        when (command) {
            is OpenFile -> showOpen(command)
            is ShareFile -> showShare(command)
            is DisplayMessage -> showSnackbar(command.messageId, command.arg)
            is DisplayUndoMessage -> showUndo(command)
            is CancelDownload -> cancelDownload(command)
        }
    }

    private fun showOpen(command: OpenFile) {
        val file = File(command.item.filePath)
        if (file.exists()) {
            val result = downloadsFileActions.openFile(this@DownloadsActivity, file)
            if (!result) {
                showSnackbar(R.string.downloadsCannotOpenFileErrorMessage)
            }
        } else {
            viewModel.delete(command.item)
        }
    }

    private fun showShare(command: ShareFile) {
        downloadsFileActions.shareFile(this@DownloadsActivity, File(command.item.filePath))
    }

    private fun showUndo(command: DisplayUndoMessage) {
        Snackbar.make(
            binding.root,
            getString(command.messageId, command.arg),
            Snackbar.LENGTH_LONG,
        ).setAction(R.string.downloadsUndoActionName) {
            // noop, handled in onDismissed callback
        }.addCallback(
            object : Snackbar.Callback() {
                override fun onDismissed(
                    transientBottomBar: Snackbar?,
                    event: Int,
                ) {
                    when (event) {
                        // handle the UNDO action here as we only have one
                        BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_ACTION -> viewModel.insert(command.items)
                        BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_SWIPE,
                        BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_MANUAL,
                        BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_TIMEOUT,
                        -> handleDeleteAll(command.items)
                        BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_CONSECUTIVE -> { /* noop */ }
                    }
                }
            },
        ).show()
    }

    private fun handleDeleteAll(items: List<DownloadItem>) {
        viewModel.removeFromDiskAndFromDownloadManager(items)
    }

    private fun cancelDownload(command: CancelDownload) {
        viewModel.removeFromDownloadManager(command.item.downloadId)
    }

    private fun showSnackbar(@StringRes messageId: Int, arg: String = "") {
        Snackbar.make(binding.root, getString(messageId, arg), Snackbar.LENGTH_LONG).show()
    }

    private fun render(viewState: ViewState) {
        downloadsAdapter.updateData(viewState.filteredItems)
        searchMenuItem?.isVisible = itemsAvailable(viewState)
    }

    private fun itemsAvailable(viewState: ViewState): Boolean {
        // The empty view is part of the list.
        return viewState.filteredItems.size > 1
    }

    private fun setupRecyclerView() {
        downloadsAdapter.setListener(viewModel)
        binding.downloadsContentView.layoutManager = LinearLayoutManager(this)
        binding.downloadsContentView.adapter = downloadsAdapter
    }

    private fun initializeSearchBar() {
        searchMenuItem?.setOnMenuItemClickListener {
            showSearchBar()
            return@setOnMenuItemClickListener true
        }

        searchBar.onAction {
            when (it) {
                is SearchBar.Action.PerformUpAction -> hideSearchBar()
                is SearchBar.Action.PerformSearch -> viewModel.onQueryTextChange(it.searchText)
            }
        }
    }

    private fun showSearchBar() {
        toolbar.gone()
        searchBar.handle(SearchBar.Event.ShowSearchBar)
        searchBar.showKeyboard()
    }

    private fun hideSearchBar() {
        toolbar.show()
        searchBar.handle(SearchBar.Event.DismissSearchBar)
        searchBar.hideKeyboard()
    }

    companion object {
        fun intent(context: Context): Intent = Intent(context, DownloadsActivity::class.java)
    }
}
