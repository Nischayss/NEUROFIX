package com.neurofix.app.domain.usecase;

import androidx.lifecycle.LiveData;

import com.neurofix.app.domain.repository.VaultedAppRepository;

import javax.inject.Inject;

/**
 * Returns the LIVE count of active Vaulted Apps for the Dashboard.
 *
 * Originally a one-shot int load — that was the root cause of a real bug:
 * Dashboard's count froze at whatever value it had when first loaded and
 * never updated after apps were removed/disabled in Manage Vault, because
 * DashboardFragment's ViewModel instance is reused (not recreated) when
 * navigating back from Manage Vault. Now backed by the DAO's live query
 * (which already existed, just wasn't wired through), so Dashboard reflects
 * changes exactly as promptly as Manage Vault's own list does — both are
 * driven by the same Room table invalidation.
 */
public class GetVaultedAppCountUseCase {

    private final VaultedAppRepository repository;

    @Inject
    public GetVaultedAppCountUseCase(VaultedAppRepository repository) {
        this.repository = repository;
    }

    public LiveData<Integer> execute() {
        return repository.observeActiveVaultedAppCount();
    }
}
