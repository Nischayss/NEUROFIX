package com.neurofix.app.domain.usecase;

import com.neurofix.app.domain.repository.FocusModeRepository;

import javax.inject.Inject;

public class AddAppToFocusModeUseCase {

    private final FocusModeRepository repository;

    @Inject
    public AddAppToFocusModeUseCase(FocusModeRepository repository) {
        this.repository = repository;
    }

    public void execute(long modeId, String packageName, String displayName) {
        repository.addAppToMode(modeId, packageName, displayName);
    }
}
