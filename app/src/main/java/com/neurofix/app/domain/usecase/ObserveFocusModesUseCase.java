package com.neurofix.app.domain.usecase;

import androidx.lifecycle.LiveData;

import com.neurofix.app.domain.model.FocusMode;
import com.neurofix.app.domain.repository.FocusModeRepository;

import java.util.List;

import javax.inject.Inject;

public class ObserveFocusModesUseCase {

    private final FocusModeRepository repository;

    @Inject
    public ObserveFocusModesUseCase(FocusModeRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<FocusMode>> execute() {
        return repository.observeAllFocusModes();
    }
}
