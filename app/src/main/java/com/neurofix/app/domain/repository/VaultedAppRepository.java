package com.neurofix.app.domain.repository;

import androidx.lifecycle.LiveData;

import com.neurofix.app.domain.model.InstalledApp;
import com.neurofix.app.domain.model.VaultedApp;

import java.util.List;

/**
 * Domain contract for persisting the user's Vault selections and onboarding
 * completion state. Implemented by data/repository/VaultedAppRepositoryImpl,
 * which coordinates Room (VaultedAppDao) and SharedPreferences — domain
 * never imports either.
 */
public interface VaultedAppRepository {
    void vaultApps(List<InstalledApp> apps);

    boolean isOnboardingComplete();

    void setOnboardingComplete();

    int getActiveVaultedAppCount();

    LiveData<List<VaultedApp>> observeAllVaultedApps();

    List<String> getVaultedPackageNames();

    void setAppActive(String packageName, boolean isActive);

    void removeApp(String packageName);
}
