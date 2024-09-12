package com.duckduckgo.app.browser.mediaplayback

import com.duckduckgo.feature.toggles.api.Toggle

interface MediaPlaybackFeature {
    @Toggle.DefaultValue(true)
    fun self(): Toggle
}
