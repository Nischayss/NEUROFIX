package com.neurofix.app.di;

import android.content.Context;

import com.neurofix.app.data.local.OnboardingPreferencesDataStore;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

/**
 * Constructs OnboardingPreferencesDataStore explicitly via @Provides rather
 * than letting Dagger inspect its constructor (@Inject on a Kotlin class
 * would require the kapt annotation-processing pipeline in addition to the
 * existing Java annotationProcessor one). Keeping this a plain Java factory
 * method avoids that extra build complexity entirely.
 */
@Module
@InstallIn(SingletonComponent.class)
public class PreferencesModule {

    @Provides
    @Singleton
    public static OnboardingPreferencesDataStore provideOnboardingPreferencesDataStore(
            @ApplicationContext Context context) {
        return new OnboardingPreferencesDataStore(context);
    }
}
