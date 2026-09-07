package com.neurofix.app.domain.usecase;

import androidx.lifecycle.LiveData;

import com.neurofix.app.domain.repository.FocusModeRepository;

import java.util.List;

import javax.inject.Inject;

/**
 * The stream the enforcement engine will consume (wired in a later step,
 * not yet in VaultAccessibilityService) to union with the base Vault's
 * active package names. Package names only — deliberately not the full
 * FocusMode/FocusModeApp domain objects, since enforcement doesn't need
 * them.
 */
public class ObserveActiveModePackageNamesUseCase {

    private final FocusModeRepository repository;

    @Inject
    public ObserveActiveModePackageNamesUseCase(FocusModeRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<String>> execute() {
        return repository.observeActiveModePackageNames();
    }
}
