

package com.duckduckgo.app.onboarding.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.duckduckgo.anvil.annotations.InjectWith
import com.duckduckgo.app.browser.BrowserActivity
import com.duckduckgo.app.browser.databinding.ActivityOnboardingBinding
import com.duckduckgo.common.ui.DuckDuckGoActivity
import com.duckduckgo.common.ui.viewbinding.viewBinding
import com.duckduckgo.di.scopes.ActivityScope

@InjectWith(ActivityScope::class)
class OnboardingActivity : DuckDuckGoActivity() {

    private lateinit var viewPageAdapter: PagerAdapter

    private val viewModel: OnboardingViewModel by bindViewModel()

    private val binding: ActivityOnboardingBinding by viewBinding()

    private val viewPager
        get() = binding.viewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        configurePager()
    }

    fun onContinueClicked() {
        val next = viewPager.currentItem + 1
        if (next < viewPager.adapter!!.count) {
            viewPager.setCurrentItem(next, true)
        } else {
            onOnboardingDone()
        }
    }

    private fun onOnboardingDone() {
        viewModel.onOnboardingDone()
        startActivity(BrowserActivity.intent(this@OnboardingActivity))
        finish()
    }

    private fun configurePager() {
        viewModel.initializePages()

        viewPageAdapter = PagerAdapter(supportFragmentManager, viewModel)
        viewPager.offscreenPageLimit = 1
        viewPager.adapter = viewPageAdapter
        viewPager.setSwipingEnabled(false)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val currentPage = viewPager.currentItem
        if (currentPage == 0) {
            finish()
        } else {
            viewPager.setCurrentItem(currentPage - 1, true)
        }
    }

    companion object {

        fun intent(context: Context): Intent {
            return Intent(context, OnboardingActivity::class.java)
        }
    }
}
