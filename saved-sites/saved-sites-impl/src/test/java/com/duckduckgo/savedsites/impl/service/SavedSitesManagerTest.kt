

package com.duckduckgo.savedsites.impl.service

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.common.utils.formatters.time.DatabaseDateFormatter
import com.duckduckgo.savedsites.api.models.SavedSite
import com.duckduckgo.savedsites.api.service.ExportSavedSitesResult
import com.duckduckgo.savedsites.api.service.ImportSavedSitesResult
import com.duckduckgo.savedsites.api.service.SavedSitesExporter
import com.duckduckgo.savedsites.api.service.SavedSitesImporter
import com.duckduckgo.savedsites.impl.SavedSitesPixelName
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class SavedSitesManagerTest {

    @get:Rule
    @Suppress("unused")
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private var importer: SavedSitesImporter = mock()
    private var exporter: SavedSitesExporter = mock()
    private var pixel: Pixel = mock()
    private lateinit var testee: RealSavedSitesManager

    @Before
    fun before() {
        testee = RealSavedSitesManager(importer, exporter, pixel)
    }

    @Test
    fun whenBookmarksImportSucceedsThenPixelIsSent() = runTest {
        val someUri = Uri.parse("")
        val importedBookmarks = listOf(aBookmark())
        whenever(importer.import(someUri)).thenReturn(ImportSavedSitesResult.Success(importedBookmarks))

        testee.import(someUri)

        verify(pixel).fire(
            SavedSitesPixelName.BOOKMARK_IMPORT_SUCCESS,
            mapOf(Pixel.PixelParameter.BOOKMARK_COUNT to importedBookmarks.size.toString()),
        )
    }

    @Test
    fun whenFavoritesImportSucceedsThenPixelIsSent() = runTest {
        val someUri = Uri.parse("")
        val importedFavorites = listOf(aFavorite())
        whenever(importer.import(someUri)).thenReturn(ImportSavedSitesResult.Success(importedFavorites))

        testee.import(someUri)

        verify(pixel).fire(
            SavedSitesPixelName.BOOKMARK_IMPORT_SUCCESS,
            mapOf(Pixel.PixelParameter.BOOKMARK_COUNT to importedFavorites.size.toString()),
        )
    }

    @Test
    fun whenSavedSitesImportFailsThenPixelIsSent() = runTest {
        val someUri = Uri.parse("")
        whenever(importer.import(someUri)).thenReturn(ImportSavedSitesResult.Error(Exception()))

        testee.import(someUri)

        verify(pixel).fire(SavedSitesPixelName.BOOKMARK_IMPORT_ERROR)
    }

    @Test
    fun whenSavedSitesExportSucceedsThenPixelIsSent() = runTest {
        val someUri = Uri.parse("")
        whenever(exporter.export(someUri)).thenReturn(ExportSavedSitesResult.Success)

        testee.export(someUri)

        verify(pixel).fire(SavedSitesPixelName.BOOKMARK_EXPORT_SUCCESS)
    }

    @Test
    fun whenSavedSitesExportFailsThenPixelIsSent() = runTest {
        val someUri = Uri.parse("")
        whenever(exporter.export(someUri)).thenReturn(ExportSavedSitesResult.Error(Exception()))

        testee.export(someUri)

        verify(pixel).fire(SavedSitesPixelName.BOOKMARK_EXPORT_ERROR)
    }

    private fun aBookmark(): SavedSite.Bookmark {
        return SavedSite.Bookmark(
            UUID.randomUUID().toString(),
            "title",
            "url",
            UUID.randomUUID().toString(),
            lastModified = DatabaseDateFormatter.iso8601(),
        )
    }

    private fun aFavorite(): SavedSite.Favorite {
        return SavedSite.Favorite(UUID.randomUUID().toString(), "title", "url", lastModified = DatabaseDateFormatter.iso8601(), 0)
    }
}
