package com.duckduckgo.breakagereporting.impl

import com.duckduckgo.app.di.AppCoroutineScope
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.FragmentScope
import com.duckduckgo.js.messaging.api.JsMessage
import com.duckduckgo.js.messaging.api.JsMessageCallback
import com.duckduckgo.js.messaging.api.JsMessageHandler
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.CoroutineScope

@ContributesBinding(FragmentScope::class)
@Named("breakageMessageHandler")
class BreakageReportingMessageHandlerPlugin @Inject constructor(
    @AppCoroutineScope val appCoroutineScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
) : JsMessageHandler {

    override fun process(
        jsMessage: JsMessage,
        secret: String,
        jsMessageCallback: JsMessageCallback?,
    ) {
        jsMessageCallback?.process(featureName, jsMessage.method, jsMessage.id, jsMessage.params)
    }

    override val allowedDomains: List<String> = emptyList()
    override val featureName: String = "breakageReporting"
    override val methods: List<String> = listOf("breakageReportResult")
}
