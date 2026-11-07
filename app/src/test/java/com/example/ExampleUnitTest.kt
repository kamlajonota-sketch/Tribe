package com.example

import com.example.core.localization.AppLanguage
import com.example.core.localization.BanglaStrings
import com.example.core.localization.EnglishStrings
import com.example.core.localization.StrictAuthStrings
import com.example.core.localization.TribeLanguageContext
import com.example.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PhaseOneFoundationTest {

    @Test
    fun verifyStrictEnglishAuthLabels() {
        // Section 7 Requirement: Authentication must strictly match these exact English labels
        assertEquals("Welcome to TRIBE", StrictAuthStrings.WELCOME_TITLE)
        assertEquals("Your next adventure starts here.", StrictAuthStrings.WELCOME_SUBTITLE)
        assertEquals("Continue with Google", StrictAuthStrings.CONTINUE_WITH_GOOGLE)
        assertEquals("Or continue with email", StrictAuthStrings.OR_CONTINUE_WITH_EMAIL)
        assertEquals("Email Address", StrictAuthStrings.EMAIL_ADDRESS)
        assertEquals("Password", StrictAuthStrings.PASSWORD)
        assertEquals("Forgot Password?", StrictAuthStrings.FORGOT_PASSWORD)
        assertEquals("Sign In", StrictAuthStrings.SIGN_IN)
        assertEquals("Create Account", StrictAuthStrings.CREATE_ACCOUNT)
        assertEquals("Sign Up", StrictAuthStrings.SIGN_UP)
    }

    @Test
    fun verifyBilingualResolution() {
        val en = TribeLanguageContext.getStrings(AppLanguage.ENGLISH)
        val bn = TribeLanguageContext.getStrings(AppLanguage.BANGLA)

        assertEquals("Home", en.navHome)
        assertEquals("হোম", bn.navHome)

        assertEquals("My Plans", en.navMyPlans)
        assertEquals("আমার পরিকল্পনা", bn.navMyPlans)

        assertEquals("Explore", en.navExplore)
        assertEquals("ঘুরে দেখুন", bn.navExplore)

        assertEquals("Profile", en.navProfile)
        assertEquals("প্রোফাইল", bn.navProfile)
    }

    @Test
    fun verifyThemeModes() {
        val modes = ThemeMode.entries
        assertEquals(3, modes.size)
        assertNotNull(ThemeMode.valueOf("LIGHT"))
        assertNotNull(ThemeMode.valueOf("DARK"))
        assertNotNull(ThemeMode.valueOf("SYSTEM"))
    }
}
