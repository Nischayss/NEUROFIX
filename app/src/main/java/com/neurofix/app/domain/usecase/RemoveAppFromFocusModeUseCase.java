package com.neurofix.app.domain.usecase;

import com.neurofix.app.domain.repository.FocusModeRepository;

import javax.inject.Inject;

public class RemoveAppFromFocusModeUseCase {

    private final FocusModeRepository repository;

    @Inject
    public RemoveAppFromFocusModeUseCase(FocusModeRepository repository) {
        this.repository = repository;
    }

    public void execute(long modeId, String packageName) {
        repository.removeAppFromMode(modeId, packageName);
    }
}
