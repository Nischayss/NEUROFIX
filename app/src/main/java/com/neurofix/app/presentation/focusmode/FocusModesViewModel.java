package com.neurofix.app.presentation.focusmode;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.neurofix.app.core.AppExecutors;
import com.neurofix.app.domain.model.FocusMode;
import com.neurofix.app.domain.usecase.ActivateFocusModeUseCase;
import com.neurofix.app.domain.usecase.CreateCustomFocusModeUseCase;
import com.neurofix.app.domain.usecase.DeactivateFocusModeUseCase;
import com.neurofix.app.domain.usecase.DeleteCustomFocusModeUseCase;
import com.neurofix.app.domain.usecase.ObserveFocusModesUseCase;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Room's LiveData auto-invalidates on any focus_modes/focus_mode_apps
 * change, so this never manually refreshes after a write — same pattern as
 * ManageVaultViewModel. All writes go through AppExecutors.diskIO().
 */
@HiltViewModel
public class FocusModesViewModel extends ViewModel {

    private final ActivateFocusModeUseCase activateFocusModeUseCase;
    private final DeactivateFocusModeUseCase deactivateFocusModeUseCase;
    private final CreateCustomFocusModeUseCase createCustomFocusModeUseCase;
    private final DeleteCustomFocusModeUseCase deleteCustomFocusModeUseCase;
    private final AppExecutors appExecutors;

    private final LiveData<List<FocusMode>> focusModes;

    @Inject
    public FocusModesViewModel(ObserveFocusModesUseCase observeFocusModesUseCase,
                                ActivateFocusModeUseCase activateFocusModeUseCase,
                                DeactivateFocusModeUseCase deactivateFocusModeUseCase,
                                CreateCustomFocusModeUseCase createCustomFocusModeUseCase,
                                DeleteCustomFocusModeUseCase deleteCustomFocusModeUseCase,
                                AppExecutors appExecutors) {
        this.activateFocusModeUseCase = activateFocusModeUseCase;
        this.deactivateFocusModeUseCase = deactivateFocusModeUseCase;
        this.createCustomFocusModeUseCase = createCustomFocusModeUseCase;
        this.deleteCustomFocusModeUseCase = deleteCustomFocusModeUseCase;
        this.appExecutors = appExecutors;
        this.focusModes = observeFocusModesUseCase.execute();
    }

    public LiveData<List<FocusMode>> getFocusModes() {
        return focusModes;
    }

    /** Switch turned on: activate this mode (deactivates any other, in one DAO transaction). */
    public void setActive(FocusMode mode, boolean active) {
        appExecutors.diskIO().execute(() -> {
            if (active) {
                activateFocusModeUseCase.execute(mode.getId());
            } else {
                deactivateFocusModeUseCase.execute();
            }
        });
    }

    public void createCustomMode(String name) {
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        appExecutors.diskIO().execute(() -> createCustomFocusModeUseCase.execute(name.trim()));
    }

    /** No-op (with the guard already enforced in the repository) if mode is a preset — the UI never shows delete for presets in the first place. */
    public void deleteMode(FocusMode mode) {
        appExecutors.diskIO().execute(() -> deleteCustomFocusModeUseCase.execute(mode));
    }
}
