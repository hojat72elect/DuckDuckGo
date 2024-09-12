package com.duckduckgo.autofill.impl.engagement.store

import com.duckduckgo.autofill.impl.engagement.store.AutofillEngagementBucketing.Companion.FEW
import com.duckduckgo.autofill.impl.engagement.store.AutofillEngagementBucketing.Companion.LOTS
import com.duckduckgo.autofill.impl.engagement.store.AutofillEngagementBucketing.Companion.MANY
import com.duckduckgo.autofill.impl.engagement.store.AutofillEngagementBucketing.Companion.NONE
import com.duckduckgo.autofill.impl.engagement.store.AutofillEngagementBucketing.Companion.SOME
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

interface AutofillEngagementBucketing {
    fun bucketNumberOfSavedPasswords(savedPasswords: Int): String

    companion object {
        const val NONE = "none"
        const val FEW = "few"
        const val SOME = "some"
        const val MANY = "many"
        const val LOTS = "lots"
    }
}

@ContributesBinding(AppScope::class)
class DefaultAutofillEngagementBucketing @Inject constructor() : AutofillEngagementBucketing {

    override fun bucketNumberOfSavedPasswords(savedPasswords: Int): String {
        return when {
            savedPasswords == 0 -> NONE
            savedPasswords < 4 -> FEW
            savedPasswords < 11 -> SOME
            savedPasswords < 50 -> MANY
            else -> LOTS
        }
    }
}
