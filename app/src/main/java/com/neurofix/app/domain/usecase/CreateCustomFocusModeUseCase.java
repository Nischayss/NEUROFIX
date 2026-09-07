package com.neurofix.app.domain.usecase;

import com.neurofix.app.domain.repository.FocusModeRepository;

import javax.inject.Inject;

public class CreateCustomFocusModeUseCase {

    private final FocusModeRepository repository;

    @Inject
    public CreateCustomFocusModeUseCase(FocusModeRepository repository) {
        this.repository = repository;
    }

    public long execute(String name) {
        return repository.createCustomMode(name);
    }
}
