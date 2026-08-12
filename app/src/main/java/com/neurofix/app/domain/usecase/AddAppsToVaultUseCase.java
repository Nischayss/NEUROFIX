package com.neurofix.app.domain.usecase;

import com.neurofix.app.domain.model.InstalledApp;
import com.neurofix.app.domain.repository.VaultedAppRepository;

import java.util.List;

import javax.inject.Inject;

/**
 * Adds the user's selection from the Add Apps screen to the Vault. Thin
 * wrapper over the existing repository.vaultApps(...) — the same method
 * Onboarding already uses to insert vaulted apps (REPLACE on the
 * packageName primary key, marked active, timestamped) — no new insert
 * logic was written for Step 7.
 */
public class AddAppsToVaultUseCase {

    private final VaultedAppRepository repository;

    @Inject
    public AddAppsToVaultUseCase(VaultedAppRepository repository) {
        this.repository = repository;
    }

    public void execute(List<InstalledApp> apps) {
        repository.vaultApps(apps);
    }
}
