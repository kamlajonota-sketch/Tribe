package com.example.core.preferences

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.core.localization.AppLanguage
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.StateFlow

class UserPreferencesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserPreferencesRepository.getInstance(application)

    val themeMode: StateFlow<ThemeMode> = repository.themeMode
    val appLanguage: StateFlow<AppLanguage> = repository.appLanguage

    fun updateTheme(mode: ThemeMode) {
        repository.setThemeMode(mode)
    }

    fun updateLanguage(language: AppLanguage) {
        repository.setAppLanguage(language)
    }
}
