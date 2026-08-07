package com.neurofix.app.presentation.onboarding;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.neurofix.app.core.AppExecutors;
import com.neurofix.app.domain.model.InstalledApp;
import com.neurofix.app.domain.usecase.CompleteOnboardingUseCase;
import com.neurofix.app.domain.usecase.GetInstallableAppsUseCase;
import com.neurofix.app.permissions.PermissionHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Single ViewModel shared by Welcome, Permissions, and App Selection
 * fragments (scoped to the Activity via ViewModelProvider) so selection
 * state survives navigation between onboarding screens without a second
 * persistence mechanism. Extends ViewModel directly, not a Base class —
 * nothing here is shared with any other ViewModel yet.
 *
 * Injecting the Application Context (not an Activity Context) is safe: it
 * cannot leak a destroyed Activity, and is required to call PermissionHelper.
 */
@HiltViewModel
public class OnboardingViewModel extends ViewModel {

    private final GetInstallableAppsUseCase getInstallableAppsUseCase;
    private final CompleteOnboardingUseCase completeOnboardingUseCase;
    private final AppExecutors appExecutors;
    private final Context appContext;

    private final MutableLiveData<List<InstalledApp>> installableApps = new MutableLiveData<>();
    private final MutableLiveData<Set<String>> selectedPackageNames = new MutableLiveData<>(new HashSet<>());
    private final MutableLiveData<Boolean> usageAccessGranted = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> accessibilityGranted = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> onboardingCompleted = new MutableLiveData<>(false);

    @Inject
    public OnboardingViewModel(GetInstallableAppsUseCase getInstallableAppsUseCase,
                                CompleteOnboardingUseCase completeOnboardingUseCase,
                                AppExecutors appExecutors,
                                @ApplicationContext Context appContext) {
        this.getInstallableAppsUseCase = getInstallableAppsUseCase;
        this.completeOnboardingUseCase = completeOnboardingUseCase;
        this.appExecutors = appExecutors;
        this.appContext = appContext;
    }

    public LiveData<List<InstalledApp>> getInstallableApps() {
        return installableApps;
    }

    public LiveData<Set<String>> getSelectedPackageNames() {
        return selectedPackageNames;
    }

    public LiveData<Boolean> getUsageAccessGranted() {
        return usageAccessGranted;
    }

    public LiveData<Boolean> getAccessibilityGranted() {
        return accessibilityGranted;
    }

    public LiveData<Boolean> getOnboardingCompleted() {
        return onboardingCompleted;
    }

    public void loadInstallableApps() {
        appExecutors.diskIO().execute(() -> {
            List<InstalledApp> apps = getInstallableAppsUseCase.execute();
            appExecutors.mainThread().execute(() -> installableApps.setValue(apps));
        });
    }

    public void toggleAppSelection(@NonNull InstalledApp app) {
        Set<String> current = new HashSet<>(selectedPackageNames.getValue());
        if (!current.remove(app.getPackageName())) {
            current.add(app.getPackageName());
        }
        selectedPackageNames.setValue(current);
    }

    /** Called from each Fragment's onResume — the user grants permissions in
     *  a separate Settings screen, so state must be re-checked on return. */
    public void refreshPermissionState() {
        usageAccessGranted.setValue(PermissionHelper.isUsageAccessGranted(appContext));
        accessibilityGranted.setValue(PermissionHelper.isAccessibilityServiceEnabled(appContext));
    }

    public void completeOnboarding() {
        List<InstalledApp> allApps = installableApps.getValue();
        Set<String> selected = selectedPackageNames.getValue();
        if (allApps == null || selected == null) {
            return;
        }

        List<InstalledApp> toVault = new ArrayList<>();
        for (InstalledApp app : allApps) {
            if (selected.contains(app.getPackageName())) {
                toVault.add(app);
            }
        }

        appExecutors.diskIO().execute(() -> {
            completeOnboardingUseCase.execute(toVault);
            appExecutors.mainThread().execute(() -> onboardingCompleted.setValue(true));
        });
    }
}
