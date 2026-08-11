package com.aurax.operator.app

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.aurax.operator.core.theme.AuraTheme
import com.aurax.operator.ui.navigation.AuraNavigation

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AuraTheme {
                Surface(Modifier.fillMaxSize()) { AuraNavigation() }
            }
        }
    }
}
