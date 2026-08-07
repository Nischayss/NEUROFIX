package com.neurofix.app.domain.repository;

import com.neurofix.app.domain.model.InstalledApp;

import java.util.List;

/**
 * Domain contract for persisting the user's Vault selections and onboarding
 * completion state. Implemented by data/repository/VaultedAppRepositoryImpl,
 * which coordinates Room (VaultedAppDao) and DataStore — domain never
 * imports either.
 */
public interface VaultedAppRepository {
    void vaultApps(List<InstalledApp> apps);

    boolean isOnboardingComplete();

    void setOnboardingComplete();
}
