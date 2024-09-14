

package com.duckduckgo.mobile.android.vpn.breakage

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.mobile.android.vpn.ui.OpenVpnBreakageCategoryWithBrokenApp
import com.duckduckgo.mobile.android.vpn.ui.OpenVpnReportBreakageFrom
import com.duckduckgo.navigation.api.GlobalActivityStarter
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class ReportBreakageContractTest {

    private val globalActivityStarter: GlobalActivityStarter = mock()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun whenCreateIntentForIssueDescriptionFormThenReturnCorrectIntent() {
        whenever(globalActivityStarter.startIntent(any(), any<OpenVpnBreakageCategoryWithBrokenApp>()))
            .thenReturn(Intent(context, ReportBreakageCategorySingleChoiceActivity::class.java))

        val intent = ReportBreakageContract(globalActivityStarter)
            .createIntent(context, ReportBreakageScreen.IssueDescriptionForm(ORIGIN, emptyList(), "myApp", "my.package.com"))

        assertEquals(ReportBreakageCategorySingleChoiceActivity::class.java.canonicalName, intent.component?.className)
    }

    @Test
    fun whenCreateIntentForListOfInstalledAppsThenReturnCorrectIntent() {
        whenever(globalActivityStarter.startIntent(context, OpenVpnReportBreakageFrom(ORIGIN, emptyList())))
            .thenReturn(Intent(context, ReportBreakageAppListActivity::class.java))

        val intent = ReportBreakageContract(globalActivityStarter).createIntent(
            context,
            ReportBreakageScreen.ListOfInstalledApps(ORIGIN, emptyList()),
        )

        assertEquals(ReportBreakageAppListActivity::class.java.canonicalName, intent.component?.className)
    }
}

private const val ORIGIN = "origin"
