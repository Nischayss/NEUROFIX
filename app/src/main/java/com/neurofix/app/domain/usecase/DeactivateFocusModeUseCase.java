package com.neurofix.app.domain.usecase;

import com.neurofix.app.domain.repository.FocusModeRepository;

import javax.inject.Inject;

/**
 * Turns off whichever mode is currently active. No mode active means only
 * the base Vault is enforced — the Union rule's ground state.
 */
public class DeactivateFocusModeUseCase {

    private final FocusModeRepository repository;

    @Inject
    public DeactivateFocusModeUseCase(FocusModeRepository repository) {
        this.repository = repository;
    }

    public void execute() {
        repository.setActiveMode(null);
    }
}
