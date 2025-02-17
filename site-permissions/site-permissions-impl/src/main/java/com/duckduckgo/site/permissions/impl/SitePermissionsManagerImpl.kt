

package com.duckduckgo.site.permissions.impl

import android.content.pm.PackageManager
import android.webkit.PermissionRequest
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.site.permissions.api.SitePermissionsManager
import com.duckduckgo.site.permissions.api.SitePermissionsManager.SitePermissionQueryResponse
import com.duckduckgo.site.permissions.api.SitePermissionsManager.SitePermissions
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import kotlinx.coroutines.withContext

// Cannot be a Singleton
@ContributesBinding(AppScope::class)
class SitePermissionsManagerImpl @Inject constructor(
    private val packageManager: PackageManager,
    private val sitePermissionsRepository: SitePermissionsRepository,
    private val dispatcherProvider: DispatcherProvider,
) : SitePermissionsManager {

    private fun getSitePermissionsGranted(
        url: String,
        tabId: String,
        resources: Array<String>,
    ): Array<String> =
        resources
            .filter { sitePermissionsRepository.isDomainGranted(url, tabId, it) }
            .toTypedArray()

    override suspend fun getSitePermissions(
        tabId: String,
        request: PermissionRequest,
    ): SitePermissions {
        val autoAccept = mutableListOf<String>()
        val url = request.origin.toString()
        val sitePermissionsAllowedToAsk = request.resources
            .filter { isPermissionSupported(it) && isHardwareSupported(it) }
            .filter { sitePermissionsRepository.isDomainAllowedToAsk(url, it) }
            .toTypedArray()

        val sitePermissionsGranted = getSitePermissionsGranted(url, tabId, sitePermissionsAllowedToAsk)
        if (sitePermissionsGranted.isNotEmpty()) {
            withContext(dispatcherProvider.main()) {
                autoAccept.addAll(sitePermissionsGranted)
            }
        }
        val userList = sitePermissionsAllowedToAsk.filter { !sitePermissionsGranted.contains(it) }
        if (userList.isEmpty() && sitePermissionsGranted.isEmpty()) {
            withContext(dispatcherProvider.main()) {
                request.deny()
            }
        }
        if (userList.isEmpty() && autoAccept.isNotEmpty()) {
            withContext(dispatcherProvider.main()) {
                request.grant(autoAccept.toTypedArray())
                autoAccept.clear()
            }
        }

        return SitePermissions(autoAccept = autoAccept, userHandled = userList)
    }

    override suspend fun clearAllButFireproof(fireproofDomains: List<String>) {
        sitePermissionsRepository.sitePermissionsForAllWebsites().forEach { permission ->
            if (!fireproofDomains.contains(permission.domain)) {
                sitePermissionsRepository.deletePermissionsForSite(permission.domain)
            }
        }
    }

    override fun getPermissionsQueryResponse(
        url: String,
        tabId: String,
        queriedPermission: String,
    ): SitePermissionQueryResponse {
        getAndroidPermission(queriedPermission)?.let { androidPermission ->
            if (sitePermissionsRepository.isDomainGranted(url, tabId, androidPermission)) {
                return SitePermissionQueryResponse.Granted
            } else if (isHardwareSupported(androidPermission) && sitePermissionsRepository.isDomainAllowedToAsk(url, androidPermission)) {
                return SitePermissionQueryResponse.Prompt
            }
            return SitePermissionQueryResponse.Denied
        }

        return SitePermissionQueryResponse.Denied
    }

    private fun isPermissionSupported(permission: String): Boolean =
        permission == PermissionRequest.RESOURCE_AUDIO_CAPTURE || permission == PermissionRequest.RESOURCE_VIDEO_CAPTURE ||
            permission == PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID

    private fun isHardwareSupported(permission: String): Boolean = when (permission) {
        PermissionRequest.RESOURCE_VIDEO_CAPTURE -> {
            kotlin.runCatching { packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) }.getOrDefault(false)
        }
        else -> {
            true
        }
    }

    private fun getAndroidPermission(webQueryPermission: String): String? {
        return when (webQueryPermission) {
            "camera" -> PermissionRequest.RESOURCE_VIDEO_CAPTURE
            "microphone" -> PermissionRequest.RESOURCE_AUDIO_CAPTURE
            else -> null
        }
    }
}
