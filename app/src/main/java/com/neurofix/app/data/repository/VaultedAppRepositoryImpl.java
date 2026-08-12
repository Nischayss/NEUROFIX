package com.neurofix.app.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.neurofix.app.database.dao.VaultedAppDao;
import com.neurofix.app.database.entity.VaultedAppEntity;
import com.neurofix.app.domain.model.InstalledApp;
import com.neurofix.app.domain.model.VaultedApp;
import com.neurofix.app.domain.repository.VaultedAppRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Coordinates Room (Vaulted Apps table) and a small SharedPreferences file
 * (a single onboarding-complete boolean).
 *
 * Using plain SharedPreferences here rather than DataStore is a deliberate
 * simplicity choice: DataStore's API is Flow/coroutines-based, which adds
 * real complexity in a Java-only project for what is a single synchronous
 * boolean read/write.
 */
public class VaultedAppRepositoryImpl implements VaultedAppRepository {

    private static final String PREFS_NAME = "neurofix_onboarding_prefs";
    private static final String KEY_ONBOARDING_COMPLETE = "onboarding_complete";

    private final VaultedAppDao vaultedAppDao;
    private final SharedPreferences preferences;

    @Inject
    public VaultedAppRepositoryImpl(VaultedAppDao vaultedAppDao, @ApplicationContext Context context) {
        this.vaultedAppDao = vaultedAppDao;
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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
        return preferences.getBoolean(KEY_ONBOARDING_COMPLETE, false);
    }

    @Override
    public void setOnboardingComplete() {
        preferences.edit().putBoolean(KEY_ONBOARDING_COMPLETE, true).apply();
    }

    @Override
    public int getActiveVaultedAppCount() {
        return vaultedAppDao.getActiveVaultedAppCount();
    }

    @Override
    public LiveData<List<VaultedApp>> observeAllVaultedApps() {
        return Transformations.map(vaultedAppDao.observeAllVaultedApps(), this::mapToDomainList);
    }

    @Override
    public List<String> getVaultedPackageNames() {
        return vaultedAppDao.getAllVaultedPackageNames();
    }

    @Override
    public void setAppActive(String packageName, boolean isActive) {
        vaultedAppDao.updateActiveState(packageName, isActive);
    }

    @Override
    public void removeApp(String packageName) {
        vaultedAppDao.deleteByPackageName(packageName);
    }

    private List<VaultedApp> mapToDomainList(List<VaultedAppEntity> entities) {
        List<VaultedApp> result = new ArrayList<>();
        for (VaultedAppEntity entity : entities) {
            result.add(new VaultedApp(entity.getPackageName(), entity.getDisplayName(), entity.isActive()));
        }
        return result;
    }
}
