package com.duckduckgo.app.browser

import android.net.Uri
import androidx.collection.LruCache
import androidx.core.util.PatternsCompat
import com.duckduckgo.common.utils.UrlScheme
import com.duckduckgo.common.utils.baseHost
import com.duckduckgo.common.utils.withScheme
import okhttp3.HttpUrl.Companion.toHttpUrl
import timber.log.Timber

class UriString {

    companion object {
        private const val localhost = "localhost"
        private const val space = " "
        private val webUrlRegex by lazy { PatternsCompat.WEB_URL.toRegex() }
        private val domainRegex by lazy { PatternsCompat.DOMAIN_NAME.toRegex() }
        private val cache = LruCache<Int, Boolean>(250_000)

        fun host(uriString: String): String? {
            return Uri.parse(uriString).baseHost
        }

        fun sameOrSubdomain(
            child: String,
            parent: String,
        ): Boolean {
            val childHost = host(child) ?: return false
            val parentHost = host(parent) ?: return false
            return parentHost == childHost || childHost.endsWith(".$parentHost")
        }

        fun sameOrSubdomain(
            child: Domain?,
            parent: Domain,
        ): Boolean {
            child ?: return false
            val hash = (child.value + parent.value).hashCode()
            return cache.get(hash)
                ?: (parent == child || child.value.endsWith(".${parent.value}")).also {
                    cache.put(hash, it)
                }
        }

        fun sameOrSubdomain(
            child: Uri,
            parent: String,
        ): Boolean {
            val childHost = child.host ?: return false
            val parentHost = host(parent) ?: return false
            return parentHost == childHost || childHost.endsWith(".$parentHost")
        }

        fun sameOrSubdomain(
            child: Uri,
            parent: Domain,
        ): Boolean {
            val childHost = child.host ?: return false
            val parentHost = host(parent.value) ?: return false
            return parentHost == childHost || childHost.endsWith(".$parentHost")
        }

        fun sameOrSubdomainPair(
            first: Uri,
            second: String,
        ): Boolean {
            val childHost = first.host ?: return false
            val parentHost = host(second) ?: return false
            return parentHost == childHost || (childHost.endsWith(".$parentHost") || parentHost.endsWith(
                ".$childHost"
            ))
        }

        fun sameOrSubdomainPair(
            first: Uri,
            second: Uri,
        ): Boolean {
            val childHost = first.host ?: return false
            val parentHost = second.host ?: return false
            return parentHost == childHost || (childHost.endsWith(".$parentHost") || parentHost.endsWith(
                ".$childHost"
            ))
        }

        fun isWebUrl(inputQuery: String): Boolean {
            if (inputQuery.contains("\"") || inputQuery.contains("'")) {
                return false
            }
            if (inputQuery.contains(space)) return false
            val rawUri = Uri.parse(inputQuery)

            val uri = rawUri.withScheme()
            if (!uri.hasWebScheme()) return false
            if (uri.userInfo != null) return false

            val host = uri.host ?: return false
            if (host == localhost) return true
            if (host.contains("!")) return false

            if (webUrlRegex.containsMatchIn(host)) return true

            return try {
                // this will throw an exception if OkHttp thinks it's not a well-formed HTTP or HTTPS URL
                uri.toString().toHttpUrl()

                // it didn't match the regex and OkHttp thinks it looks good
                // we might have prepended a scheme to let okHttp check, so only consider it valid at this point if the scheme was manually provided
                // e.g., this means "http://raspberrypi" will be considered a webUrl, but "raspberrypi" will not
                rawUri.hasWebScheme()
            } catch (e: IllegalArgumentException) {
                Timber.i("Failed to parse %s as a web url; assuming it isn't", inputQuery)
                false
            }
        }

        fun isValidDomain(domain: String): Boolean {
            return domainRegex.matches(domain)
        }

        private fun Uri.hasWebScheme(): Boolean {
            val normalized = normalizeScheme()
            return normalized.scheme == UrlScheme.http || normalized.scheme == UrlScheme.https
        }
    }
}
