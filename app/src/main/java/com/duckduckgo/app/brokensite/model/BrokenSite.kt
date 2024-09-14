

package com.duckduckgo.app.brokensite.model

import androidx.annotation.StringRes
import com.duckduckgo.app.browser.R

sealed class BrokenSiteCategory(
    @StringRes val category: Int,
    val key: String,
) {
    object BlockedCategory : BrokenSiteCategory(R.string.brokenSiteCategoryBlocked, BLOCKED_CATEGORY_KEY)
    object LayoutCategory : BrokenSiteCategory(R.string.brokenSiteCategoryLayout, LAYOUT_CATEGORY_KEY)
    object EmptySpacesCategory : BrokenSiteCategory(R.string.brokenSiteCategoryEmptySpaces, EMPTY_SPACES_CATEGORY_KEY)
    object ShoppingCategory : BrokenSiteCategory(R.string.brokenSiteCategoryShopping, SHOPPING_CATEGORY_KEY)
    object PaywallCategory : BrokenSiteCategory(R.string.brokenSiteCategoryPaywall, PAYWALL_CATEGORY_KEY)
    object CommentsCategory : BrokenSiteCategory(R.string.brokenSiteCategoryComments, COMMENTS_CATEGORY_KEY)
    object VideosCategory : BrokenSiteCategory(R.string.brokenSiteCategoryVideos, VIDEOS_CATEGORY_KEY)
    object LoginCategory : BrokenSiteCategory(R.string.brokenSiteCategoryLogin, LOGIN_CATEGORY_KEY)
    object OtherCategory : BrokenSiteCategory(R.string.brokenSiteCategoryOther, OTHER_CATEGORY_KEY)

    companion object {
        const val BLOCKED_CATEGORY_KEY = "blocked"
        const val LAYOUT_CATEGORY_KEY = "layout"
        const val EMPTY_SPACES_CATEGORY_KEY = "empty-spaces"
        const val SHOPPING_CATEGORY_KEY = "shopping"
        const val PAYWALL_CATEGORY_KEY = "paywall"
        const val COMMENTS_CATEGORY_KEY = "comments"
        const val VIDEOS_CATEGORY_KEY = "videos"
        const val LOGIN_CATEGORY_KEY = "login"
        const val OTHER_CATEGORY_KEY = "other"
    }
}
