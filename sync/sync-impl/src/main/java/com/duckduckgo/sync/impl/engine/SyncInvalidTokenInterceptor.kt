package com.duckduckgo.sync.impl.engine

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.duckduckgo.app.global.api.ApiInterceptorPlugin
import com.duckduckgo.common.utils.notification.checkPermissionAndNotify
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.sync.impl.API_CODE
import com.duckduckgo.sync.impl.SyncService
import com.squareup.anvil.annotations.ContributesMultibinding
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response
import timber.log.Timber

@ContributesMultibinding(
    scope = AppScope::class,
    boundType = ApiInterceptorPlugin::class,
)
class SyncInvalidTokenInterceptor @Inject constructor(
    private val context: Context,
    private val notificationManager: NotificationManagerCompat,
    private val syncNotificationBuilder: SyncNotificationBuilder,
) : ApiInterceptorPlugin, Interceptor {

    override fun intercept(chain: Chain): Response {
        val isSyncEndpoint =
            chain.request().url.toString().contains(SyncService.SYNC_PROD_ENVIRONMENT_URL) ||
                    chain.request().url.toString().contains(SyncService.SYNC_DEV_ENVIRONMENT_URL)

        if (!isSyncEndpoint) {
            return chain.proceed(chain.request())
        }

        val response = chain.proceed(chain.request())

        val method = chain.request().method
        if (response.code == API_CODE.INVALID_LOGIN_CREDENTIALS.code && (method == "PATCH" || method == "GET")) {
            Timber.d("Sync-Engine: User logged out, invalid token detected.")
            notificationManager.checkPermissionAndNotify(
                context,
                SYNC_USER_LOGGED_OUT_NOTIFICATION_ID,
                syncNotificationBuilder.buildSyncSignedOutNotification(context),
            )
        }
        return response
    }

    override fun getInterceptor() = this

    companion object {
        internal const val SYNC_USER_LOGGED_OUT_NOTIFICATION_ID = 8451
    }
}
