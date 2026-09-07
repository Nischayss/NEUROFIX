package com.neurofix.app.domain.usecase;

import com.neurofix.app.domain.repository.FocusModeRepository;

import javax.inject.Inject;

/**
 * Activates exactly one mode — any previously active mode is deactivated
 * first, in the same transaction (see FocusModeDao.setActiveMode), so at
 * most one mode is ever active.
 */
public class ActivateFocusModeUseCase {

    private final FocusModeRepository repository;

    @Inject
    public ActivateFocusModeUseCase(FocusModeRepository repository) {
        this.repository = repository;
    }

    public void execute(long modeId) {
        repository.setActiveMode(modeId);
    }
}
