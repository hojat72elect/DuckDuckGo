package com.duckduckgo.subscriptions.impl.feedback

import android.os.Bundle
import android.view.View
import com.duckduckgo.anvil.annotations.InjectWith
import com.duckduckgo.common.ui.viewbinding.viewBinding
import com.duckduckgo.di.scopes.FragmentScope
import com.duckduckgo.subscriptions.impl.R
import com.duckduckgo.subscriptions.impl.databinding.ContentFeedbackCategoryBinding
import com.duckduckgo.subscriptions.impl.feedback.SubscriptionFeedbackCategory.ITR
import com.duckduckgo.subscriptions.impl.feedback.SubscriptionFeedbackCategory.PIR
import com.duckduckgo.subscriptions.impl.feedback.SubscriptionFeedbackCategory.SUBS_AND_PAYMENTS
import com.duckduckgo.subscriptions.impl.feedback.SubscriptionFeedbackCategory.VPN

@InjectWith(FragmentScope::class)
class SubscriptionFeedbackCategoryFragment :
    SubscriptionFeedbackFragment(R.layout.content_feedback_category) {
    private val binding: ContentFeedbackCategoryBinding by viewBinding()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val listener = activity as Listener

        binding.categorySubscription.setOnClickListener {
            listener.onUserClickedCategory(SUBS_AND_PAYMENTS)
        }
        binding.categoryVpn.setOnClickListener {
            listener.onUserClickedCategory(VPN)
        }
        binding.categoryItr.setOnClickListener {
            listener.onUserClickedCategory(ITR)
        }
        binding.categoryPir.setOnClickListener {
            listener.onUserClickedCategory(PIR)
        }
    }

    interface Listener {
        fun onUserClickedCategory(category: SubscriptionFeedbackCategory)
    }

    companion object {
        internal fun instance(): SubscriptionFeedbackCategoryFragment =
            SubscriptionFeedbackCategoryFragment()
    }
}
