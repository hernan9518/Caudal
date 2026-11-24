package com.empresa.caudal.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "caudal_preferences")

data class UserSettings(
    val monthlyBudget: Double = 0.0,
    val isDarkTheme: Boolean = true,
    val currencySymbol: String = "$",
    val isBiometricEnabled: Boolean = false,
    val isDailyReminderEnabled: Boolean = false
)

class UserPreferences(private val context: Context) {

    private object Keys {
        val MONTHLY_BUDGET = doublePreferencesKey("monthly_budget")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            monthlyBudget = prefs[Keys.MONTHLY_BUDGET] ?: 0.0,
            isDarkTheme = prefs[Keys.IS_DARK_THEME] ?: true,
            currencySymbol = prefs[Keys.CURRENCY_SYMBOL] ?: "$",
            isBiometricEnabled = prefs[Keys.BIOMETRIC_ENABLED] ?: false,
            isDailyReminderEnabled = prefs[Keys.DAILY_REMINDER_ENABLED] ?: false
        )
    }

    suspend fun updateMonthlyBudget(amount: Double) {
        context.dataStore.edit { it[Keys.MONTHLY_BUDGET] = amount }
    }

    suspend fun updateDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_DARK_THEME] = enabled }
    }

    suspend fun updateCurrencySymbol(symbol: String) {
        context.dataStore.edit { it[Keys.CURRENCY_SYMBOL] = symbol }
    }

    suspend fun updateBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun updateDailyReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DAILY_REMINDER_ENABLED] = enabled }
    }
}