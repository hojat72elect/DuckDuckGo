

package com.duckduckgo.voice.impl

import com.duckduckgo.voice.api.VoiceSearchAvailability
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PermissionAwareVoiceSearchLauncherTest {
    @Mock
    private lateinit var permissionRequest: PermissionRequest

    @Mock
    private lateinit var voiceSearchActivityLauncher: VoiceSearchActivityLauncher

    @Mock
    private lateinit var voiceSearchPermissionCheck: VoiceSearchPermissionCheck

    @Mock
    private lateinit var voiceSearchAvailability: VoiceSearchAvailability

    private lateinit var testee: PermissionAwareVoiceSearchLauncher

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        testee =
            PermissionAwareVoiceSearchLauncher(permissionRequest, voiceSearchActivityLauncher, voiceSearchPermissionCheck, voiceSearchAvailability)
    }

    @Test
    fun whenPermissionsNotGrantedThenLaunchPermissionRequest() {
        whenever(voiceSearchPermissionCheck.hasRequiredPermissionsGranted()).thenReturn(false)
        whenever(voiceSearchAvailability.isVoiceSearchAvailable).thenReturn(true)

        testee.launch(mock())

        verify(permissionRequest).launch(any())
        verify(voiceSearchActivityLauncher, never()).launch(any())
    }

    @Test
    fun whenPermissionsNotGrantedAndVoiceSearchNotAvailableThenLaunchPermissionRequest() {
        whenever(voiceSearchPermissionCheck.hasRequiredPermissionsGranted()).thenReturn(false)
        whenever(voiceSearchAvailability.isVoiceSearchAvailable).thenReturn(false)

        testee.launch(mock())

        verify(permissionRequest, never()).launch(any())
        verify(voiceSearchActivityLauncher, never()).launch(any())
    }

    @Test
    fun whenPermissionsGrantedThenLaunchVoiceSearchActivity() {
        whenever(voiceSearchPermissionCheck.hasRequiredPermissionsGranted()).thenReturn(true)
        whenever(voiceSearchAvailability.isVoiceSearchAvailable).thenReturn(true)

        testee.launch(mock())

        verify(voiceSearchActivityLauncher).launch(any())
        verify(permissionRequest, never()).launch(any())
    }

    @Test
    fun whenPermissionsGrantedAndVoiceSearchNotAvailableThenLaunchVoiceSearchActivity() {
        whenever(voiceSearchPermissionCheck.hasRequiredPermissionsGranted()).thenReturn(true)
        whenever(voiceSearchAvailability.isVoiceSearchAvailable).thenReturn(false)

        testee.launch(mock())

        verify(voiceSearchActivityLauncher, never()).launch(any())
        verify(permissionRequest, never()).launch(any())
    }
}
