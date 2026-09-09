package com.neurofix.app.domain.usecase;

import com.neurofix.app.domain.repository.EnforcementSettingsRepository;
import javax.inject.Inject;

public class SetFocusModeUnionEnabledUseCase {
    private final EnforcementSettingsRepository repository;

    @Inject
    public SetFocusModeUnionEnabledUseCase(EnforcementSettingsRepository repository) {
        this.repository = repository;
    }

    public void execute(boolean enabled) {
        repository.setFocusModeUnionEnabled(enabled);
    }
}