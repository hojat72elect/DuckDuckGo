

package com.duckduckgo.app

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import com.duckduckgo.anvil.annotations.InjectWith
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.browser.databinding.ActivityWidgetConfigurationBinding
import com.duckduckgo.app.pixels.AppPixelName
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.common.ui.DuckDuckGoActivity
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.widget.SearchAndFavoritesWidget
import com.duckduckgo.widget.WidgetPreferences
import com.duckduckgo.widget.WidgetTheme
import javax.inject.Inject

@InjectWith(ActivityScope::class)
class WidgetThemeConfiguration : DuckDuckGoActivity() {

    @Inject
    lateinit var widgetPrefs: WidgetPreferences

    @Inject
    lateinit var pixel: Pixel

    @Inject
    lateinit var appBuildConfig: AppBuildConfig

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    @Suppress("NewApi") // we use appBuildConfig
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityWidgetConfigurationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val extras = intent.extras
        extras?.let {
            appWidgetId = it.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_CANCELED, resultValue)

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }

        if (appBuildConfig.sdkInt >= Build.VERSION_CODES.Q) {
            binding.widgetConfigThemeSystem.visibility = View.VISIBLE
            binding.widgetConfigThemeSystem.isChecked = true
        } else {
            binding.widgetConfigThemeSystem.visibility = View.GONE
            val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                binding.widgetConfigThemeDark.isChecked = true
            } else {
                binding.widgetConfigThemeLight.isChecked = true
            }
        }

        binding.widgetConfigThemeRadioGroup.setOnCheckedChangeListener { _, radioId ->
            when (radioId) {
                R.id.widgetConfigThemeSystem -> {
                    val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                        binding.widgetConfigPreview.setImageResource(R.drawable.search_favorites_widget_dark_preview)
                    } else {
                        binding.widgetConfigPreview.setImageResource(R.drawable.search_favorites_widget_light_preview)
                    }
                }
                R.id.widgetConfigThemeLight -> {
                    binding.widgetConfigPreview.setImageResource(R.drawable.search_favorites_widget_light_preview)
                }
                R.id.widgetConfigThemeDark -> {
                    binding.widgetConfigPreview.setImageResource(R.drawable.search_favorites_widget_dark_preview)
                }
            }
        }

        binding.widgetConfigAddWidgetButton.setOnClickListener {
            val selectedTheme = when (binding.widgetConfigThemeRadioGroup.checkedRadioButtonId) {
                R.id.widgetConfigThemeLight -> {
                    WidgetTheme.LIGHT
                }
                R.id.widgetConfigThemeDark -> {
                    WidgetTheme.DARK
                }
                R.id.widgetConfigThemeSystem -> {
                    WidgetTheme.SYSTEM_DEFAULT
                }
                else -> throw IllegalArgumentException("Unknown Radio button Id")
            }
            storeAndSubmitConfiguration(appWidgetId, selectedTheme)
        }

        pixel.fire(AppPixelName.FAVORITE_WIDGET_CONFIGURATION_SHOWN)
    }

    private fun storeAndSubmitConfiguration(
        widgetId: Int,
        selectedTheme: WidgetTheme,
    ) {
        widgetPrefs.saveWidgetSelectedTheme(widgetId, selectedTheme.toString())
        pixelSelectedTheme(selectedTheme)

        val widgetUpdateIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
            component = ComponentName(this@WidgetThemeConfiguration, SearchAndFavoritesWidget::class.java)
        }
        val widgetsToUpdate = IntArray(1).also { it[0] = widgetId }
        widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetsToUpdate)
        sendBroadcast(widgetUpdateIntent)

        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    private fun pixelSelectedTheme(selectedTheme: WidgetTheme) {
        when (selectedTheme) {
            WidgetTheme.LIGHT -> pixel.fire(AppPixelName.FAVORITES_WIDGETS_LIGHT)
            WidgetTheme.DARK -> pixel.fire(AppPixelName.FAVORITES_WIDGETS_DARK)
            WidgetTheme.SYSTEM_DEFAULT -> pixel.fire(AppPixelName.FAVORITES_WIDGETS_SYSTEM)
        }
    }
}
