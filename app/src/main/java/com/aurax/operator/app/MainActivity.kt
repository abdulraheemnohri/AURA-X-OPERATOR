package com.aurax.operator.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.aurax.operator.core.security.SafetyController
import com.aurax.operator.core.security.SecurePrefs
import com.aurax.operator.core.theme.AuraTheme
import com.aurax.operator.core.theme.AuraThemeMode
import com.aurax.operator.ui.navigation.AuraNavigation
import com.aurax.operator.ui.onboarding.OnboardingController
import com.aurax.operator.ui.onboarding.OnboardingScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeMode = AuraThemeMode.fromStored(SecurePrefs(this).themeMode)
        setContent { AuraTheme(themeMode) { Surface(Modifier.fillMaxSize()) {
            if (OnboardingController(this).completed) AuraNavigation() else OnboardingScreen { recreate() }
        } } }
    }
    override fun onStop() { super.onStop(); SafetyController.requestAbort("AURA-X UI moved to background") }
}
