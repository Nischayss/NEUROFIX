package com.neurofix.app.domain.repository;

import com.neurofix.app.domain.model.EnforcementMode;

/**
 * Domain contract for the single Vault Engine setting introduced in Step 8.
 * Implemented by data/repository/EnforcementSettingsRepositoryImpl using
 * SharedPreferences — the same storage mechanism already used for the
 * onboarding-complete flag. Kept as its own small repository rather than
 * folded into VaultedAppRepository since it's a genuinely separate concern
 * (a user preference, not vault data).
 */
public interface EnforcementSettingsRepository {
    EnforcementMode getEnforcementMode();

    void setEnforcementMode(EnforcementMode mode);
}
