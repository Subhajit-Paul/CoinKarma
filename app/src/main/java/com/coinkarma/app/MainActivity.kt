package com.coinkarma.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.coinkarma.app.nav.AppNavGraph
import com.coinkarma.app.ui.theme.CoinKarmaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = (application as CoinKarmaApp).db
        setContent {
            val profile by db.profile().observe().collectAsState(initial = null)
            CoinKarmaTheme(
                isDark     = profile?.darkMode ?: true,
                paletteKey = if (profile?.darkMode != false) profile?.paletteDark ?: "forest"
                             else profile?.paletteLight ?: "forest",
            ) {
                AppNavGraph()
            }
        }
    }
}
