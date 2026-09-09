package com.neurofix.app.domain.usecase;

import com.neurofix.app.domain.repository.EnforcementSettingsRepository;
import javax.inject.Inject;

public class GetReliabilityNotificationEnabledUseCase {
    private final EnforcementSettingsRepository repository;

    @Inject
    public GetReliabilityNotificationEnabledUseCase(EnforcementSettingsRepository repository) {
        this.repository = repository;
    }

    public boolean execute() {
        return repository.isReliabilityNotificationEnabled();
    }
}