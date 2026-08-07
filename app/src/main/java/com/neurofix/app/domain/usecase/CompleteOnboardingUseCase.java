package com.neurofix.app.domain.usecase;

import com.neurofix.app.domain.model.InstalledApp;
import com.neurofix.app.domain.repository.VaultedAppRepository;

import java.util.List;

import javax.inject.Inject;

/**
 * Finishes onboarding: persists the user's chosen apps as Vaulted Apps and
 * marks onboarding complete so it isn't shown again on next launch. Two
 * repository calls are grouped here because they represent one business
 * transaction ("finish onboarding"), not two unrelated actions — a ViewModel
 * calling both separately could leave the app in a half-finished state if
 * one step is skipped.
 */
public class CompleteOnboardingUseCase {

    private final VaultedAppRepository repository;

    @Inject
    public CompleteOnboardingUseCase(VaultedAppRepository repository) {
        this.repository = repository;
    }

    public void execute(List<InstalledApp> selectedApps) {
        repository.vaultApps(selectedApps);
        repository.setOnboardingComplete();
    }
}
