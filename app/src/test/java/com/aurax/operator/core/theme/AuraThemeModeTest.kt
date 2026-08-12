package com.aurax.operator.core.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class AuraThemeModeTest {
    @Test
    fun storedThemeValuesAreParsedCaseInsensitively() {
        assertEquals(AuraThemeMode.SYSTEM, AuraThemeMode.fromStored("system"))
        assertEquals(AuraThemeMode.DARK, AuraThemeMode.fromStored("DARK"))
        assertEquals(AuraThemeMode.LIGHT, AuraThemeMode.fromStored("Light"))
    }

    @Test
    fun unknownThemeFallsBackToSystem() {
        assertEquals(AuraThemeMode.SYSTEM, AuraThemeMode.fromStored("invalid"))
    }
}
