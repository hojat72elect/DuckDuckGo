package com.duckduckgo.subscriptions.internal.settings

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.duckduckgo.anvil.annotations.InjectWith
import com.duckduckgo.app.di.AppCoroutineScope
import com.duckduckgo.common.ui.viewbinding.viewBinding
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.di.scopes.ViewScope
import com.duckduckgo.subscriptions.impl.SubscriptionsManager
import com.duckduckgo.subscriptions.internal.SubsSettingPlugin
import com.duckduckgo.subscriptions.internal.databinding.SubsSimpleViewBinding
import com.squareup.anvil.annotations.ContributesMultibinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@InjectWith(ViewScope::class)
class DeleteSubscriptionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {

    @Inject
    lateinit var dispatcherProvider: DispatcherProvider

    @Inject
    @AppCoroutineScope
    lateinit var appCoroutineScope: CoroutineScope

    @Inject
    lateinit var subscriptionsManager: SubscriptionsManager

    private val binding: SubsSimpleViewBinding by viewBinding()

    override fun onAttachedToWindow() {
        AndroidSupportInjection.inject(this)
        super.onAttachedToWindow()

        binding.root.setPrimaryText("Delete Subscription Account")
        binding.root.setSecondaryText("Deletes your subscription account")

        binding.root.setClickListener {
            deleteSubscription()
        }
    }

    private fun deleteSubscription() {
        appCoroutineScope.launch(dispatcherProvider.io()) {
            val message = if (subscriptionsManager.deleteAccount()) {
                subscriptionsManager.signOut()
                "Account deleted"
            } else {
                "We could not delete your account"
            }
            withContext(dispatcherProvider.main()) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@ContributesMultibinding(ActivityScope::class)
class DeleteSubscriptionViewPlugin @Inject constructor() : SubsSettingPlugin {
    override fun getView(context: Context): View {
        return DeleteSubscriptionView(context)
    }
}
