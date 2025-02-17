

package com.duckduckgo.site.permissions.api

import android.webkit.PermissionRequest

/** Public interface for managing site permissions data */
interface SitePermissionsManager {
    /**
     * Returns an array of permissions that we support and the user has to manually handle
     *
     * @param tabId the tab where the request was originated
     * @param request original permission request
     * @return map where keys are the type [PermissionsKey] and have a list of [String] as values
     */
    suspend fun getSitePermissions(tabId: String, request: PermissionRequest): SitePermissions

    /**
     * Deletes all site permissions but the ones that are fireproof
     *
     * @param fireproofDomains list of domains that are fireproof
     */
    suspend fun clearAllButFireproof(fireproofDomains: List<String>)

    /**
     * Returns the proper response for a permissions.query JavaScript API call - see
     * https://developer.mozilla.org/en-US/docs/Web/API/Permissions/query
     *
     * @param url website querying the permission
     * @param tabId the tab where the query was originated
     * @param queriedPermission permission being queried (note: this is different from WebView permissions, check link above)
     * @return state of the permission as expected by the API: 'granted', 'prompt', or 'denied'
     */
    fun getPermissionsQueryResponse(url: String, tabId: String, queriedPermission: String): SitePermissionQueryResponse

    data class SitePermissions(
        val autoAccept: List<String>,
        val userHandled: List<String>,
    )

    /**
     * Contains possible responses to the permissions.query JavaScript API call - see
     * https://developer.mozilla.org/en-US/docs/Web/API/Permissions/query
     */
    sealed class SitePermissionQueryResponse {
        object Granted : SitePermissionQueryResponse()
        object Prompt : SitePermissionQueryResponse()
        object Denied : SitePermissionQueryResponse()
    }
}
