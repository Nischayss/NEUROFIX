package com.neurofix.app.presentation.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.neurofix.app.domain.usecase.GetVaultedAppCountUseCase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Dashboard is informational only in this step — no usage tracking beyond
 * the Vault Engine's own enforcement. Vaulted App Count is now a LIVE value
 * (fixed from a one-shot load — see GetVaultedAppCountUseCase for the bug
 * this corrects), sourced directly from Room via the repository's LiveData.
 * AppExecutors is no longer needed here since there's no manual background
 * load to perform — Room's LiveData already delivers on the main thread.
 * Every other field remains a static placeholder, still exposed as LiveData
 * so the View only ever observes and never hardcodes display values.
 */
@HiltViewModel
public class DashboardViewModel extends ViewModel {

    private final LiveData<Integer> vaultedAppCount;
    private final MutableLiveData<String> currentDate = new MutableLiveData<>();
    private final MutableLiveData<String> focusStatus = new MutableLiveData<>("Ready");
    private final MutableLiveData<String> todaysFocus = new MutableLiveData<>("--");
    private final MutableLiveData<String> currentStreak = new MutableLiveData<>("0");
    private final MutableLiveData<String> longestStreak = new MutableLiveData<>("0");
    private final MutableLiveData<String> emergencyBudget = new MutableLiveData<>("Not Configured");

    @Inject
    public DashboardViewModel(GetVaultedAppCountUseCase getVaultedAppCountUseCase) {
        this.vaultedAppCount = getVaultedAppCountUseCase.execute();
        currentDate.setValue(new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(new Date()));
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
}
