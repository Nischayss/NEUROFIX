package com.neurofix.app.presentation.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.neurofix.app.core.AppExecutors;
import com.neurofix.app.domain.usecase.GetVaultedAppCountUseCase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Dashboard is informational only in this step — no Vault Engine, no usage
 * tracking. Vaulted App Count is the one real value, loaded from Room via
 * GetVaultedAppCountUseCase. Every other field is a static placeholder,
 * still exposed as LiveData so the View only ever observes and never
 * hardcodes or computes display values — the same rule applies whether a
 * value is real or a placeholder today.
 */
@HiltViewModel
public class DashboardViewModel extends ViewModel {

    private final GetVaultedAppCountUseCase getVaultedAppCountUseCase;
    private final AppExecutors appExecutors;

    private final MutableLiveData<Integer> vaultedAppCount = new MutableLiveData<>(0);
    private final MutableLiveData<String> currentDate = new MutableLiveData<>();
    private final MutableLiveData<String> focusStatus = new MutableLiveData<>("Ready");
    private final MutableLiveData<String> todaysFocus = new MutableLiveData<>("--");
    private final MutableLiveData<String> currentStreak = new MutableLiveData<>("0");
    private final MutableLiveData<String> longestStreak = new MutableLiveData<>("0");
    private final MutableLiveData<String> emergencyBudget = new MutableLiveData<>("Not Configured");

    @Inject
    public DashboardViewModel(GetVaultedAppCountUseCase getVaultedAppCountUseCase,
                               AppExecutors appExecutors) {
        this.getVaultedAppCountUseCase = getVaultedAppCountUseCase;
        this.appExecutors = appExecutors;
        currentDate.setValue(new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(new Date()));
        loadVaultedAppCount();
    }

    public LiveData<Integer> getVaultedAppCount() {
        return vaultedAppCount;
    }

    public LiveData<String> getCurrentDate() {
        return currentDate;
    }

    public LiveData<String> getFocusStatus() {
        return focusStatus;
    }

    public LiveData<String> getTodaysFocus() {
        return todaysFocus;
    }

    public LiveData<String> getCurrentStreak() {
        return currentStreak;
    }

    public LiveData<String> getLongestStreak() {
        return longestStreak;
    }

    public LiveData<String> getEmergencyBudget() {
        return emergencyBudget;
    }

    private void loadVaultedAppCount() {
        appExecutors.diskIO().execute(() -> {
            int count = getVaultedAppCountUseCase.execute();
            appExecutors.mainThread().execute(() -> vaultedAppCount.setValue(count));
        });
    }
}
