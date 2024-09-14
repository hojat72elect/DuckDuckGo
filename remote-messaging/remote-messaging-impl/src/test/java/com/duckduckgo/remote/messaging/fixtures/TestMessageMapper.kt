

package com.duckduckgo.remote.messaging.fixtures

import com.duckduckgo.remote.messaging.impl.di.RMFMapperModule
import com.duckduckgo.remote.messaging.impl.mappers.MessageMapper

fun getMessageMapper() = MessageMapper(RMFMapperModule.provideMoshiAdapter(messageActionPlugins))
