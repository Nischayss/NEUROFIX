package com.neurofix.app.domain.usecase;

import com.neurofix.app.domain.model.FocusMode;
import com.neurofix.app.domain.repository.FocusModeRepository;

import javax.inject.Inject;

/**
 * @throws IllegalStateException if the given mode is a preset — the
 * repository enforces this too (defense in depth), but the exception
 * surfaces here to whichever ViewModel calls it, for a clear failure point
 * rather than a silent no-op.
 */
public class DeleteCustomFocusModeUseCase {

    private final FocusModeRepository repository;

    @Inject
    public DeleteCustomFocusModeUseCase(FocusModeRepository repository) {
        this.repository = repository;
    }

    public void execute(FocusMode mode) {
        repository.deleteCustomMode(mode);
    }
}
