package com.neurofix.app.presentation.main;

import androidx.lifecycle.ViewModel;

import com.neurofix.app.domain.usecase.IsOnboardingCompleteUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Answers exactly one startup question: has onboarding already been
 * completed? Read once, synchronously, in the constructor — a
 * SharedPreferences read is safe on the main thread (unlike Room), and the
 * decision must be available immediately in MainActivity.onCreate(),
 * before the NavHost visibly settles, to avoid a flash of the wrong screen.
 * A plain boolean (not LiveData) is deliberate: this value is read exactly
 * once at startup and never needs to be observed for later changes within
 * the same Activity instance's life.
 */
@HiltViewModel
public class MainViewModel extends ViewModel {

    private final boolean onboardingComplete;

    @Inject
    public MainViewModel(IsOnboardingCompleteUseCase isOnboardingCompleteUseCase) {
        this.onboardingComplete = isOnboardingCompleteUseCase.execute();
    }

    public boolean isOnboardingComplete() {
        return onboardingComplete;
    }
}
