

package com.duckduckgo.app.feedback.ui.initial

import android.os.Bundle
import com.duckduckgo.anvil.annotations.InjectWith
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.browser.databinding.ContentFeedbackBinding
import com.duckduckgo.app.feedback.ui.common.FeedbackFragment
import com.duckduckgo.app.feedback.ui.initial.InitialFeedbackFragmentViewModel.Command.*
import com.duckduckgo.common.ui.DuckDuckGoTheme
import com.duckduckgo.common.ui.store.ThemingDataStore
import com.duckduckgo.common.ui.viewbinding.viewBinding
import com.duckduckgo.di.scopes.FragmentScope
import javax.inject.Inject

@InjectWith(FragmentScope::class)
class InitialFeedbackFragment : FeedbackFragment(R.layout.content_feedback) {

    interface InitialFeedbackListener {
        fun userSelectedPositiveFeedback()
        fun userSelectedNegativeFeedback()
        fun userCancelled()
    }

    @Inject
    lateinit var themingDataStore: ThemingDataStore

    private val binding: ContentFeedbackBinding by viewBinding()

    private val viewModel by bindViewModel<InitialFeedbackFragmentViewModel>()

    private val listener: InitialFeedbackListener?
        get() = activity as InitialFeedbackListener

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (themingDataStore.theme == DuckDuckGoTheme.LIGHT) {
            binding.positiveFeedbackButton.setImageResource(R.drawable.button_happy_light_theme)
            binding.negativeFeedbackButton.setImageResource(R.drawable.button_sad_light_theme)
        } else {
            binding.positiveFeedbackButton.setImageResource(R.drawable.button_happy_dark_theme)
            binding.negativeFeedbackButton.setImageResource(R.drawable.button_sad_dark_theme)
        }
    }

    override fun configureViewModelObservers() {
        viewModel.command.observe(this) {
            when (it) {
                PositiveFeedbackSelected -> listener?.userSelectedPositiveFeedback()
                NegativeFeedbackSelected -> listener?.userSelectedNegativeFeedback()
                UserCancelled -> listener?.userCancelled()
            }
        }
    }

    override fun configureListeners() {
        binding.positiveFeedbackButton.setOnClickListener { viewModel.onPositiveFeedback() }
        binding.negativeFeedbackButton.setOnClickListener { viewModel.onNegativeFeedback() }
    }

    companion object {
        fun instance(): InitialFeedbackFragment {
            return InitialFeedbackFragment()
        }
    }
}
