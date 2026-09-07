package com.neurofix.app.domain.usecase;

import com.neurofix.app.domain.repository.FocusModeRepository;

import javax.inject.Inject;

/**
 * Inserts the 4 preset modes (Study, Work, Sleep, Gym) exactly once. Safe
 * to call on every app startup — the count check makes repeated calls a
 * no-op after the first. Called from NeuroFixApplication.onCreate(), off
 * the main thread via AppExecutors, matching this codebase's existing
 * threading convention for Room writes triggered by app-level code.
 */
public class SeedDefaultFocusModesUseCase {

    private static final String[] PRESET_MODE_NAMES = {"Study", "Work", "Sleep", "Gym"};

    private final FocusModeRepository repository;

    @Inject
    public SeedDefaultFocusModesUseCase(FocusModeRepository repository) {
        this.repository = repository;
    }

    public void execute() {
        if (repository.getFocusModeCount() > 0) {
            return;
        }
        for (String name : PRESET_MODE_NAMES) {
            repository.createMode(name, true);
        }
    }
}
