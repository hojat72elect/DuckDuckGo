

package com.duckduckgo.autofill.impl.jsbridge.request

import com.duckduckgo.common.utils.DefaultDispatcherProvider
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.moshi.Moshi
import javax.inject.Inject
import kotlinx.coroutines.withContext

interface AutofillRequestParser {
    suspend fun parseAutofillDataRequest(request: String): Result<AutofillDataRequest>
    suspend fun parseStoreFormDataRequest(request: String): Result<AutofillStoreFormDataRequest>
}

@ContributesBinding(AppScope::class)
class AutofillJsonRequestParser @Inject constructor(
    val moshi: Moshi,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
) : AutofillRequestParser {

    private val autofillDataRequestParser by lazy { moshi.adapter(AutofillDataRequest::class.java) }
    private val autofillStoreFormDataRequestParser by lazy { moshi.adapter(AutofillStoreFormDataRequest::class.java) }

    override suspend fun parseAutofillDataRequest(request: String): Result<AutofillDataRequest> {
        return withContext(dispatchers.io()) {
            val result = kotlin.runCatching {
                autofillDataRequestParser.fromJson(request)
            }.getOrNull()

            return@withContext if (result == null) {
                Result.failure(IllegalArgumentException("Failed to parse autofill JSON for AutofillDataRequest"))
            } else {
                Result.success(result)
            }
        }
    }

    override suspend fun parseStoreFormDataRequest(request: String): Result<AutofillStoreFormDataRequest> {
        return withContext(dispatchers.io()) {
            val result = kotlin.runCatching {
                autofillStoreFormDataRequestParser.fromJson(request)
            }.getOrNull()

            return@withContext if (result == null) {
                Result.failure(IllegalArgumentException("Failed to parse autofill JSON for AutofillStoreFormDataRequest"))
            } else {
                Result.success(result)
            }
        }
    }
}
