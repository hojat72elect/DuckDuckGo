

package com.duckduckgo.remote.messaging.fixtures

import com.duckduckgo.remote.messaging.impl.mappers.DefaultBrowserActionMapper
import com.duckduckgo.remote.messaging.impl.mappers.DismissActionMapper
import com.duckduckgo.remote.messaging.impl.mappers.PlayStoreActionMapper
import com.duckduckgo.remote.messaging.impl.mappers.ShareActionMapper
import com.duckduckgo.remote.messaging.impl.mappers.UrlActionMapper

val messageActionPlugins = listOf(
    UrlActionMapper(),
    DismissActionMapper(),
    PlayStoreActionMapper(),
    DefaultBrowserActionMapper(),
    ShareActionMapper(),
).toSet()
