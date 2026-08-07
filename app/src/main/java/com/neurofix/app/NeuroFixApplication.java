package com.neurofix.app;

import android.app.Application;

import dagger.hilt.android.HiltAndroidApp;

/**
 * Application entry point. Annotating with @HiltAndroidApp triggers Hilt's
 * code generation and creates the root DI container that every Activity,
 * Fragment, ViewModel, and Service in the app will draw dependencies from.
 *
 * Intentionally empty otherwise: no analytics, no crash reporting SDK,
 * no third-party initialization — consistent with Offline First / Privacy First.
 */
@HiltAndroidApp
public class NeuroFixApplication extends Application {
}
