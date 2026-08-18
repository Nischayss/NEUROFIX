package com.neurofix.app.domain.usecase;

import com.neurofix.app.domain.repository.VaultedAppRepository;

import javax.inject.Inject;

/**
 * Reads the existing onboarding-complete flag (SharedPreferences, via the
 * existing VaultedAppRepository) — no new storage mechanism, no new flag.
 * This didn't previously exist because nothing needed to READ the flag
 * before now; only CompleteOnboardingUseCase ever WROTE it.
 */
public class IsOnboardingCompleteUseCase {

    private final VaultedAppRepository repository;

    @Inject
    public IsOnboardingCompleteUseCase(VaultedAppRepository repository) {
        this.repository = repository;
    }

    public boolean execute() {
        return repository.isOnboardingComplete();
    }
}
