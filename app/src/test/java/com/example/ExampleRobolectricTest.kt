package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.localization.AppLanguage
import com.example.core.preferences.UserPreferencesRepository
import com.example.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `verify app name resource is TRIBE`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TRIBE", appName)
    }

    @Test
    fun `verify preferences persistence for theme and language`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = UserPreferencesRepository(context)

        repo.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, repo.themeMode.value)

        repo.setAppLanguage(AppLanguage.BANGLA)
        assertEquals(AppLanguage.BANGLA, repo.appLanguage.value)
    }

    @Test
    fun `launch MainActivity without crash`() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        assertNotNull(controller.get())
    }
}
