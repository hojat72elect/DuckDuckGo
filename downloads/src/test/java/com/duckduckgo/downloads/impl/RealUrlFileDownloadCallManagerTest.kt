

package com.duckduckgo.downloads.impl

import okhttp3.ResponseBody
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import retrofit2.Call

class RealUrlFileDownloadCallManagerTest {

    private val call: Call<ResponseBody> = mock()

    @Test
    fun whenRemovingNotAddedDownloadIdThenNoop() {
        RealUrlFileDownloadCallManager().remove(0L)

        verifyNoInteractions(call)
    }

    @Test
    fun whenRemovingAddedDownloadIdThenCancelDownloadAndRemove() {
        val downloadManager = RealUrlFileDownloadCallManager()
        downloadManager.add(0L, call)

        downloadManager.remove(0L)

        verify(call).cancel()

        downloadManager.remove(0L)
        verifyNoMoreInteractions(call)
    }

    @Test
    fun whenAddingDownloadIdTwiceThenOnlyLatestCallIsReplaced() {
        val secondCall: Call<ResponseBody> = mock()

        val downloadManager = RealUrlFileDownloadCallManager()
        downloadManager.add(0L, call)
        downloadManager.add(0L, secondCall)

        downloadManager.remove(0L)

        verifyNoInteractions(call)
        verify(secondCall).cancel()

        downloadManager.remove(0L)
        verifyNoInteractions(call)
        verifyNoMoreInteractions(secondCall)
    }
}
