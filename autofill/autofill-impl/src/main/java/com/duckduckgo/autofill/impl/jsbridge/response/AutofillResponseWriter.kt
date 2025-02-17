

package com.duckduckgo.autofill.impl.jsbridge.response

import com.duckduckgo.autofill.impl.domain.javascript.JavascriptCredentials
import com.duckduckgo.autofill.impl.jsbridge.response.EmailProtectionInContextSignupDismissedAtResponse.DismissedAt
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.moshi.Moshi
import javax.inject.Inject

interface AutofillResponseWriter {
    fun generateResponseGetAutofillData(credentials: JavascriptCredentials): String
    fun generateEmptyResponseGetAutofillData(): String
    fun generateResponseForAcceptingGeneratedPassword(): String
    fun generateResponseForRejectingGeneratedPassword(): String
    fun generateResponseForEmailProtectionInContextSignup(installedRecently: Boolean, permanentlyDismissedAtTimestamp: Long?): String
    fun generateResponseForEmailProtectionEndOfFlow(isSignedIn: Boolean): String
}

@ContributesBinding(AppScope::class)
class AutofillJsonResponseWriter @Inject constructor(val moshi: Moshi) : AutofillResponseWriter {

    private val autofillDataAdapterCredentialsAvailable = moshi.adapter(ContainingCredentials::class.java).indent("  ")
    private val autofillDataAdapterCredentialsUnavailable = moshi.adapter(EmptyResponse::class.java).indent("  ")
    private val autofillDataAdapterAcceptGeneratedPassword = moshi.adapter(AcceptGeneratedPasswordResponse::class.java).indent("  ")
    private val autofillDataAdapterRejectGeneratedPassword = moshi.adapter(RejectGeneratedPasswordResponse::class.java).indent("  ")
    private val emailProtectionDataAdapterInContextSignup = moshi.adapter(EmailProtectionInContextSignupDismissedAtResponse::class.java).indent("  ")
    private val emailDataAdapterInContextEndOfFlow = moshi.adapter(ShowInContextEmailProtectionSignupPromptResponse::class.java).indent("  ")

    override fun generateResponseGetAutofillData(credentials: JavascriptCredentials): String {
        val credentialsResponse = ContainingCredentials.CredentialSuccessResponse(credentials)
        val topLevelResponse = ContainingCredentials(success = credentialsResponse)
        return autofillDataAdapterCredentialsAvailable.toJson(topLevelResponse)
    }

    override fun generateEmptyResponseGetAutofillData(): String {
        val credentialsResponse = EmptyResponse.EmptyCredentialResponse()
        val topLevelResponse = EmptyResponse(success = credentialsResponse)
        return autofillDataAdapterCredentialsUnavailable.toJson(topLevelResponse)
    }

    override fun generateResponseForAcceptingGeneratedPassword(): String {
        val response = AcceptGeneratedPasswordResponse.AcceptGeneratedPassword()
        val topLevelResponse = AcceptGeneratedPasswordResponse(success = response)
        return autofillDataAdapterAcceptGeneratedPassword.toJson(topLevelResponse)
    }

    override fun generateResponseForRejectingGeneratedPassword(): String {
        val response = RejectGeneratedPasswordResponse.RejectGeneratedPassword()
        val topLevelResponse = RejectGeneratedPasswordResponse(success = response)
        return autofillDataAdapterRejectGeneratedPassword.toJson(topLevelResponse)
    }

    override fun generateResponseForEmailProtectionInContextSignup(installedRecently: Boolean, permanentlyDismissedAtTimestamp: Long?): String {
        val response = DismissedAt(isInstalledRecently = installedRecently, permanentlyDismissedAt = permanentlyDismissedAtTimestamp)
        val topLevelResponse = EmailProtectionInContextSignupDismissedAtResponse(success = response)
        return emailProtectionDataAdapterInContextSignup.toJson(topLevelResponse)
    }

    override fun generateResponseForEmailProtectionEndOfFlow(isSignedIn: Boolean): String {
        val response = ShowInContextEmailProtectionSignupPromptResponse.SignupResponse(isSignedIn = isSignedIn)
        val topLevelResponse = ShowInContextEmailProtectionSignupPromptResponse(success = response)
        return emailDataAdapterInContextEndOfFlow.toJson(topLevelResponse)
    }
}
