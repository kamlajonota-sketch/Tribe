package com.example.core.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalTribeStrings = staticCompositionLocalOf { EnglishStrings }

object TribeLanguageContext {
    val current: TribeStrings
        @Composable
        @ReadOnlyComposable
        get() = LocalTribeStrings.current

    fun getStrings(language: AppLanguage): TribeStrings {
        return when (language) {
            AppLanguage.ENGLISH -> EnglishStrings
            AppLanguage.BANGLA -> BanglaStrings
        }
    }
}
