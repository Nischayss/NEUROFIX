package com.neurofix.app.di;

import com.neurofix.app.data.repository.EnforcementSettingsRepositoryImpl;
import com.neurofix.app.data.repository.InstalledAppRepositoryImpl;
import com.neurofix.app.data.repository.VaultedAppRepositoryImpl;
import com.neurofix.app.domain.repository.EnforcementSettingsRepository;
import com.neurofix.app.domain.repository.InstalledAppRepository;
import com.neurofix.app.domain.repository.VaultedAppRepository;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule {

    @Binds
    public abstract InstalledAppRepository bindInstalledAppRepository(InstalledAppRepositoryImpl impl);

    @Binds
    public abstract VaultedAppRepository bindVaultedAppRepository(VaultedAppRepositoryImpl impl);

    @Binds
    public abstract EnforcementSettingsRepository bindEnforcementSettingsRepository(EnforcementSettingsRepositoryImpl impl);
}
