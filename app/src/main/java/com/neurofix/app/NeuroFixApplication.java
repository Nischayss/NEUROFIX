package com.neurofix.app;

import android.app.Application;

import com.neurofix.app.core.AppExecutors;
import com.neurofix.app.domain.usecase.SeedDefaultFocusModesUseCase;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

/**
 * Application entry point. Annotating with @HiltAndroidApp triggers Hilt's
 * code generation and creates the root DI container that every Activity,
 * Fragment, ViewModel, and Service in the app will draw dependencies from.
 *
 * Step 9: field-injects SeedDefaultFocusModesUseCase to insert the 4 preset
 * Focus Modes on first run only (the use case's own count check makes this
 * a no-op on every later launch). Run via AppExecutors.diskIO(), same
 * threading convention already used for Room writes triggered by app-level
 * (not UI-triggered) code elsewhere in this project.
 *
 * Otherwise intentionally minimal: no analytics, no crash reporting SDK,
 * no third-party initialization — consistent with Offline First / Privacy First.
 */
@HiltAndroidApp
public class NeuroFixApplication extends Application {

    @Inject
    SeedDefaultFocusModesUseCase seedDefaultFocusModesUseCase;

    @Inject
    AppExecutors appExecutors;

    @Override
    public void onCreate() {
        super.onCreate();
        appExecutors.diskIO().execute(() -> seedDefaultFocusModesUseCase.execute());
    }
}
