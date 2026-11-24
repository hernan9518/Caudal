package com.empresa.caudal

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.empresa.caudal.security.BiometricAuthHelper
import com.empresa.caudal.ui.CaudalViewModel
import com.empresa.caudal.ui.components.AppLockScreen
import com.empresa.caudal.ui.navigation.CaudalScaffold
import com.empresa.caudal.ui.theme.CaudalTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as CaudalApp
        val factory = CaudalViewModel.Factory(app.repository)

        setContent {
            val viewModel: CaudalViewModel = viewModel(factory = factory)
            val settings by viewModel.userSettings.collectAsState()

            var isUnlocked by remember { mutableStateOf(false) }
            var authError by remember { mutableStateOf<String?>(null) }

            val triggerAuth = {
                BiometricAuthHelper.authenticate(
                    activity = this@MainActivity,
                    onSuccess = {
                        authError = null
                        isUnlocked = true
                    },
                    onError = { msg ->
                        authError = msg
                    }
                )
            }

            LaunchedEffect(settings.isBiometricEnabled) {
                if (!settings.isBiometricEnabled) {
                    isUnlocked = true
                } else if (!isUnlocked) {
                    triggerAuth()
                }
            }

            CaudalTheme(darkTheme = settings.isDarkTheme) {
                if (settings.isBiometricEnabled && !isUnlocked) {
                    AppLockScreen(
                        errorMessage = authError,
                        onUnlockClick = triggerAuth
                    )
                } else {
                    CaudalScaffold(viewModel = viewModel)
                }
            }
        }
    }
}