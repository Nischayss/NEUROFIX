package com.neurofix.app.presentation.vault;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.neurofix.app.core.AppExecutors;
import com.neurofix.app.domain.model.InstalledApp;
import com.neurofix.app.domain.usecase.AddAppsToVaultUseCase;
import com.neurofix.app.domain.usecase.GetAddableAppsUseCase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Same selection-tracking shape as OnboardingViewModel (load list, track
 * selected package names, confirm). Not extracted into a shared base class
 * for two consumers — each ViewModel serves a genuinely distinct screen and
 * flow, consistent with the project's "don't over-abstract" rule.
 */
@HiltViewModel
public class AddAppsViewModel extends ViewModel {

    private final GetAddableAppsUseCase getAddableAppsUseCase;
    private final AddAppsToVaultUseCase addAppsToVaultUseCase;
    private final AppExecutors appExecutors;

    private final MutableLiveData<List<InstalledApp>> addableApps = new MutableLiveData<>();
    private final MutableLiveData<Set<String>> selectedPackageNames = new MutableLiveData<>(new HashSet<>());
    private final MutableLiveData<Boolean> addCompleted = new MutableLiveData<>(false);

    @Inject
    public AddAppsViewModel(GetAddableAppsUseCase getAddableAppsUseCase,
                             AddAppsToVaultUseCase addAppsToVaultUseCase,
                             AppExecutors appExecutors) {
        this.getAddableAppsUseCase = getAddableAppsUseCase;
        this.addAppsToVaultUseCase = addAppsToVaultUseCase;
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

    public void loadAddableApps() {
        appExecutors.diskIO().execute(() -> {
            List<InstalledApp> apps = getAddableAppsUseCase.execute();
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

    public void confirmAdd() {
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
            addAppsToVaultUseCase.execute(toAdd);
            appExecutors.mainThread().execute(() -> addCompleted.setValue(true));
        });
    }
}
