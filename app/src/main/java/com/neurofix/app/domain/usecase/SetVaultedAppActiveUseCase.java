package com.neurofix.app.domain.usecase;

import com.neurofix.app.domain.repository.VaultedAppRepository;

import javax.inject.Inject;

/**
 * Toggles a vaulted app's active state. Keeps the Room row — only
 * RemoveVaultedAppUseCase deletes it.
 */
public class SetVaultedAppActiveUseCase {

    private final VaultedAppRepository repository;

    @Inject
    public SetVaultedAppActiveUseCase(VaultedAppRepository repository) {
        this.repository = repository;
    }

    public void execute(String packageName, boolean isActive) {
        repository.setAppActive(packageName, isActive);
    }
}
