package com.neurofix.app.di;

import android.content.Context;

import androidx.room.Room;

import com.neurofix.app.database.NeuroFixDatabase;
import com.neurofix.app.database.dao.VaultedAppDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public static NeuroFixDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, NeuroFixDatabase.class, NeuroFixDatabase.DATABASE_NAME)
                // No fallbackToDestructiveMigration in production: every schema
                // change from here on must ship a real Migration so user data
                // (their Vault, their streaks) is never silently wiped.
                .build();
    }

    @Provides
    @Singleton
    public static VaultedAppDao provideVaultedAppDao(NeuroFixDatabase database) {
        return database.vaultedAppDao();
    }
}
