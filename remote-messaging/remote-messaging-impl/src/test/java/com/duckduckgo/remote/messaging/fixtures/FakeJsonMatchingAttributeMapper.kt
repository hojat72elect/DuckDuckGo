package com.duckduckgo.remote.messaging.fixtures

import com.duckduckgo.remote.messaging.api.JsonMatchingAttribute
import com.duckduckgo.remote.messaging.api.JsonToMatchingAttributeMapper
import com.duckduckgo.remote.messaging.api.MatchingAttribute

class FakeJsonMatchingAttributeMapper : JsonToMatchingAttributeMapper {
    override fun map(
        key: String,
        jsonMatchingAttribute: JsonMatchingAttribute,
    ): MatchingAttribute? {
        if (key == "Fake") {
            return FakeMatchingAttribute(jsonMatchingAttribute.value as Boolean)
        }

        return null
    }
}

data class FakeMatchingAttribute(val value: Boolean) : MatchingAttribute
