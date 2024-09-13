package com.duckduckgo.privacyprotectionspopup.impl

import com.duckduckgo.privacyprotectionspopup.impl.store.PrivacyProtectionsPopupData
import com.duckduckgo.privacyprotectionspopup.impl.store.PrivacyProtectionsPopupDataStore
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

class FakePrivacyProtectionsPopupDataStore : PrivacyProtectionsPopupDataStore {

    override val data = MutableStateFlow(
        PrivacyProtectionsPopupData(
            toggleUsedAt = null,
            popupTriggerCount = 0,
            doNotShowAgainClicked = false,
            experimentVariant = null,
        ),
    )

    override suspend fun getToggleUsageTimestamp(): Instant? =
        data.first().toggleUsedAt

    override suspend fun setToggleUsageTimestamp(timestamp: Instant) {
        data.update { it.copy(toggleUsedAt = timestamp) }
    }

    override suspend fun getPopupTriggerCount(): Int =
        data.first().popupTriggerCount

    override suspend fun setPopupTriggerCount(count: Int) {
        data.update { it.copy(popupTriggerCount = count) }
    }

    override suspend fun getDoNotShowAgainClicked(): Boolean =
        data.first().doNotShowAgainClicked

    override suspend fun setDoNotShowAgainClicked(clicked: Boolean) {
        data.update { it.copy(doNotShowAgainClicked = clicked) }
    }

    override suspend fun getExperimentVariant(): PrivacyProtectionsPopupExperimentVariant? =
        data.first().experimentVariant

    override suspend fun setExperimentVariant(variant: PrivacyProtectionsPopupExperimentVariant) {
        data.update { it.copy(experimentVariant = variant) }
    }
}
