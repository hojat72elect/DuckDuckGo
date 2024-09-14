

package com.duckduckgo.privacyprotectionspopup.impl

import android.net.Uri
import com.duckduckgo.app.privacy.db.UserAllowListRepository
import com.duckduckgo.common.utils.extractDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeUserAllowlistRepository : UserAllowListRepository {

    private val domains = MutableStateFlow<List<String>>(emptyList())

    override fun isUrlInUserAllowList(url: String): Boolean = isDomainInUserAllowList(url.extractDomain())

    override fun isUriInUserAllowList(uri: Uri): Boolean = throw UnsupportedOperationException()

    override fun isDomainInUserAllowList(domain: String?): Boolean = domain in domains.value

    override fun domainsInUserAllowList(): List<String> = domains.value

    override fun domainsInUserAllowListFlow(): Flow<List<String>> = domains

    override suspend fun addDomainToUserAllowList(domain: String) = domains.update { it + domain }

    override suspend fun removeDomainFromUserAllowList(domain: String) = domains.update { it - domain }
}
