

package com.duckduckgo.app.sitepermissions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.duckduckgo.anvil.annotations.InjectWith
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.browser.databinding.ActivitySitePermissionsBinding
import com.duckduckgo.app.browser.favicon.FaviconManager
import com.duckduckgo.app.location.data.LocationPermissionEntity
import com.duckduckgo.app.sitepermissions.SitePermissionsViewModel.Command
import com.duckduckgo.app.sitepermissions.SitePermissionsViewModel.Command.LaunchWebsiteAllowed
import com.duckduckgo.app.sitepermissions.SitePermissionsViewModel.Command.ShowRemovedAllConfirmationSnackbar
import com.duckduckgo.app.sitepermissions.permissionsperwebsite.PermissionsPerWebsiteActivity
import com.duckduckgo.common.ui.DuckDuckGoActivity
import com.duckduckgo.common.ui.viewbinding.viewBinding
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.site.permissions.store.sitepermissions.SitePermissionsEntity
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@InjectWith(ActivityScope::class)
class SitePermissionsActivity : DuckDuckGoActivity() {

    @Inject
    lateinit var faviconManager: FaviconManager

    private val viewModel: SitePermissionsViewModel by bindViewModel()
    private val binding: ActivitySitePermissionsBinding by viewBinding()
    private lateinit var adapter: SitePermissionsAdapter

    private val toolbar
        get() = binding.includeToolbar.toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupToolbar(toolbar)
        setupRecyclerView()
        observeViewModel()
        viewModel.allowedSites()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.viewState
                .flowWithLifecycle(lifecycle, STARTED)
                .collectLatest { state ->
                    val sitePermissionsWebsites = viewModel.combineAllPermissions(state.locationPermissionsAllowed, state.sitesPermissionsAllowed)
                    updateList(sitePermissionsWebsites, state.askLocationEnabled, state.askCameraEnabled, state.askMicEnabled, state.askDrmEnabled)
                }
        }
        lifecycleScope.launch {
            viewModel.commands
                .flowWithLifecycle(lifecycle, STARTED)
                .collectLatest { processCommand(it) }
        }
    }

    private fun processCommand(command: Command) {
        when (command) {
            is ShowRemovedAllConfirmationSnackbar -> showRemovedAllSnackbar(command.removedSitePermissions, command.removedLocationPermissions)
            is LaunchWebsiteAllowed -> launchWebsiteAllowed(command.domain)
        }
    }

    private fun showRemovedAllSnackbar(
        removedSitePermissions: List<SitePermissionsEntity>,
        removedLocationPermissions: List<LocationPermissionEntity>,
    ) {
        val message = HtmlCompat.fromHtml(getString(R.string.sitePermissionsRemoveAllWebsitesSnackbarText), HtmlCompat.FROM_HTML_MODE_LEGACY)
        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_LONG,
        ).setAction(R.string.fireproofWebsiteSnackbarAction) {
            viewModel.onSnackBarUndoRemoveAllWebsites(removedSitePermissions, removedLocationPermissions)
        }.show()
    }

    private fun updateList(
        sitesAllowed: List<String>,
        askLocationEnabled: Boolean,
        askCameraEnabled: Boolean,
        askMicEnabled: Boolean,
        askDrmEnabled: Boolean,
    ) {
        adapter.updateItems(sitesAllowed, askLocationEnabled, askCameraEnabled, askMicEnabled, askDrmEnabled)
    }

    private fun setupRecyclerView() {
        adapter = SitePermissionsAdapter(viewModel, this, faviconManager)
        binding.recycler.adapter = adapter
    }

    private fun launchWebsiteAllowed(domain: String) {
        startActivity(PermissionsPerWebsiteActivity.intent(this, domain))
    }

    companion object {
        fun intent(context: Context): Intent {
            return Intent(context, SitePermissionsActivity::class.java)
        }
    }
}
