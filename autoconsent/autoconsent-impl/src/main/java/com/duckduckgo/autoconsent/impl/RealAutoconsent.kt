

package com.duckduckgo.autoconsent.impl

import android.webkit.WebView
import com.duckduckgo.app.browser.UriString
import com.duckduckgo.app.privacy.db.UserAllowListRepository
import com.duckduckgo.autoconsent.api.Autoconsent
import com.duckduckgo.autoconsent.api.AutoconsentCallback
import com.duckduckgo.autoconsent.impl.AutoconsentInterface.Companion.AUTOCONSENT_INTERFACE
import com.duckduckgo.autoconsent.impl.handlers.ReplyHandler
import com.duckduckgo.autoconsent.impl.remoteconfig.AutoconsentExceptionsRepository
import com.duckduckgo.autoconsent.impl.remoteconfig.AutoconsentFeature
import com.duckduckgo.autoconsent.impl.store.AutoconsentSettingsRepository
import com.duckduckgo.common.utils.plugins.PluginPoint
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.privacy.config.api.UnprotectedTemporary
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class RealAutoconsent @Inject constructor(
    private val messageHandlerPlugins: PluginPoint<MessageHandlerPlugin>,
    private val settingsRepository: AutoconsentSettingsRepository,
    private val autoconsentExceptionsRepository: AutoconsentExceptionsRepository,
    private val autoconsent: AutoconsentFeature,
    private val userAllowlistRepository: UserAllowListRepository,
    private val unprotectedTemporary: UnprotectedTemporary,
) : Autoconsent {

    private lateinit var autoconsentJs: String

    override fun injectAutoconsent(webView: WebView, url: String) {
        if (isAutoconsentEnabled() && !urlInUserAllowList(url) && !isAnException(url)) {
            webView.evaluateJavascript("javascript:${getFunctionsJS()}", null)
        }
    }

    override fun addJsInterface(webView: WebView, autoconsentCallback: AutoconsentCallback) {
        webView.addJavascriptInterface(
            AutoconsentInterface(messageHandlerPlugins, webView, autoconsentCallback),
            AUTOCONSENT_INTERFACE,
        )
    }

    override fun changeSetting(setting: Boolean) {
        settingsRepository.userSetting = setting
    }

    override fun isSettingEnabled(): Boolean {
        return settingsRepository.userSetting
    }

    override fun isAutoconsentEnabled(): Boolean {
        return isEnabled() && isSettingEnabled()
    }

    override fun setAutoconsentOptOut(webView: WebView) {
        settingsRepository.userSetting = true
        webView.evaluateJavascript("javascript:${ReplyHandler.constructReply("""{ "type": "optOut" }""")}", null)
    }

    override fun setAutoconsentOptIn() {
        settingsRepository.userSetting = false
    }

    override fun firstPopUpHandled() {
        settingsRepository.firstPopupHandled = true
    }

    private fun urlInUserAllowList(url: String): Boolean {
        return try {
            userAllowlistRepository.isUrlInUserAllowList(url)
        } catch (e: Exception) {
            false
        }
    }

    private fun isEnabled(): Boolean {
        return autoconsent.self().isEnabled()
    }

    private fun isAnException(url: String): Boolean {
        return matches(url) || unprotectedTemporary.isAnException(url)
    }

    private fun matches(url: String): Boolean {
        return autoconsentExceptionsRepository.exceptions.any { UriString.sameOrSubdomain(url, it.domain) }
    }

    private fun getFunctionsJS(): String {
        if (!this::autoconsentJs.isInitialized) {
            autoconsentJs = JsReader.loadJs("autoconsent-bundle.js")
        }
        return autoconsentJs
    }
}
