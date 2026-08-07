package com.neurofix.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.onboardingDataStore by preferencesDataStore(name = "neurofix_onboarding_prefs")

/**
 * The only Kotlin file in the project. DataStore's public API is Kotlin
 * Flow/coroutines-based with no first-class Java surface (aside from an
 * RxJava3 adapter Google ships specifically for Java callers — using it
 * would reintroduce the reactive library already removed from this
 * project). Isolating the Kotlin surface to this one small class keeps the
 * rest of the codebase Java, per the project's primary-language rule.
 *
 * runBlocking is intentional, not a shortcut: every caller
 * (VaultedAppRepositoryImpl) already invokes these methods off the main
 * thread via AppExecutors.diskIO(), so blocking here preserves the exact
 * same synchronous contract the previous SharedPreferences-based
 * implementation had — no other class needed to change.
 */
class OnboardingPreferencesDataStore(private val context: Context) {

    companion object {
        private val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }

    fun isOnboardingComplete(): Boolean = runBlocking {
        context.onboardingDataStore.data.first()[KEY_ONBOARDING_COMPLETE] ?: false
    }

    fun setOnboardingComplete() {
        runBlocking {
            context.onboardingDataStore.edit { prefs ->
                prefs[KEY_ONBOARDING_COMPLETE] = true
            }
        }
    }
}
