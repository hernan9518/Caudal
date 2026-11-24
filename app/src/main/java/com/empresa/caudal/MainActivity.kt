package com.empresa.caudal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.empresa.caudal.ui.CaudalViewModel
import com.empresa.caudal.ui.navigation.CaudalScaffold
import com.empresa.caudal.ui.theme.CaudalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as CaudalApp
        val factory = CaudalViewModel.Factory(app.repository)

        setContent {
            val viewModel: CaudalViewModel = viewModel(factory = factory)
            val settings by viewModel.userSettings.collectAsState()

            CaudalTheme(darkTheme = settings.isDarkTheme) {
                CaudalScaffold(viewModel = viewModel)
            }
        }
    }
}