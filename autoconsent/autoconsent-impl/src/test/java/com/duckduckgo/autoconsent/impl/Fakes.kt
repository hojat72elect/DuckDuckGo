

package com.duckduckgo.autoconsent.impl

import android.net.Uri
import android.webkit.WebView
import androidx.core.net.toUri
import com.duckduckgo.app.privacy.db.UserAllowListRepository
import com.duckduckgo.autoconsent.api.AutoconsentCallback
import com.duckduckgo.autoconsent.impl.store.AutoconsentSettingsRepository
import com.duckduckgo.common.utils.domain
import com.duckduckgo.common.utils.plugins.PluginPoint
import com.duckduckgo.feature.toggles.api.FeatureExceptions.FeatureException
import com.duckduckgo.privacy.config.api.UnprotectedTemporary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakePluginPoint : PluginPoint<MessageHandlerPlugin> {
    val plugin = FakeMessageHandlerPlugin()
    override fun getPlugins(): Collection<MessageHandlerPlugin> {
        return listOf(plugin)
    }
}

class FakeMessageHandlerPlugin : MessageHandlerPlugin {
    var count = 0

    override fun process(
        messageType: String,
        jsonString: String,
        webView: WebView,
        autoconsentCallback: AutoconsentCallback,
    ) {
        count++
    }

    override val supportedTypes: List<String> = listOf("fake")
}

class FakeSettingsRepository : AutoconsentSettingsRepository {
    override var userSetting: Boolean = false
    override var firstPopupHandled: Boolean = false
}

class FakeUnprotected(private val exceptionList: List<String>) : UnprotectedTemporary {
    override fun isAnException(url: String): Boolean {
        return exceptionList.contains(url.toUri().domain())
    }

    override val unprotectedTemporaryExceptions: List<FeatureException>
        get() = exceptionList.map { FeatureException(domain = it, reason = "A reason") }
}

class FakeUserAllowlist(private val userAllowList: List<String>) : UserAllowListRepository {
    override fun isUrlInUserAllowList(url: String): Boolean {
        return userAllowList.contains(url.toUri().domain())
    }

    override fun isUriInUserAllowList(uri: Uri): Boolean {
        return false
    }

    override fun isDomainInUserAllowList(domain: String?): Boolean {
        return false
    }

    override fun domainsInUserAllowList(): List<String> {
        return emptyList()
    }

    override fun domainsInUserAllowListFlow(): Flow<List<String>> {
        return flowOf(emptyList())
    }

    override suspend fun addDomainToUserAllowList(domain: String) = Unit

    override suspend fun removeDomainFromUserAllowList(domain: String) = Unit
}
