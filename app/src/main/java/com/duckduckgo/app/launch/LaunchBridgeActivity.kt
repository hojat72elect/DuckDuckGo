

package com.duckduckgo.app.launch

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.duckduckgo.anvil.annotations.InjectWith
import com.duckduckgo.app.browser.BrowserActivity
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.onboarding.ui.OnboardingActivity
import com.duckduckgo.common.ui.DuckDuckGoActivity
import com.duckduckgo.di.scopes.ActivityScope
import kotlinx.coroutines.launch

@InjectWith(ActivityScope::class)
class LaunchBridgeActivity : DuckDuckGoActivity() {

    private val viewModel: LaunchViewModel by bindViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { true }

        setContentView(R.layout.activity_launch)

        configureObservers()

        lifecycleScope.launch { viewModel.determineViewToShow() }
    }

    private fun configureObservers() {
        viewModel.command.observe(this) {
            processCommand(it)
        }
    }

    private fun processCommand(it: LaunchViewModel.Command) {
        when (it) {
            is LaunchViewModel.Command.Onboarding -> {
                showOnboarding()
            }

            is LaunchViewModel.Command.Home -> {
                showHome()
            }
        }
    }

    private fun showOnboarding() {
        startActivity(OnboardingActivity.intent(this))
        finish()
    }

    private fun showHome() {
        startActivity(BrowserActivity.intent(this))
        overridePendingTransition(0, 0)
        finish()
    }
}
