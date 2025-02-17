

package com.duckduckgo.app.browser.logindetection

import com.duckduckgo.common.utils.ValidUrl
import java.util.regex.Pattern

private val twoFactorAuthUrlPatterns = mapOf<String, Set<Pattern>>(
    Pair("accounts.google.com", setOf(Pattern.compile("signin/v\\d.*/challenge"))),
    Pair("sso", setOf(Pattern.compile("duosecurity/getduo"))),
    Pair("amazon.com", setOf(Pattern.compile("ap/challenge"), Pattern.compile("ap/cvf/approval"))),
)

private var ssoUrlPatterns = mapOf<String, Set<Pattern>>(
    Pair("sso", setOf(Pattern.compile("saml2/idp/SSOService"))),
)

// Following patterns have been extracted from each domain oauth official docs
private var oAuthUrlPatterns = mapOf<String, Set<Pattern>>(
    Pair("accounts.google.com", setOf(Pattern.compile("o/oauth2/auth"), Pattern.compile("o/oauth2/v\\d.*/auth"))),
    Pair("appleid.apple.com", setOf(Pattern.compile("auth/authorize"))),
    Pair("amazon.com", setOf(Pattern.compile("ap/oa"))),
    Pair("auth.atlassian.com", setOf(Pattern.compile("authorize"))),
    Pair("facebook.com", setOf(Pattern.compile("/v\\d.*/dialog/oauth"), Pattern.compile("dialog/oauth"))),
    Pair("login.microsoftonline.com", setOf(Pattern.compile("common/oauth2/authorize"), Pattern.compile("common/oauth2/v2.0/authorize"))),
    Pair("linkedin.com", setOf(Pattern.compile("oauth/v\\d.*/authorization"))),
    Pair("github.com", setOf(Pattern.compile("login/oauth/authorize"))),
    Pair("api.twitter.com", setOf(Pattern.compile("oauth/authenticate"), Pattern.compile("oauth/authorize"))),
    Pair("duosecurity.com", setOf(Pattern.compile("oauth/v\\d.*/authorize"))),
)

fun ValidUrl.isOAuthUrl(): Boolean = this.findMatchIn(oAuthUrlPatterns)

fun ValidUrl.is2FAUrl(): Boolean = this.findMatchIn(twoFactorAuthUrlPatterns)

fun ValidUrl.isSSOUrl(): Boolean = this.findMatchIn(ssoUrlPatterns)

private fun ValidUrl.findMatchIn(urlPatterns: Map<String, Set<Pattern>>): Boolean {
    urlPatterns.keys
        .firstOrNull { host.contains(it) }
        ?.let { urlPatterns[it] }
        ?.forEach {
            if (it.matcher(path.orEmpty()).find()) {
                return true
            }
        }.let { return false }
}
