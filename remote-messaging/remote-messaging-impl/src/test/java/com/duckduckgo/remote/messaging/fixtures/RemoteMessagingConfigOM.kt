

package com.duckduckgo.remote.messaging.fixtures

import com.duckduckgo.remote.messaging.store.RemoteMessagingConfig

object RemoteMessagingConfigOM {

    fun aRemoteMessagingConfig(
        id: Int = 1,
        version: Long = 0L,
        invalidate: Boolean = false,
        evaluationTimestamp: String = "",
    ) = RemoteMessagingConfig(
        id = id,
        version = version,
        invalidate = invalidate,
        evaluationTimestamp = evaluationTimestamp,
    )
}
