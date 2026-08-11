package com.neurofix.app.domain.usecase;

import com.neurofix.app.domain.repository.VaultedAppRepository;

import javax.inject.Inject;

/**
 * Returns how many apps are currently active in the Vault. Kept as its own
 * Use Case rather than calling the repository directly from the ViewModel,
 * since "vault count" is a value Statistics and Vault Engine will also need
 * later — one canonical definition instead of each caller re-deriving it.
 */
public class GetVaultedAppCountUseCase {

    private final VaultedAppRepository repository;

    @Inject
    public GetVaultedAppCountUseCase(VaultedAppRepository repository) {
        this.repository = repository;
    }

    public int execute() {
        return repository.getActiveVaultedAppCount();
    }
}
