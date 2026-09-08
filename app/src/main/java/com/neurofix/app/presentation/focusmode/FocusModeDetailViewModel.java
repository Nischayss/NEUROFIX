package com.neurofix.app.presentation.focusmode;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.neurofix.app.core.AppExecutors;
import com.neurofix.app.domain.model.FocusModeApp;
import com.neurofix.app.domain.usecase.ObserveAppsForModeUseCase;
import com.neurofix.app.domain.usecase.RemoveAppFromFocusModeUseCase;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * No @Inject constructor arguments carry the modeId — this ViewModel is
 * created fresh per Fragment instance (default ViewModelProvider scope, not
 * activity-scoped), and the Fragment passes modeId into the observe/remove
 * calls directly from its nav arguments, same shape as SettingsViewModel
 * takes no per-screen ID either. Simpler than a Hilt assisted-injection
 * setup for a single Long.
 */
@HiltViewModel
public class FocusModeDetailViewModel extends ViewModel {

    private final ObserveAppsForModeUseCase observeAppsForModeUseCase;
    private final RemoveAppFromFocusModeUseCase removeAppFromFocusModeUseCase;
    private final AppExecutors appExecutors;

    @Inject
    public FocusModeDetailViewModel(ObserveAppsForModeUseCase observeAppsForModeUseCase,
                                     RemoveAppFromFocusModeUseCase removeAppFromFocusModeUseCase,
                                     AppExecutors appExecutors) {
        this.observeAppsForModeUseCase = observeAppsForModeUseCase;
        this.removeAppFromFocusModeUseCase = removeAppFromFocusModeUseCase;
        this.appExecutors = appExecutors;
    }

    public LiveData<List<FocusModeApp>> getAppsForMode(long modeId) {
        return observeAppsForModeUseCase.execute(modeId);
    }

    public void removeApp(long modeId, FocusModeApp app) {
        appExecutors.diskIO().execute(() ->
                removeAppFromFocusModeUseCase.execute(modeId, app.getPackageName()));
    }
}
