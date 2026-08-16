package com.neurofix.app.domain.usecase;

import com.neurofix.app.domain.model.EnforcementMode;
import com.neurofix.app.domain.repository.EnforcementSettingsRepository;

import javax.inject.Inject;

public class SetEnforcementModeUseCase {

    private final EnforcementSettingsRepository repository;

    @Inject
    public SetEnforcementModeUseCase(EnforcementSettingsRepository repository) {
        this.repository = repository;
    }

    public void execute(EnforcementMode mode) {
        repository.setEnforcementMode(mode);
    }
}
