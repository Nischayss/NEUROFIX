package com.neurofix.app.presentation.vault;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.neurofix.app.core.AppExecutors;
import com.neurofix.app.domain.model.VaultedApp;
import com.neurofix.app.domain.usecase.ObserveVaultedAppsUseCase;
import com.neurofix.app.domain.usecase.RemoveVaultedAppUseCase;
import com.neurofix.app.domain.usecase.SetVaultedAppActiveUseCase;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Room's LiveData auto-invalidates and re-queries whenever the vaulted_apps
 * table changes, so this ViewModel never manually refreshes the list after
 * setActive()/remove() — Room handles that. Writes must not run on the main
 * thread, so they go through AppExecutors.diskIO(), same convention as
 * every other ViewModel in this project.
 */
@HiltViewModel
public class ManageVaultViewModel extends ViewModel {

    private final SetVaultedAppActiveUseCase setVaultedAppActiveUseCase;
    private final RemoveVaultedAppUseCase removeVaultedAppUseCase;
    private final AppExecutors appExecutors;

    private final LiveData<List<VaultedApp>> vaultedApps;

    @Inject
    public ManageVaultViewModel(ObserveVaultedAppsUseCase observeVaultedAppsUseCase,
                                 SetVaultedAppActiveUseCase setVaultedAppActiveUseCase,
                                 RemoveVaultedAppUseCase removeVaultedAppUseCase,
                                 AppExecutors appExecutors) {
        this.setVaultedAppActiveUseCase = setVaultedAppActiveUseCase;
        this.removeVaultedAppUseCase = removeVaultedAppUseCase;
        this.appExecutors = appExecutors;
        this.vaultedApps = observeVaultedAppsUseCase.execute();
    }

    public LiveData<List<VaultedApp>> getVaultedApps() {
        return vaultedApps;
    }

    public void setActive(VaultedApp app, boolean isActive) {
        appExecutors.diskIO().execute(() ->
                setVaultedAppActiveUseCase.execute(app.getPackageName(), isActive));
    }

    public void remove(VaultedApp app) {
        appExecutors.diskIO().execute(() ->
                removeVaultedAppUseCase.execute(app.getPackageName()));
    }
}
