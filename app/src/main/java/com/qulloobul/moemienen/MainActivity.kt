package com.qulloobul.moemienen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.qulloobul.moemienen.data.AppViewModel
import com.qulloobul.moemienen.nav.AppNavHost
import com.qulloobul.moemienen.ui.theme.QulloobulMoemienenTheme

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val systemInDarkTheme = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(systemInDarkTheme) }

            QulloobulMoemienenTheme(useDarkTheme = isDarkMode) {
                AppNavHost(
                    viewModel = appViewModel,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { isDarkMode = !isDarkMode }
                )
            }
        }
    }
}
