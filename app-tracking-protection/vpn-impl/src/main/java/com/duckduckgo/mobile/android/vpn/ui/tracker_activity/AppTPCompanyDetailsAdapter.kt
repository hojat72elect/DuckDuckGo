package com.duckduckgo.mobile.android.vpn.ui.tracker_activity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.duckduckgo.common.ui.view.Chip
import com.duckduckgo.common.ui.view.gone
import com.duckduckgo.common.ui.view.show
import com.duckduckgo.mobile.android.vpn.R
import com.duckduckgo.mobile.android.vpn.ui.util.TextDrawable
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("NoHardcodedCoroutineDispatcher")
class AppTPCompanyDetailsAdapter :
    RecyclerView.Adapter<AppTPCompanyDetailsAdapter.CompanyDetailsViewHolder>() {

    private val items = mutableListOf<AppTPCompanyTrackersViewModel.CompanyTrackingDetails>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CompanyDetailsViewHolder {
        return CompanyDetailsViewHolder.create(parent)
    }

    override fun onBindViewHolder(
        holder: CompanyDetailsViewHolder,
        position: Int,
    ) {
        val companyTrackingDetails = items[position]
        holder.bind(companyTrackingDetails) { expanded ->
            items.forEachIndexed { index, companyDetails ->
                companyDetails.takeIf { it.companyName == companyTrackingDetails.companyName }
                    ?.let {
                        items[index] = it.copy(expanded = expanded)
                    }
            }
        }
    }

    override fun getItemCount() = items.size

    suspend fun updateData(data: List<AppTPCompanyTrackersViewModel.CompanyTrackingDetails>) {
        val newData = mutableListOf<AppTPCompanyTrackersViewModel.CompanyTrackingDetails>()
        data.forEach { updateDataItem ->
            val existingItem = items.find { it.companyName == updateDataItem.companyName }
            val itemToAdd = if (existingItem != null) {
                existingItem.copy(trackingAttempts = updateDataItem.trackingAttempts)
            } else {
                updateDataItem
            }

            newData.add(itemToAdd)
        }
        val oldData = items
        val diffResult = withContext(Dispatchers.IO) {
            DiffCallback(oldData, newData).run { DiffUtil.calculateDiff(this) }
        }

        items.clear().also { items.addAll(newData) }

        diffResult.dispatchUpdatesTo(this@AppTPCompanyDetailsAdapter)
    }

    private class DiffCallback(
        private val old: List<AppTPCompanyTrackersViewModel.CompanyTrackingDetails>,
        private val new: List<AppTPCompanyTrackersViewModel.CompanyTrackingDetails>,
    ) :
        DiffUtil.Callback() {
        override fun getOldListSize() = old.size

        override fun getNewListSize() = new.size

        override fun areItemsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int,
        ): Boolean {
            return old[oldItemPosition].companyName == new[newItemPosition].companyName
        }

        override fun areContentsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int,
        ): Boolean {
            return old[oldItemPosition] == new[newItemPosition]
        }
    }

    class CompanyDetailsViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            const val TOP_SIGNALS = 5
            fun create(parent: ViewGroup): CompanyDetailsViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val view = inflater.inflate(R.layout.item_apptp_company_details, parent, false)
                return CompanyDetailsViewHolder(view)
            }
        }

        private var badgeImage: ImageView = view.findViewById(R.id.tracking_company_icon)
        private var companyName: TextView = view.findViewById(R.id.tracking_company_name)
        private var trackingAttempts: TextView = view.findViewById(R.id.tracking_company_attempts)
        private var showMore: TextView = view.findViewById(R.id.tracking_company_show_more)
        private var topSignalsLayout: LinearLayout =
            view.findViewById(R.id.tracking_company_top_signals)
        private var bottomSignalsLayout: LinearLayout =
            view.findViewById(R.id.tracking_company_bottom_signals)
        private var showLess: TextView = view.findViewById(R.id.tracking_company_show_less)

        fun bind(
            companyDetails: AppTPCompanyTrackersViewModel.CompanyTrackingDetails,
            onExpanded: (Boolean) -> Unit,
        ) {
            val badge = badgeIcon(view.context, companyDetails.companyName)
            if (badge == null) {
                badgeImage.setImageDrawable(
                    TextDrawable.builder()
                        .beginConfig()
                        .fontSize(50)
                        .endConfig()
                        .buildRound(companyDetails.companyName.take(1)),
                )
            } else {
                badgeImage.setImageResource(badge)
            }

            companyName.text = companyDetails.companyDisplayName
            trackingAttempts.text = view.context.resources.getQuantityString(
                R.plurals.atp_CompanyDetailsTrackingAttempts,
                companyDetails.trackingAttempts,
                companyDetails.trackingAttempts,
            )

            val inflater = LayoutInflater.from(view.context)
            topSignalsLayout.removeAllViews()
            bottomSignalsLayout.removeAllViews()

            val topSignals = companyDetails.trackingSignals.take(TOP_SIGNALS)
            val bottomSignals = companyDetails.trackingSignals.drop(TOP_SIGNALS)

            topSignals.forEach {
                val topSignal = inflater.inflate(
                    com.duckduckgo.mobile.android.R.layout.view_chip,
                    topSignalsLayout,
                    false
                ) as Chip
                topSignal.setChipText(it.signalDisplayName)
                topSignal.setChipIcon(it.signalIcon)
                topSignalsLayout.addView(topSignal)
            }

            if (bottomSignals.isNotEmpty()) {
                bottomSignals.forEach {
                    val bottomSignal = inflater.inflate(
                        com.duckduckgo.mobile.android.R.layout.view_chip,
                        bottomSignalsLayout,
                        false
                    ) as Chip
                    bottomSignal.setChipText(it.signalDisplayName)
                    bottomSignal.setChipIcon(it.signalIcon)
                    bottomSignalsLayout.addView(bottomSignal)
                }
                showMore.show()
            } else {
                showMore.gone()
            }

            showMore.text = view.context.getString(
                R.string.atp_CompanyDetailsTrackingShowMore,
                bottomSignals.size
            )
            showMore.setOnClickListener {
                if (!bottomSignalsLayout.isVisible) {
                    showMore()
                    onExpanded.invoke(true)
                }
            }
            showLess.setOnClickListener {
                showLess()
                onExpanded.invoke(false)
            }
            if (companyDetails.expanded) {
                showMore()
            } else {
                showLess()
            }
        }

        private fun showMore() {
            bottomSignalsLayout.show()
            showMore.gone()
            showLess.show()
        }

        private fun showLess() {
            bottomSignalsLayout.gone()
            showMore.show()
            showLess.gone()
        }

        private fun badgeIcon(
            context: Context,
            networkName: String,
            prefix: String = "tracking_network_logo_",
        ): Int? {
            val drawable = "$prefix$networkName"
                .replace(" ", "_")
                .replace(".", "")
                .replace(",", "")
                .lowercase(Locale.ROOT)
            val resource =
                context.resources.getIdentifier(drawable, "drawable", context.packageName)
            return if (resource != 0) resource else null
        }
    }
}
