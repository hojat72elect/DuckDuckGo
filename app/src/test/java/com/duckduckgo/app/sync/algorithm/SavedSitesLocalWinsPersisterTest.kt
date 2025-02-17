

package com.duckduckgo.app.sync.algorithm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.app.bookmarks.BookmarkTestUtils
import com.duckduckgo.app.global.db.AppDatabase
import com.duckduckgo.app.sync.FakeDisplayModeSettingsRepository
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.common.utils.formatters.time.DatabaseDateFormatter
import com.duckduckgo.savedsites.api.SavedSitesRepository
import com.duckduckgo.savedsites.api.models.BookmarkFolder
import com.duckduckgo.savedsites.api.models.SavedSite.Bookmark
import com.duckduckgo.savedsites.api.models.SavedSitesNames
import com.duckduckgo.savedsites.impl.MissingEntitiesRelationReconciler
import com.duckduckgo.savedsites.impl.RealFavoritesDelegate
import com.duckduckgo.savedsites.impl.RealSavedSitesRepository
import com.duckduckgo.savedsites.impl.sync.RealSyncSavedSitesRepository
import com.duckduckgo.savedsites.impl.sync.SyncSavedSitesRepository
import com.duckduckgo.savedsites.impl.sync.algorithm.SavedSitesLocalWinsPersister
import com.duckduckgo.savedsites.impl.sync.store.RealSavedSitesSyncEntitiesStore
import com.duckduckgo.savedsites.impl.sync.store.SavedSitesSyncMetadataDao
import com.duckduckgo.savedsites.impl.sync.store.SavedSitesSyncMetadataDatabase
import com.duckduckgo.savedsites.store.SavedSitesEntitiesDao
import com.duckduckgo.savedsites.store.SavedSitesRelationsDao
import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SavedSitesLocalWinsPersisterTest {

    @get:Rule
    @Suppress("unused")
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private lateinit var db: AppDatabase
    private lateinit var savedSitesDatabase: SavedSitesSyncMetadataDatabase
    private lateinit var repository: SavedSitesRepository
    private lateinit var syncRepository: SyncSavedSitesRepository
    private lateinit var savedSitesEntitiesDao: SavedSitesEntitiesDao
    private lateinit var savedSitesRelationsDao: SavedSitesRelationsDao
    private lateinit var savedSitesMetadataDao: SavedSitesSyncMetadataDao
    private lateinit var persister: SavedSitesLocalWinsPersister
    private val store = RealSavedSitesSyncEntitiesStore(
        InstrumentationRegistry.getInstrumentation().context,
    )

    private val twoHoursAgo = DatabaseDateFormatter.iso8601(OffsetDateTime.now(ZoneOffset.UTC).minusHours(2))

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        savedSitesDatabase = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            SavedSitesSyncMetadataDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()
        savedSitesMetadataDao = savedSitesDatabase.syncMetadataDao()

        savedSitesEntitiesDao = db.syncEntitiesDao()
        savedSitesRelationsDao = db.syncRelationsDao()
        savedSitesMetadataDao = savedSitesDatabase.syncMetadataDao()

        val favoritesDelegate = RealFavoritesDelegate(
            savedSitesEntitiesDao,
            savedSitesRelationsDao,
            FakeDisplayModeSettingsRepository(),
            MissingEntitiesRelationReconciler(savedSitesEntitiesDao),
            coroutinesTestRule.testDispatcherProvider,
        )
        syncRepository = RealSyncSavedSitesRepository(savedSitesEntitiesDao, savedSitesRelationsDao, savedSitesMetadataDao, store)
        repository = RealSavedSitesRepository(
            savedSitesEntitiesDao,
            savedSitesRelationsDao,
            favoritesDelegate,
            MissingEntitiesRelationReconciler(savedSitesEntitiesDao),
            coroutinesTestRule.testDispatcherProvider,
        )
        persister = SavedSitesLocalWinsPersister(repository, syncRepository)
    }

    @Test
    fun whenProcessingBookmarkNotPresentLocallyThenBookmarkIsInserted() {
        val bookmark = Bookmark("bookmark1", "title", "www.example.com", "folder2", twoHoursAgo)
        assertTrue(repository.getBookmarkById(bookmark.id) == null)

        persister.processBookmark(bookmark, SavedSitesNames.BOOKMARKS_ROOT)

        assertTrue(repository.getBookmarkById(bookmark.id) != null)
    }

    @Test
    fun whenProcessingDeletedBookmarkNotPresentLocallyThenBookmarkIsNotInserted() {
        val bookmark = Bookmark("bookmark1", "title", "www.example.com", "folder2", twoHoursAgo, deleted = "1")
        assertTrue(repository.getBookmarkById(bookmark.id) == null)

        persister.processBookmark(bookmark, SavedSitesNames.BOOKMARKS_ROOT)

        assertTrue(repository.getBookmarkById(bookmark.id) == null)
    }

    @Test
    fun whenProcessingRemoteBookmarkPresentLocallyThenBookmarkIsNotReplaced() {
        val bookmark = Bookmark("bookmark1", "title", "www.example.com", "folder2", twoHoursAgo)
        repository.insert(bookmark)

        assertTrue(repository.getBookmarkById(bookmark.id) != null)

        val remoteBookmark = bookmark.copy(title = "title replaced")
        persister.processBookmark(remoteBookmark, SavedSitesNames.BOOKMARKS_ROOT)

        val storedBookmark = repository.getBookmarkById(bookmark.id)
        assertTrue(storedBookmark != null)
        assertTrue(storedBookmark!!.title == bookmark.title)
    }

    @Test
    fun whenProcessingDeletedRemoteBookmarkThenBookmarkIsDeleted() {
        val bookmark = Bookmark("bookmark1", "title", "www.example.com", "folder2", twoHoursAgo)
        repository.insert(bookmark)

        assertTrue(repository.getBookmarkById(bookmark.id) != null)

        val remoteBookmark = bookmark.copy(deleted = "1")
        persister.processBookmark(remoteBookmark, SavedSitesNames.BOOKMARKS_ROOT)

        val replacedBookmark = repository.getBookmarkById(remoteBookmark.id)
        assertTrue(replacedBookmark == null)
    }

    @Test
    fun whenProcessingEmptyFavouriteFoldersThenFavouritesAreAdded() {
        // given some bookmarks
        val firstBatch = BookmarkTestUtils.givenSomeBookmarks(10)
        savedSitesEntitiesDao.insertList(firstBatch)
        savedSitesRelationsDao.insertList(BookmarkTestUtils.givenFolderWithContent(BookmarkTestUtils.bookmarksRoot.entityId, firstBatch))

        // when processing the favourites folder
        persister.processFavouritesFolder(BookmarkTestUtils.favouritesRoot.entityId, firstBatch.map { it.entityId })

        // then all favourites have been added
        val favourites = repository.getFavoritesSync()
        assertTrue(favourites.map { it.id } == firstBatch.map { it.entityId })
    }

    @Test
    fun whenProcessingNotEmptyFavouriteFoldersThenFavouritesAreReplaced() {
        // given some favourites
        val firstBatch = BookmarkTestUtils.givenSomeBookmarks(10)
        savedSitesEntitiesDao.insertList(firstBatch)
        savedSitesRelationsDao.insertList(BookmarkTestUtils.givenFolderWithContent(BookmarkTestUtils.bookmarksRoot.entityId, firstBatch))
        savedSitesRelationsDao.insertList(BookmarkTestUtils.givenFolderWithContent(BookmarkTestUtils.favouritesRoot.entityId, firstBatch))

        // and new bookmarks have been added
        val secondBatch = BookmarkTestUtils.givenSomeBookmarks(10)
        savedSitesEntitiesDao.insertList(secondBatch)
        savedSitesRelationsDao.insertList(BookmarkTestUtils.givenFolderWithContent(BookmarkTestUtils.bookmarksRoot.entityId, secondBatch))

        // when processing the favourites folder
        persister.processFavouritesFolder(BookmarkTestUtils.favouritesRoot.entityId, secondBatch.map { it.entityId })

        // then all favourites have been replaced
        val favourites = repository.getFavoritesSync()
        assertTrue(favourites.map { it.id } == secondBatch.map { it.entityId })
    }

    @Test
    fun whenProcessingFolderNotPresentLocallyThenFolderIsInserted() {
        val folder = BookmarkFolder("folder1", "title", SavedSitesNames.BOOKMARKS_ROOT, 0, 0)
        assertTrue(repository.getFolder(folder.id) == null)

        persister.processBookmarkFolder(folder, emptyList())

        assertTrue(repository.getFolder(folder.id) != null)
    }

    @Test
    fun whenProcessingDeletedFolderPresentLocallyThenFolderIsDeleted() {
        val folder = BookmarkFolder("folder1", "title", SavedSitesNames.BOOKMARKS_ROOT, 0, 0)
        repository.insert(folder)
        assertTrue(repository.getFolder(folder.id) != null)

        val deletedFolder = folder.copy(deleted = "1")
        persister.processBookmarkFolder(deletedFolder, emptyList())

        assertTrue(repository.getFolder(folder.id) == null)
    }

    @Test
    fun whenProcessingFolderThenFolderIsNotReplaced() {
        val folder = BookmarkFolder("folder1", "title", SavedSitesNames.BOOKMARKS_ROOT, 0, 0, lastModified = twoHoursAgo)
        repository.insert(folder)
        assertTrue(repository.getFolder(folder.id) != null)

        val updatedFolder = folder.copy(name = "remoteFolder1")
        persister.processBookmarkFolder(updatedFolder, emptyList())

        assertTrue(repository.getFolder(folder.id) != null)
        assertTrue(repository.getFolder(folder.id)!!.name == folder.name)
    }
}
