

package com.duckduckgo.app.fire.fireproofwebsite.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.app.browser.favicon.FaviconManager
import com.duckduckgo.app.fire.fireproofwebsite.data.FireproofWebsiteDao
import com.duckduckgo.app.fire.fireproofwebsite.data.FireproofWebsiteEntity
import com.duckduckgo.app.fire.fireproofwebsite.data.FireproofWebsiteRepositoryImpl
import com.duckduckgo.app.fire.fireproofwebsite.ui.FireproofWebsitesViewModel.Command.ConfirmRemoveFireproofWebsite
import com.duckduckgo.app.global.db.AppDatabase
import com.duckduckgo.app.global.events.db.UserEventKey
import com.duckduckgo.app.global.events.db.UserEventsStore
import com.duckduckgo.app.pixels.AppPixelName
import com.duckduckgo.app.pixels.AppPixelName.FIREPROOF_SETTING_SELECTION_ALWAYS
import com.duckduckgo.app.settings.db.SettingsDataStore
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.common.test.InstantSchedulersRule
import dagger.Lazy
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@Suppress("EXPERIMENTAL_API_USAGE")
class FireproofWebsitesViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val schedulers = InstantSchedulersRule()

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private lateinit var fireproofWebsiteDao: FireproofWebsiteDao

    private lateinit var viewModel: FireproofWebsitesViewModel

    private lateinit var db: AppDatabase

    private val commandCaptor = argumentCaptor<FireproofWebsitesViewModel.Command>()

    private val viewStateCaptor = argumentCaptor<FireproofWebsitesViewModel.ViewState>()

    private val mockCommandObserver: Observer<FireproofWebsitesViewModel.Command> = mock()

    private val mockViewStateObserver: Observer<FireproofWebsitesViewModel.ViewState> = mock()

    private val mockPixel: Pixel = mock()

    private val mockSettingsDataStore: SettingsDataStore = mock {
        on { it.automaticFireproofSetting } doReturn AutomaticFireproofSetting.ASK_EVERY_TIME
    }

    private val mockFaviconManager: FaviconManager = mock()

    private val mockUserEventsStore: UserEventsStore = mock()

    private val lazyFaviconManager = Lazy { mockFaviconManager }

    @Before
    fun before() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        fireproofWebsiteDao = db.fireproofWebsiteDao()
        viewModel = FireproofWebsitesViewModel(
            FireproofWebsiteRepositoryImpl(fireproofWebsiteDao, coroutineRule.testDispatcherProvider, lazyFaviconManager),
            coroutineRule.testDispatcherProvider,
            mockPixel,
            mockSettingsDataStore,
            mockUserEventsStore,
        )
        viewModel.command.observeForever(mockCommandObserver)
        viewModel.viewState.observeForever(mockViewStateObserver)
    }

    @After
    fun after() {
        db.close()
        viewModel.command.removeObserver(mockCommandObserver)
        viewModel.viewState.removeObserver(mockViewStateObserver)
    }

    @Test
    fun whenViewModelCreateThenInitialisedWithDefaultViewState() {
        val defaultViewState = FireproofWebsitesViewModel.ViewState(AutomaticFireproofSetting.ASK_EVERY_TIME, emptyList())
        verify(mockViewStateObserver, atLeastOnce()).onChanged(viewStateCaptor.capture())
        assertEquals(defaultViewState, viewStateCaptor.lastValue)
    }

    @Test
    fun whenUserDeletesFireProofWebsiteThenConfirmDeleteCommandIssued() {
        val fireproofWebsiteEntity = FireproofWebsiteEntity("domain.com")
        viewModel.onDeleteRequested(fireproofWebsiteEntity)

        assertCommandIssued<ConfirmRemoveFireproofWebsite> {
            assertEquals(fireproofWebsiteEntity, this.entity)
        }
    }

    @Test
    fun whenUserConfirmsToDeleteThenEntityRemovedAndViewStateUpdated() {
        givenFireproofWebsiteDomain("domain.com")

        viewModel.remove(FireproofWebsiteEntity("domain.com"))

        verify(mockViewStateObserver, atLeastOnce()).onChanged(viewStateCaptor.capture())
        assertTrue(viewStateCaptor.lastValue.fireproofWebsitesEntities.isEmpty())
    }

    @Test
    fun whenUserConfirmsToRemoveAllThenEntitiesRemovedAndViewStateUpdated() {
        fireproofWebsiteDao.insert(FireproofWebsiteEntity("domain.com"))
        fireproofWebsiteDao.insert(FireproofWebsiteEntity("domain2.com"))

        viewModel.removeAllWebsites()

        verify(mockViewStateObserver, atLeastOnce()).onChanged(viewStateCaptor.capture())
        assertTrue(viewStateCaptor.lastValue.fireproofWebsitesEntities.isEmpty())
    }

    @Test
    fun whenUserConfirmsToDeleteThenPixelSent() {
        givenFireproofWebsiteDomain("domain.com")

        viewModel.remove(FireproofWebsiteEntity("domain.com"))

        verify(mockPixel).fire(AppPixelName.FIREPROOF_WEBSITE_DELETED)
    }

    @Test
    fun whenViewModelInitialisedThenViewStateShowsCurrentFireproofWebsites() {
        givenFireproofWebsiteDomain("domain.com")

        verify(mockViewStateObserver, atLeastOnce()).onChanged(viewStateCaptor.capture())
        assertTrue(viewStateCaptor.lastValue.fireproofWebsitesEntities.size == 1)
    }

    @Test
    fun whenUserChangesAutomaticFireproofSettingThenFirePixel() {
        viewModel.onAutomaticFireproofSettingChanged(AutomaticFireproofSetting.ALWAYS)

        verify(mockPixel).fire(FIREPROOF_SETTING_SELECTION_ALWAYS)
    }

    @Test
    fun whenUserChangesAutomaticFireproofSettingThenUpdateViewState() {
        viewModel.onAutomaticFireproofSettingChanged(AutomaticFireproofSetting.ALWAYS)

        verify(mockViewStateObserver, atLeastOnce()).onChanged(viewStateCaptor.capture())
        assertTrue(viewStateCaptor.lastValue.automaticFireproofSetting == AutomaticFireproofSetting.ALWAYS)
    }

    @Test
    fun whenUserEnablesAutomaticFireproofSettingThenRegisterEvent() = runTest {
        viewModel.onAutomaticFireproofSettingChanged(AutomaticFireproofSetting.ALWAYS)

        verify(mockUserEventsStore).registerUserEvent(UserEventKey.USER_ENABLED_FIREPROOF_LOGIN)
    }

    @Test
    fun whenUserChangesAutomaticFireproofSettingThenUpdateSettingsDataStore() {
        viewModel.onAutomaticFireproofSettingChanged(AutomaticFireproofSetting.ALWAYS)

        verify(mockSettingsDataStore).automaticFireproofSetting = AutomaticFireproofSetting.ALWAYS
    }

    @Test
    fun whenUserUndosDeleteFireproofThenSiteIsAddedBack() {
        val entity = FireproofWebsiteEntity("domain.com")

        viewModel.onSnackBarUndoFireproof(entity)

        verify(mockViewStateObserver, atLeastOnce()).onChanged(viewStateCaptor.capture())
        assertTrue(viewStateCaptor.lastValue.fireproofWebsitesEntities.isNotEmpty())
    }

    @Test
    fun whenUserUndoesRemoveAllFireproofSitesThenSitesAreAddedBack() {
        val removedWebsites: List<FireproofWebsiteEntity> = listOf(
            FireproofWebsiteEntity(domain = "domain.com"),
            FireproofWebsiteEntity(domain = "domain2.com"),
        )
        viewModel.onSnackBarUndoRemoveAllWebsites(removedWebsites)

        verify(mockViewStateObserver, atLeastOnce()).onChanged(viewStateCaptor.capture())
        assertTrue(viewStateCaptor.lastValue.fireproofWebsitesEntities.isNotEmpty())
    }

    private inline fun <reified T : FireproofWebsitesViewModel.Command> assertCommandIssued(instanceAssertions: T.() -> Unit = {}) {
        verify(mockCommandObserver, atLeastOnce()).onChanged(commandCaptor.capture())
        val issuedCommand = commandCaptor.allValues.find { it is T }
        assertNotNull(issuedCommand)
        (issuedCommand as T).apply { instanceAssertions() }
    }

    private fun givenFireproofWebsiteDomain(vararg fireproofWebsitesDomain: String) {
        fireproofWebsitesDomain.forEach {
            fireproofWebsiteDao.insert(FireproofWebsiteEntity(domain = it))
        }
    }
}
