package com.neurofix.app.data.repository;

import com.neurofix.app.data.local.OnboardingPreferencesDataStore;
import com.neurofix.app.database.dao.VaultedAppDao;
import com.neurofix.app.database.entity.VaultedAppEntity;
import com.neurofix.app.domain.model.InstalledApp;
import com.neurofix.app.domain.repository.VaultedAppRepository;

import java.util.List;

import javax.inject.Inject;

/**
 * Coordinates Room (Vaulted Apps table) and DataStore Preferences (a single
 * onboarding-complete boolean, via OnboardingPreferencesDataStore).
 *
 * Interface contract (VaultedAppRepository) is unchanged from the
 * SharedPreferences-based version — vaultApps/isOnboardingComplete/
 * setOnboardingComplete keep identical signatures and behavior, so
 * CompleteOnboardingUseCase and every caller above this layer needed no
 * changes.
 */
public class VaultedAppRepositoryImpl implements VaultedAppRepository {

    private final VaultedAppDao vaultedAppDao;
    private final OnboardingPreferencesDataStore onboardingPreferencesDataStore;

    @Inject
    public VaultedAppRepositoryImpl(VaultedAppDao vaultedAppDao,
                                     OnboardingPreferencesDataStore onboardingPreferencesDataStore) {
        this.vaultedAppDao = vaultedAppDao;
        this.onboardingPreferencesDataStore = onboardingPreferencesDataStore;
    }

    @Override
    public void vaultApps(List<InstalledApp> apps) {
        long now = System.currentTimeMillis();
        for (InstalledApp app : apps) {
            vaultedAppDao.insert(new VaultedAppEntity(
                    app.getPackageName(),
                    app.getDisplayName(),
                    now,
                    true
            ));
        }
    }

    @Override
    public boolean isOnboardingComplete() {
        return onboardingPreferencesDataStore.isOnboardingComplete();
    }

    @Override
    public void setOnboardingComplete() {
        onboardingPreferencesDataStore.setOnboardingComplete();
    }
}
