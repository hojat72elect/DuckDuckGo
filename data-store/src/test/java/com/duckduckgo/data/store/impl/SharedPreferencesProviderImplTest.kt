package com.duckduckgo.data.store.impl

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.data.store.api.SharedPreferencesProvider
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SharedPreferencesProviderImplTest {

    private val context: Context =
        InstrumentationRegistry.getInstrumentation().context.applicationContext
    private lateinit var prefs: SharedPreferences
    private lateinit var vpnPreferencesProvider: SharedPreferencesProvider
    private lateinit var NAME: String

    @Before
    fun setup() {
        NAME = UUID.randomUUID().toString()
        prefs = context.getSharedPreferences(NAME, MODE_PRIVATE)
        vpnPreferencesProvider = SharedPreferencesProviderImpl(context)
    }

    @Test
    fun whenGetMultiprocessPreferencesThenMigrateToHarmony() {
        prefs.edit(commit = true) { putBoolean("bool", true) }
        prefs.edit(commit = true) { putString("string", "true") }
        prefs.edit(commit = true) { putInt("int", 1) }
        prefs.edit(commit = true) { putFloat("float", 1f) }
        prefs.edit(commit = true) { putLong("long", 1L) }

        val harmony =
            vpnPreferencesProvider.getSharedPreferences(NAME, multiprocess = true, migrate = true)

        assertEquals(true, harmony.getBoolean("bool", false))
        assertEquals("true", harmony.getString("string", "false"))
        assertEquals(1, harmony.getInt("int", 0))
        assertEquals(1f, harmony.getFloat("float", 0f))
        assertEquals(1L, harmony.getLong("long", 0L))
    }

    @Test
    fun whenGetMultiprocessPreferencesAndMigrateIsFalseThenDoNotMigrateToHarmony() {
        prefs.edit(commit = true) { putBoolean("bool", true) }
        prefs.edit(commit = true) { putString("string", "true") }
        prefs.edit(commit = true) { putInt("int", 1) }
        prefs.edit(commit = true) { putFloat("float", 1f) }
        prefs.edit(commit = true) { putLong("long", 1L) }

        val harmony = vpnPreferencesProvider.getSharedPreferences(NAME, multiprocess = true)

        assertNotEquals(true, harmony.getBoolean("bool", false))
        assertNotEquals("true", harmony.getString("string", "false"))
        assertNotEquals(1, harmony.getInt("int", 0))
        assertNotEquals(1f, harmony.getFloat("float", 0f))
        assertNotEquals(1L, harmony.getLong("long", 0L))
    }

    @Test
    fun testSafeSharedPreferences() {
        val prefs = com.duckduckgo.data.store.impl.SafeSharedPreferences(
            vpnPreferencesProvider.getSharedPreferences(NAME)
        )

        prefs.edit(commit = true) { putBoolean("bool", true) }
        prefs.edit(commit = true) { putString("string", "true") }
        prefs.edit(commit = true) { putInt("int", 1) }
        prefs.edit(commit = true) { putFloat("float", 1f) }
        prefs.edit(commit = true) { putLong("long", 1L) }

        assertEquals(true, prefs.getBoolean("bool", false))
        assertEquals("true", prefs.getString("string", "false"))
        assertEquals(1, prefs.getInt("int", 0))
        assertEquals(1f, prefs.getFloat("float", 0f))
        assertEquals(1L, prefs.getLong("long", 0L))
    }
}
