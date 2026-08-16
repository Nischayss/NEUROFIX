package com.neurofix.app.presentation.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.neurofix.app.core.AppExecutors;
import com.neurofix.app.domain.model.EnforcementMode;
import com.neurofix.app.domain.usecase.GetEnforcementModeUseCase;
import com.neurofix.app.domain.usecase.SetEnforcementModeUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Deliberately minimal: this is not a general Settings feature (theme, PIN,
 * biometrics, notification preferences are future steps) — it exposes only
 * the one Vault Engine enforcement-mode toggle introduced in Step 8.
 */
@HiltViewModel
public class SettingsViewModel extends ViewModel {

    private final SetEnforcementModeUseCase setEnforcementModeUseCase;
    private final AppExecutors appExecutors;

    private final MutableLiveData<EnforcementMode> enforcementMode = new MutableLiveData<>();

    @Inject
    public SettingsViewModel(GetEnforcementModeUseCase getEnforcementModeUseCase,
                              SetEnforcementModeUseCase setEnforcementModeUseCase,
                              AppExecutors appExecutors) {
        this.setEnforcementModeUseCase = setEnforcementModeUseCase;
        this.appExecutors = appExecutors;
        enforcementMode.setValue(getEnforcementModeUseCase.execute());
    }

    public LiveData<EnforcementMode> getEnforcementMode() {
        return enforcementMode;
    }

    public void setEnforcementMode(EnforcementMode mode) {
        enforcementMode.setValue(mode);
        appExecutors.diskIO().execute(() -> setEnforcementModeUseCase.execute(mode));
    }
}
