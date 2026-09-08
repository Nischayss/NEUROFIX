package com.neurofix.app.presentation.focusmode;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.neurofix.app.core.AppExecutors;
import com.neurofix.app.domain.model.InstalledApp;
import com.neurofix.app.domain.usecase.AddAppToFocusModeUseCase;
import com.neurofix.app.domain.usecase.GetAddableAppsForModeUseCase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/** Same selection-tracking shape as AddAppsViewModel, scoped to one mode's list instead of the base Vault. */
@HiltViewModel
public class AddAppsToFocusModeViewModel extends ViewModel {

    private final GetAddableAppsForModeUseCase getAddableAppsForModeUseCase;
    private final AddAppToFocusModeUseCase addAppToFocusModeUseCase;
    private final AppExecutors appExecutors;

    private final MutableLiveData<List<InstalledApp>> addableApps = new MutableLiveData<>();
    private final MutableLiveData<Set<String>> selectedPackageNames = new MutableLiveData<>(new HashSet<>());
    private final MutableLiveData<Boolean> addCompleted = new MutableLiveData<>(false);

    @Inject
    public AddAppsToFocusModeViewModel(GetAddableAppsForModeUseCase getAddableAppsForModeUseCase,
                                        AddAppToFocusModeUseCase addAppToFocusModeUseCase,
                                        AppExecutors appExecutors) {
        this.getAddableAppsForModeUseCase = getAddableAppsForModeUseCase;
        this.addAppToFocusModeUseCase = addAppToFocusModeUseCase;
        this.appExecutors = appExecutors;
    }

    public LiveData<List<InstalledApp>> getAddableApps() {
        return addableApps;
    }

    public LiveData<Set<String>> getSelectedPackageNames() {
        return selectedPackageNames;
    }

    public LiveData<Boolean> getAddCompleted() {
        return addCompleted;
    }

    public void loadAddableApps(long modeId) {
        appExecutors.diskIO().execute(() -> {
            List<InstalledApp> apps = getAddableAppsForModeUseCase.execute(modeId);
            appExecutors.mainThread().execute(() -> addableApps.setValue(apps));
        });
    }

    public void toggleAppSelection(@NonNull InstalledApp app) {
        Set<String> current = new HashSet<>(selectedPackageNames.getValue());
        if (!current.remove(app.getPackageName())) {
            current.add(app.getPackageName());
        }
        selectedPackageNames.setValue(current);
    }

    public void confirmAdd(long modeId) {
        List<InstalledApp> allApps = addableApps.getValue();
        Set<String> selected = selectedPackageNames.getValue();
        if (allApps == null || selected == null || selected.isEmpty()) {
            return;
        }

        List<InstalledApp> toAdd = new ArrayList<>();
        for (InstalledApp app : allApps) {
            if (selected.contains(app.getPackageName())) {
                toAdd.add(app);
            }
        }

        appExecutors.diskIO().execute(() -> {
            for (InstalledApp app : toAdd) {
                addAppToFocusModeUseCase.execute(modeId, app.getPackageName(), app.getDisplayName());
            }
            appExecutors.mainThread().execute(() -> addCompleted.setValue(true));
        });
    }
}
