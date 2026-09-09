package com.neurofix.app.presentation.settings;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.neurofix.app.core.AppExecutors;
import com.neurofix.app.domain.model.EnforcementMode;
import com.neurofix.app.domain.usecase.GetEnforcementModeUseCase;
import com.neurofix.app.domain.usecase.GetFocusModeUnionEnabledUseCase;
import com.neurofix.app.domain.usecase.GetReliabilityNotificationEnabledUseCase;
import com.neurofix.app.domain.usecase.SetEnforcementModeUseCase;
import com.neurofix.app.domain.usecase.SetFocusModeUnionEnabledUseCase;
import com.neurofix.app.domain.usecase.SetReliabilityNotificationEnabledUseCase;
import com.neurofix.app.permissions.PermissionHelper;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Exposes the Step 8 enforcement-mode toggle, plus read-only status for the
 * two permissions Vault enforcement depends on (Accessibility, Usage
 * Access) — reusing the exact same PermissionHelper checks Onboarding
 * already uses, not a second permission-management system.
 */
@HiltViewModel
public class SettingsViewModel extends ViewModel {

       private final GetEnforcementModeUseCase getEnforcementModeUseCase;
    private final SetEnforcementModeUseCase setEnforcementModeUseCase;
    private final GetReliabilityNotificationEnabledUseCase getReliabilityNotificationEnabledUseCase;
    private final SetReliabilityNotificationEnabledUseCase setReliabilityNotificationEnabledUseCase;
    private final GetFocusModeUnionEnabledUseCase getFocusModeUnionEnabledUseCase;
    private final SetFocusModeUnionEnabledUseCase setFocusModeUnionEnabledUseCase;
    private final AppExecutors appExecutors;
    private final Context appContext;

    private final MutableLiveData<EnforcementMode> enforcementMode = new MutableLiveData<>();
    private final MutableLiveData<Boolean> accessibilityEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> usageAccessGranted = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> batteryOptimizationIgnored = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> reliabilityNotificationEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> focusModeUnionEnabled = new MutableLiveData<>();

    @Inject
    public SettingsViewModel(GetEnforcementModeUseCase getEnforcementModeUseCase,
                              SetEnforcementModeUseCase setEnforcementModeUseCase,
                              GetReliabilityNotificationEnabledUseCase getReliabilityNotificationEnabledUseCase,
                              SetReliabilityNotificationEnabledUseCase setReliabilityNotificationEnabledUseCase,
                              GetFocusModeUnionEnabledUseCase getFocusModeUnionEnabledUseCase,
                              SetFocusModeUnionEnabledUseCase setFocusModeUnionEnabledUseCase,
                              AppExecutors appExecutors,
                              @ApplicationContext Context appContext) {
        this.getEnforcementModeUseCase = getEnforcementModeUseCase;
        this.setEnforcementModeUseCase = setEnforcementModeUseCase;
        this.getReliabilityNotificationEnabledUseCase = getReliabilityNotificationEnabledUseCase;
        this.setReliabilityNotificationEnabledUseCase = setReliabilityNotificationEnabledUseCase;
        this.getFocusModeUnionEnabledUseCase = getFocusModeUnionEnabledUseCase;
        this.setFocusModeUnionEnabledUseCase = setFocusModeUnionEnabledUseCase;
        this.appExecutors = appExecutors;
        this.appContext = appContext;
        enforcementMode.setValue(getEnforcementModeUseCase.execute());
        reliabilityNotificationEnabled.setValue(getReliabilityNotificationEnabledUseCase.execute());
        focusModeUnionEnabled.setValue(getFocusModeUnionEnabledUseCase.execute());
        refreshPermissionStatus();
    }

    public LiveData<EnforcementMode> getEnforcementMode() {
        return enforcementMode;
    }

    public LiveData<Boolean> getAccessibilityEnabled() {
        return accessibilityEnabled;
    }

    public LiveData<Boolean> getUsageAccessGranted() {
        return usageAccessGranted;
    }

    public LiveData<Boolean> getBatteryOptimizationIgnored() {
        return batteryOptimizationIgnored;
    }

    public LiveData<Boolean> getReliabilityNotificationEnabled() {
        return reliabilityNotificationEnabled;
    }

    public void setReliabilityNotificationEnabled(boolean enabled) {
        reliabilityNotificationEnabled.setValue(enabled);
        appExecutors.diskIO().execute(() -> setReliabilityNotificationEnabledUseCase.execute(enabled));
    }

    public LiveData<Boolean> getFocusModeUnionEnabled() {
        return focusModeUnionEnabled;
    }

    public void setFocusModeUnionEnabled(boolean enabled) {
        focusModeUnionEnabled.setValue(enabled);
        appExecutors.diskIO().execute(() -> setFocusModeUnionEnabledUseCase.execute(enabled));
    }

    public void setEnforcementMode(EnforcementMode mode) {
        enforcementMode.setValue(mode);
        appExecutors.diskIO().execute(() -> setEnforcementModeUseCase.execute(mode));
    }

    /**
     * Re-reads the persisted mode directly from the repository (not the
     * cached LiveData value) and updates it. Called from the Fragment's
     * onResume() so the UI can never drift from the actual persisted
     * SharedPreferences value, regardless of what caused any earlier
     * mismatch — this is a correctness guarantee, not a guess at the cause.
     */
    public EnforcementMode refreshEnforcementMode() {
        EnforcementMode current = getEnforcementModeUseCase.execute();
        enforcementMode.setValue(current);
        return current;
    }

    /**
     * The user may grant/revoke Accessibility or Usage Access at any time
     * outside NeuroFix (system Settings). This never assumes a state —
     * it re-checks the real OS state every time it's called.
     */
    public void refreshPermissionStatus() {
        accessibilityEnabled.setValue(PermissionHelper.isAccessibilityServiceEnabled(appContext));
        usageAccessGranted.setValue(PermissionHelper.isUsageAccessGranted(appContext));
        batteryOptimizationIgnored.setValue(PermissionHelper.isIgnoringBatteryOptimizations(appContext));
    }
}
