package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.screens.MainMotherBrainDashboard
import com.example.ui.theme.DashboardThemeMode
import com.example.ui.theme.MwasaAiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var currentTheme by remember { mutableStateOf(DashboardThemeMode.NAVY_ELECTRIC_BLUE) }

            MwasaAiTheme(currentTheme = currentTheme) {
                MainMotherBrainDashboard(
                    currentTheme = currentTheme,
                    onCycleTheme = {
                        val all = DashboardThemeMode.values()
                        val nextIndex = (currentTheme.ordinal + 1) % all.size
                        currentTheme = all[nextIndex]
                    },
                    onSelectTheme = { chosen ->
                        currentTheme = chosen
                    }
                )
            }
        }
    }
}
