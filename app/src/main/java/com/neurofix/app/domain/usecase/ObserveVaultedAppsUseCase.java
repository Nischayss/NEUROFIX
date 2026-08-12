package com.neurofix.app.domain.usecase;

import androidx.lifecycle.LiveData;

import com.neurofix.app.domain.model.VaultedApp;
import com.neurofix.app.domain.repository.VaultedAppRepository;

import java.util.List;

import javax.inject.Inject;

/**
 * Returns the live, always-current list of every app in the Vault (active
 * and inactive) for the Manage Vault screen. Pass-through today, but kept
 * as its own Use Case — same rationale as GetInstallableAppsUseCase —
 * rather than the ViewModel calling the repository directly.
 */
public class ObserveVaultedAppsUseCase {

    private final VaultedAppRepository repository;

    @Inject
    public ObserveVaultedAppsUseCase(VaultedAppRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<VaultedApp>> execute() {
        return repository.observeAllVaultedApps();
    }
}
