

package com.duckduckgo.httpsupgrade.impl

import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.lifecycle.LifecycleOwner
import com.duckduckgo.app.lifecycle.MainProcessLifecycleObserver
import com.duckduckgo.common.utils.isHttps
import com.duckduckgo.common.utils.toHttps
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.FeatureToggle
import com.duckduckgo.httpsupgrade.api.HttpsUpgrader
import com.duckduckgo.httpsupgrade.store.HttpsFalsePositivesDao
import com.duckduckgo.privacy.config.api.Https
import com.duckduckgo.privacy.config.api.PrivacyFeatureName
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.anvil.annotations.ContributesMultibinding
import dagger.SingleInstanceIn
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.thread
import logcat.logcat

@SingleInstanceIn(AppScope::class)
@ContributesBinding(
    scope = AppScope::class,
    boundType = HttpsUpgrader::class,
)
@ContributesMultibinding(
    scope = AppScope::class,
    boundType = MainProcessLifecycleObserver::class,
)
class HttpsUpgraderImpl @Inject constructor(
    private val bloomFactory: HttpsBloomFilterFactory,
    private val bloomFalsePositiveDao: HttpsFalsePositivesDao,
    private val toggle: FeatureToggle,
    private val https: Https,
) : HttpsUpgrader, MainProcessLifecycleObserver {

    private var bloomFilter: BloomFilter? = null
    private val bloomReloadLock = ReentrantLock()

    override fun onCreate(owner: LifecycleOwner) {
        thread { reloadData() }
    }

    @WorkerThread
    override fun shouldUpgrade(uri: Uri): Boolean {
        val host = uri.host ?: return false

        if (!toggle.isFeatureEnabled(PrivacyFeatureName.HttpsFeatureName.value)) {
            logcat { "https is disabled in the remote config and so $host is not upgradable" }
            return false
        }

        if (uri.isHttps) {
            return false
        }

        if (https.isAnException(uri.toString())) {
            logcat { "$host is in the exception list and so not upgradable" }
            return false
        }

        if (bloomFalsePositiveDao.contains(host)) {
            logcat { "$host is in https allowlist and so not upgradable" }
            return false
        }

        val isUpgradable = isInUpgradeList(host)
        logcat { "$host ${if (isUpgradable) "is" else "is not"} upgradable" }
        return isUpgradable
    }

    override fun upgrade(uri: Uri): Uri = uri.toHttps

    @WorkerThread
    private fun isInUpgradeList(host: String): Boolean {
        waitForAnyReloadsToComplete()
        return bloomFilter?.contains(host) == true
    }

    @WorkerThread
    override fun reloadData() {
        logcat { "Reload Https upgrader data" }
        bloomReloadLock.lock()
        bloomFilter = runCatching { bloomFactory.create() }.getOrNull()
        bloomReloadLock.unlock()
    }

    private fun waitForAnyReloadsToComplete() {
        // wait for lock (by locking and unlocking) before continuing
        if (bloomReloadLock.isLocked) {
            bloomReloadLock.lock()
            bloomReloadLock.unlock()
        }
    }
}
