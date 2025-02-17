

package com.duckduckgo.app.feedback.ui.negative.subreason

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.duckduckgo.anvil.annotations.InjectWith
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.browser.databinding.ContentFeedbackNegativeDisambiguationSubReasonBinding
import com.duckduckgo.app.feedback.ui.common.FeedbackFragment
import com.duckduckgo.app.feedback.ui.negative.FeedbackType.*
import com.duckduckgo.app.feedback.ui.negative.FeedbackType.MainReason.*
import com.duckduckgo.app.feedback.ui.negative.FeedbackTypeDisplay
import com.duckduckgo.app.feedback.ui.negative.FeedbackTypeDisplay.FeedbackTypeSubReasonDisplay
import com.duckduckgo.app.feedback.ui.negative.displayText
import com.duckduckgo.common.ui.viewbinding.viewBinding
import com.duckduckgo.di.scopes.FragmentScope
import timber.log.Timber

@InjectWith(FragmentScope::class)
class SubReasonNegativeFeedbackFragment : FeedbackFragment(R.layout.content_feedback_negative_disambiguation_sub_reason) {

    private lateinit var recyclerAdapter: SubReasonAdapter

    interface DisambiguationNegativeFeedbackListener {
        fun userSelectedSubReasonMissingBrowserFeatures(
            mainReason: MainReason,
            subReason: MissingBrowserFeaturesSubReasons,
        )

        fun userSelectedSubReasonSearchNotGoodEnough(
            mainReason: MainReason,
            subReason: SearchNotGoodEnoughSubReasons,
        )

        fun userSelectedSubReasonNeedMoreCustomization(
            mainReason: MainReason,
            subReason: CustomizationSubReasons,
        )

        fun userSelectedSubReasonAppIsSlowOrBuggy(
            mainReason: MainReason,
            subReason: PerformanceSubReasons,
        )
    }

    private val binding: ContentFeedbackNegativeDisambiguationSubReasonBinding by viewBinding()

    private val listener: DisambiguationNegativeFeedbackListener?
        get() = activity as DisambiguationNegativeFeedbackListener

    private lateinit var mainReason: MainReason

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recyclerAdapter = SubReasonAdapter(
            object : (FeedbackTypeSubReasonDisplay) -> Unit {
                override fun invoke(reason: FeedbackTypeSubReasonDisplay) {
                    when (reason.subReason) {
                        is MissingBrowserFeaturesSubReasons -> {
                            listener?.userSelectedSubReasonMissingBrowserFeatures(mainReason, reason.subReason)
                        }
                        is SearchNotGoodEnoughSubReasons -> {
                            listener?.userSelectedSubReasonSearchNotGoodEnough(mainReason, reason.subReason)
                        }
                        is CustomizationSubReasons -> {
                            listener?.userSelectedSubReasonNeedMoreCustomization(mainReason, reason.subReason)
                        }
                        is PerformanceSubReasons -> {
                            listener?.userSelectedSubReasonAppIsSlowOrBuggy(mainReason, reason.subReason)
                        }
                    }
                }
            },
        )

        activity?.let {
            binding.recyclerView.layoutManager = LinearLayoutManager(it)
            binding.recyclerView.adapter = recyclerAdapter

            arguments?.let { args ->

                mainReason = args.getSerializable(MAIN_REASON_EXTRA) as MainReason
                val display = mainReason.displayText()

                binding.title.text = getString(display!!.titleDisplayResId)
                binding.subtitle.text = getString(display.subtitleDisplayResId)

                val subReasons = getDisplayTextForReasonType(mainReason)
                Timber.i("There are ${subReasons.size} subReasons to show")
                recyclerAdapter.submitList(subReasons)
            }
        }
    }

    private fun getDisplayTextForReasonType(mainReason: MainReason): List<FeedbackTypeSubReasonDisplay> {
        return when (mainReason) {
            MISSING_BROWSING_FEATURES -> browserFeatureSubReasons()
            SEARCH_NOT_GOOD_ENOUGH -> searchNotGoodEnoughSubReasons()
            NOT_ENOUGH_CUSTOMIZATIONS -> moreCustomizationsSubReasons()
            APP_IS_SLOW_OR_BUGGY -> appSlowOrBuggySubReasons()
            else -> throw IllegalStateException("Not handled - $mainReason")
        }
    }

    private fun browserFeatureSubReasons(): List<FeedbackTypeSubReasonDisplay> {
        return MissingBrowserFeaturesSubReasons.values().mapNotNull {
            FeedbackTypeDisplay.subReasons[it]
        }
    }

    private fun searchNotGoodEnoughSubReasons(): List<FeedbackTypeSubReasonDisplay> {
        return SearchNotGoodEnoughSubReasons.values().mapNotNull {
            FeedbackTypeDisplay.subReasons[it]
        }
    }

    private fun moreCustomizationsSubReasons(): List<FeedbackTypeSubReasonDisplay> {
        return CustomizationSubReasons.values().mapNotNull {
            FeedbackTypeDisplay.subReasons[it]
        }
    }

    private fun appSlowOrBuggySubReasons(): List<FeedbackTypeSubReasonDisplay> {
        return PerformanceSubReasons.values().mapNotNull {
            FeedbackTypeDisplay.subReasons[it]
        }
    }

    override fun configureViewModelObservers() {}

    companion object {

        private const val MAIN_REASON_EXTRA = "MAIN_REASON_EXTRA"

        fun instance(mainReason: MainReason): SubReasonNegativeFeedbackFragment {
            val fragment = SubReasonNegativeFeedbackFragment()
            fragment.arguments = Bundle().also {
                it.putSerializable(MAIN_REASON_EXTRA, mainReason)
            }
            return fragment
        }
    }
}
