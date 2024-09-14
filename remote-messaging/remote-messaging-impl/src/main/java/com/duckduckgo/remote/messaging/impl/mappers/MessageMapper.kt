

package com.duckduckgo.remote.messaging.impl.mappers

import com.duckduckgo.remote.messaging.api.RemoteMessage
import com.squareup.moshi.JsonAdapter

class MessageMapper(
    private val messageAdapter: JsonAdapter<RemoteMessage>,
) {

    fun toString(sitePayload: RemoteMessage): String {
        return messageAdapter.toJson(sitePayload)
    }

    fun fromMessage(payload: String): RemoteMessage? {
        return runCatching {
            messageAdapter.fromJson(payload)
        }.getOrNull()
    }
}
