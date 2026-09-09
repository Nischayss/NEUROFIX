package com.neurofix.app.domain.usecase;

import com.neurofix.app.domain.repository.EnforcementSettingsRepository;
import javax.inject.Inject;

public class SetReliabilityNotificationEnabledUseCase {
    private final EnforcementSettingsRepository repository;

    @Inject
    public SetReliabilityNotificationEnabledUseCase(EnforcementSettingsRepository repository) {
        this.repository = repository;
    }

    public void execute(boolean enabled) {
        repository.setReliabilityNotificationEnabled(enabled);
    }
}