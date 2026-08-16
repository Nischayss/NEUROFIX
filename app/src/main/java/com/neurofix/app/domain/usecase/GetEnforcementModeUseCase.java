package com.neurofix.app.domain.usecase;

import com.neurofix.app.domain.model.EnforcementMode;
import com.neurofix.app.domain.repository.EnforcementSettingsRepository;

import javax.inject.Inject;

public class GetEnforcementModeUseCase {

    private final EnforcementSettingsRepository repository;

    @Inject
    public GetEnforcementModeUseCase(EnforcementSettingsRepository repository) {
        this.repository = repository;
    }

    public EnforcementMode execute() {
        return repository.getEnforcementMode();
    }
}
