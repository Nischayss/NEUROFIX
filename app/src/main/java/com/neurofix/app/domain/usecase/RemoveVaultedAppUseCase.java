package com.neurofix.app.domain.usecase;

import com.neurofix.app.domain.repository.VaultedAppRepository;

import javax.inject.Inject;

/**
 * Permanently deletes a vaulted app's Room record. Distinct from disabling
 * it (SetVaultedAppActiveUseCase) — this is irreversible; re-adding the app
 * later creates a fresh record via AddAppsToVaultUseCase.
 */
public class RemoveVaultedAppUseCase {

    private final VaultedAppRepository repository;

    @Inject
    public RemoveVaultedAppUseCase(VaultedAppRepository repository) {
        this.repository = repository;
    }

    public void execute(String packageName) {
        repository.removeApp(packageName);
    }
}
