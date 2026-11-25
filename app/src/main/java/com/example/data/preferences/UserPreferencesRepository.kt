package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.local.entity.LangueCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.langoDataStore: DataStore<Preferences> by preferencesDataStore(name = "lango_prefs")

data class LangoPreferences(
    val langue: String = LangueCode.FR,
    val onboardingCompleted: Boolean = false,
    val diagnosticCompleted: Boolean = false,
    val smsAlertsEnabled: Boolean = false,
    val smsAlertVille: String = "Bangui - PK5",
    val isOfflineSimulated: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val LANGUE = stringPreferencesKey("langue_pref")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val DIAGNOSTIC_COMPLETED = booleanPreferencesKey("diagnostic_completed")
        val SMS_ALERTS_ENABLED = booleanPreferencesKey("sms_alerts_enabled")
        val SMS_ALERT_VILLE = stringPreferencesKey("sms_alert_ville")
        val OFFLINE_SIMULATED = booleanPreferencesKey("offline_simulated")
    }

    val preferences: Flow<LangoPreferences> = context.langoDataStore.data.map { prefs ->
        LangoPreferences(
            langue = prefs[Keys.LANGUE] ?: LangueCode.FR,
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            diagnosticCompleted = prefs[Keys.DIAGNOSTIC_COMPLETED] ?: false,
            smsAlertsEnabled = prefs[Keys.SMS_ALERTS_ENABLED] ?: false,
            smsAlertVille = prefs[Keys.SMS_ALERT_VILLE] ?: "Bangui - PK5",
            isOfflineSimulated = prefs[Keys.OFFLINE_SIMULATED] ?: false
        )
    }

    suspend fun setLangue(langue: String) {
        context.langoDataStore.edit { it[Keys.LANGUE] = langue }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.langoDataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setDiagnosticCompleted(completed: Boolean) {
        context.langoDataStore.edit { it[Keys.DIAGNOSTIC_COMPLETED] = completed }
    }

    suspend fun setSmsAlerts(enabled: Boolean, ville: String? = null) {
        context.langoDataStore.edit { prefs ->
            prefs[Keys.SMS_ALERTS_ENABLED] = enabled
            if (ville != null) prefs[Keys.SMS_ALERT_VILLE] = ville
        }
    }

    suspend fun setOfflineSimulated(offline: Boolean) {
        context.langoDataStore.edit { it[Keys.OFFLINE_SIMULATED] = offline }
    }

    suspend fun clearAll() {
        context.langoDataStore.edit { it.clear() }
    }
}
