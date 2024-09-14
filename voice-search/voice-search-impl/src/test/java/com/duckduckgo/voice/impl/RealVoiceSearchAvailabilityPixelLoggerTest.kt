

package com.duckduckgo.voice.impl

import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.voice.store.VoiceSearchRepository
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RealVoiceSearchAvailabilityPixelLoggerTest {
    @Mock
    private lateinit var pixel: Pixel

    @Mock
    private lateinit var voiceSearchRepository: VoiceSearchRepository

    private lateinit var testee: RealVoiceSearchAvailabilityPixelLogger

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        testee = RealVoiceSearchAvailabilityPixelLogger(pixel, voiceSearchRepository)
    }

    @Test
    fun whenHasNotLoggedAvailabilityThenLogPixel() {
        whenever(voiceSearchRepository.getHasLoggedAvailability()).thenReturn(false)

        testee.log()

        verify(pixel).fire(VoiceSearchPixelNames.VOICE_SEARCH_AVAILABLE)
        verify(voiceSearchRepository).saveLoggedAvailability()
    }

    @Test
    fun whenHasLoggedAvailabilityThenDoNothing() {
        whenever(voiceSearchRepository.getHasLoggedAvailability()).thenReturn(true)

        testee.log()

        verify(pixel, never()).fire(VoiceSearchPixelNames.VOICE_SEARCH_AVAILABLE)
        verify(voiceSearchRepository, never()).saveLoggedAvailability()
    }
}
