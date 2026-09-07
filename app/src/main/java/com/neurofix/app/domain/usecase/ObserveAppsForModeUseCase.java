package com.neurofix.app.domain.usecase;

import androidx.lifecycle.LiveData;

import com.neurofix.app.domain.model.FocusModeApp;
import com.neurofix.app.domain.repository.FocusModeRepository;

import java.util.List;

import javax.inject.Inject;

public class ObserveAppsForModeUseCase {

    private final FocusModeRepository repository;

    @Inject
    public ObserveAppsForModeUseCase(FocusModeRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<FocusModeApp>> execute(long modeId) {
        return repository.observeAppsForMode(modeId);
    }
}
