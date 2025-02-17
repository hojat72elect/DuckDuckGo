package com.duckduckgo.savedsites.impl.newtab

import android.annotation.SuppressLint
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duckduckgo.anvil.annotations.ContributesViewModel
import com.duckduckgo.app.browser.favicon.FaviconManager
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.app.statistics.pixels.Pixel.PixelType.DAILY
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.ViewScope
import com.duckduckgo.savedsites.api.SavedSitesRepository
import com.duckduckgo.savedsites.api.models.BookmarkFolder
import com.duckduckgo.savedsites.api.models.SavedSite
import com.duckduckgo.savedsites.api.models.SavedSite.Bookmark
import com.duckduckgo.savedsites.api.models.SavedSite.Favorite
import com.duckduckgo.savedsites.impl.SavedSitesPixelName
import com.duckduckgo.savedsites.impl.newtab.FavouritesNewTabSectionViewModel.Command.DeleteFavoriteConfirmation
import com.duckduckgo.savedsites.impl.newtab.FavouritesNewTabSectionViewModel.Command.DeleteSavedSiteConfirmation
import com.duckduckgo.savedsites.impl.newtab.FavouritesNewTabSectionViewModel.Command.ShowEditSavedSiteDialog
import com.duckduckgo.sync.api.engine.SyncEngine
import com.duckduckgo.sync.api.engine.SyncEngine.SyncTrigger.FEATURE_READ
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("NoLifecycleObserver") // we don't observe app lifecycle
@ContributesViewModel(ViewScope::class)
class FavouritesNewTabSectionViewModel @Inject constructor(
    private val dispatchers: DispatcherProvider,
    private val savedSitesRepository: SavedSitesRepository,
    private val pixel: Pixel,
    private val faviconManager: FaviconManager,
    private val syncEngine: SyncEngine,
) : ViewModel(), DefaultLifecycleObserver {

    data class ViewState(val favourites: List<Favorite> = emptyList())

    data class SavedSiteChangedViewState(
        val savedSite: SavedSite,
        val bookmarkFolder: BookmarkFolder?,
    )

    sealed class Command {
        class ShowEditSavedSiteDialog(val savedSiteChangedViewState: SavedSiteChangedViewState) :
            Command()

        class DeleteFavoriteConfirmation(val savedSite: SavedSite) : Command()
        class DeleteSavedSiteConfirmation(val savedSite: SavedSite) : Command()
    }

    data class HiddenBookmarksIds(
        val favorites: List<String> = emptyList(),
        val bookmarks: List<String> = emptyList(),
    )

    val hiddenIds = MutableStateFlow(HiddenBookmarksIds())

    private val _viewState = MutableStateFlow(ViewState())
    val viewState = _viewState.asStateFlow()
    private val command = Channel<Command>(1, BufferOverflow.DROP_OLDEST)
    internal fun commands(): Flow<Command> = command.receiveAsFlow()

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        viewModelScope.launch(dispatchers.io()) {
            savedSitesRepository.getFavorites()
                .combine(hiddenIds) { favorites, hiddenIds ->
                    favorites.filter { it.id !in hiddenIds.favorites }
                }
                .flowOn(dispatchers.io())
                .onEach { favourites ->
                    withContext(dispatchers.main()) {
                        _viewState.emit(
                            viewState.value.copy(
                                favourites = favourites,
                            ),
                        )
                    }
                }
                .flowOn(dispatchers.main())
                .launchIn(viewModelScope)
        }
    }

    fun onQuickAccessListChanged(newList: List<Favorite>) {
        viewModelScope.launch(dispatchers.io()) {
            val favourites = savedSitesRepository.getFavoritesSync()
            if (favourites.size == newList.size) {
                savedSitesRepository.updateWithPosition(newList.map { it })
            } else {
                val updatedList = newList.plus(favourites.takeLast(favourites.size - newList.size))
                savedSitesRepository.updateWithPosition(updatedList.map { it })
            }
        }
    }

    fun onEditSavedSiteRequested(savedSite: SavedSite) {
        viewModelScope.launch(dispatchers.io()) {
            val bookmarkFolder =
                if (savedSite is SavedSite.Bookmark) {
                    getBookmarkFolder(savedSite)
                } else {
                    null
                }

            withContext(dispatchers.main()) {
                command.send(
                    ShowEditSavedSiteDialog(
                        SavedSiteChangedViewState(
                            savedSite,
                            bookmarkFolder,
                        ),
                    ),
                )
            }
        }
    }

    private suspend fun getBookmarkFolder(bookmark: SavedSite.Bookmark?): BookmarkFolder? {
        if (bookmark == null) return null
        return withContext(dispatchers.io()) {
            savedSitesRepository.getFolder(bookmark.parentId)
        }
    }

    fun onDeleteFavoriteRequested(savedSite: SavedSite) {
        hide(savedSite, DeleteFavoriteConfirmation(savedSite))
    }

    private fun hide(
        savedSite: SavedSite,
        deleteCommand: Command,
    ) {
        viewModelScope.launch(dispatchers.io()) {
            when (savedSite) {
                is Bookmark -> {
                    hiddenIds.emit(
                        hiddenIds.value.copy(
                            bookmarks = hiddenIds.value.bookmarks + savedSite.id,
                            favorites = hiddenIds.value.favorites + savedSite.id,
                        ),
                    )
                }

                is Favorite -> {
                    hiddenIds.emit(hiddenIds.value.copy(favorites = hiddenIds.value.favorites + savedSite.id))
                }
            }
            withContext(dispatchers.main()) {
                command.send(deleteCommand)
            }
        }
    }

    fun onDeleteSavedSiteRequested(savedSite: SavedSite) {
        hide(savedSite, DeleteSavedSiteConfirmation(savedSite))
    }

    fun undoDelete(savedSite: SavedSite) {
        viewModelScope.launch(dispatchers.io()) {
            hiddenIds.emit(
                hiddenIds.value.copy(
                    favorites = hiddenIds.value.favorites - savedSite.id,
                    bookmarks = hiddenIds.value.bookmarks - savedSite.id,
                ),
            )
        }
    }

    fun onDeleteFavoriteSnackbarDismissed(savedSite: SavedSite) {
        delete(savedSite)
    }

    fun onDeleteSavedSiteSnackbarDismissed(savedSite: SavedSite) {
        delete(savedSite, true)
    }

    private fun delete(
        savedSite: SavedSite,
        deleteBookmark: Boolean = false,
    ) {
        viewModelScope.launch(dispatchers.io()) {
            if (savedSite is Bookmark || deleteBookmark) {
                faviconManager.deletePersistedFavicon(savedSite.url)
            }
            savedSitesRepository.delete(savedSite, deleteBookmark)
        }
    }

    fun onNewTabFavouritesShown() {
        viewModelScope.launch(dispatchers.io()) {
            syncEngine.triggerSync(FEATURE_READ)
        }
    }

    fun onTooltipPressed() {
        pixel.fire(SavedSitesPixelName.FAVOURITES_TOOLTIP_PRESSED)
    }

    fun onListExpanded() {
        pixel.fire(SavedSitesPixelName.FAVOURITES_LIST_EXPANDED)
    }

    fun onListCollapsed() {
        pixel.fire(SavedSitesPixelName.FAVOURITES_LIST_COLLAPSED)
    }

    fun onFavouriteEdited(favorite: Favorite) {
        viewModelScope.launch(dispatchers.io()) {
            savedSitesRepository.updateFavourite(favorite)
        }
    }

    fun onBookmarkEdited(
        bookmark: Bookmark,
        oldFolderId: String,
        updateFavorite: Boolean,
    ) {
        viewModelScope.launch(dispatchers.io()) {
            savedSitesRepository.updateBookmark(bookmark, oldFolderId, updateFavorite)
        }
    }

    fun onSavedSiteDeleted(savedSite: SavedSite) {
        onDeleteSavedSiteRequested(savedSite)
    }

    fun onFavoriteAdded() {
        pixel.fire(SavedSitesPixelName.EDIT_BOOKMARK_ADD_FAVORITE_TOGGLED)
        pixel.fire(SavedSitesPixelName.EDIT_BOOKMARK_ADD_FAVORITE_TOGGLED_DAILY, type = DAILY)
    }

    fun onFavoriteRemoved() {
        pixel.fire(SavedSitesPixelName.EDIT_BOOKMARK_REMOVE_FAVORITE_TOGGLED)
    }

    fun onFavoriteClicked() {
        pixel.fire(SavedSitesPixelName.FAVOURITE_CLICKED)
        pixel.fire(SavedSitesPixelName.FAVOURITE_CLICKED_DAILY, type = DAILY)
    }
}
