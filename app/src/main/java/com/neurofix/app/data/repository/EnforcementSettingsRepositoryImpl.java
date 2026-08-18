package com.neurofix.app.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.neurofix.app.domain.model.EnforcementMode;
import com.neurofix.app.domain.repository.EnforcementSettingsRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Backed by SharedPreferences — same mechanism already used for the
 * onboarding-complete flag (see VaultedAppRepositoryImpl). A separate small
 * preferences file keeps this concern cleanly apart from vault/onboarding
 * data without adding any new storage technology to the project.
 */
@Singleton
public class EnforcementSettingsRepositoryImpl implements EnforcementSettingsRepository {

    private static final String PREFS_NAME = "neurofix_settings_prefs";
    private static final String KEY_ENFORCEMENT_MODE = "enforcement_mode";

    private final SharedPreferences preferences;

    @Inject
    public EnforcementSettingsRepositoryImpl(@ApplicationContext Context context) {
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public EnforcementMode getEnforcementMode() {
        String stored = preferences.getString(KEY_ENFORCEMENT_MODE, EnforcementMode.RETURN_HOME.name());
        try {
            return EnforcementMode.valueOf(stored);
        } catch (IllegalArgumentException e) {
            return EnforcementMode.RETURN_HOME;
        }
    }

    @Override
    public void setEnforcementMode(EnforcementMode mode) {
        preferences.edit().putString(KEY_ENFORCEMENT_MODE, mode.name()).apply();
    }
}
