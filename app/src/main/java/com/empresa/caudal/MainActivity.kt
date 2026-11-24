package com.empresa.caudal

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
            val settingsState by app.repository.userSettings.collectAsState(initial = null)

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

            // Re-bloquea al minimizar la app si la seguridad está activa
            DisposableEffect(settingsState?.isBiometricEnabled) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP && settingsState?.isBiometricEnabled == true) {
                        isUnlocked = false
                    }
                }
                this@MainActivity.lifecycle.addObserver(observer)
                onDispose {
                    this@MainActivity.lifecycle.removeObserver(observer)
                }
            }

            // Evalúa solo cuando DataStore ya entregó la configuración real (settingsState != null)
            LaunchedEffect(settingsState) {
                val loadedSettings = settingsState ?: return@LaunchedEffect
                if (!loadedSettings.isBiometricEnabled) {
                    isUnlocked = true
                } else if (!isUnlocked) {
                    triggerAuth()
                }
            }

            val darkTheme = settingsState?.isDarkTheme ?: true

            CaudalTheme(darkTheme = darkTheme) {
                val loadedSettings = settingsState
                when {
                    loadedSettings == null -> {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {}
                    }
                    loadedSettings.isBiometricEnabled && !isUnlocked -> {
                        AppLockScreen(
                            errorMessage = authError,
                            onUnlockClick = triggerAuth
                        )
                    }
                    else -> {
                        CaudalScaffold(viewModel = viewModel)
                    }
                }
            }
        }
    }
}