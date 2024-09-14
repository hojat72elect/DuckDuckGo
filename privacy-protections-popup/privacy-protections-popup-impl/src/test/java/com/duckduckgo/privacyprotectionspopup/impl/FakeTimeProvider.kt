

package com.duckduckgo.privacyprotectionspopup.impl

import java.time.Instant

class FakeTimeProvider : TimeProvider {

    var time: Instant = Instant.parse("2023-11-29T10:15:30.000Z")

    override fun getCurrentTime(): Instant = time
}
