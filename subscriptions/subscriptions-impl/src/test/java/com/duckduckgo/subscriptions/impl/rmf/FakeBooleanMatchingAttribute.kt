package com.duckduckgo.subscriptions.impl.rmf

import com.duckduckgo.remote.messaging.api.MatchingAttribute

internal interface FakeBooleanMatchingAttribute : MatchingAttribute {
    val value: Boolean
}

internal interface FakeStringMatchingAttribute : MatchingAttribute {
    val value: String
}

@Suppress("TestFunctionName")
internal fun FakeBooleanMatchingAttribute(block: () -> Boolean): MatchingAttribute =
    object : FakeBooleanMatchingAttribute {
        override val value: Boolean = block()
    }

@Suppress("TestFunctionName")
internal fun FakeStringMatchingAttribute(block: () -> String): MatchingAttribute =
    object : FakeStringMatchingAttribute {
        override val value: String = block()
    }
