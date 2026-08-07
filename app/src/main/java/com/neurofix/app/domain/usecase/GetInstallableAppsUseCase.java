package com.neurofix.app.domain.usecase;

import com.neurofix.app.domain.model.InstalledApp;
import com.neurofix.app.domain.repository.InstalledAppRepository;

import java.util.List;

import javax.inject.Inject;

/**
 * Fetches the list of apps the user can choose to Vault. Currently a thin
 * pass-through to the repository — kept as its own Use Case rather than
 * calling the repository straight from the ViewModel because "which apps
 * are eligible to be vaulted" is a business rule (e.g. excluding NeuroFix
 * itself) that belongs in domain, not presentation, even though today it's
 * simple.
 */
public class GetInstallableAppsUseCase {

    private final InstalledAppRepository repository;

    @Inject
    public GetInstallableAppsUseCase(InstalledAppRepository repository) {
        this.repository = repository;
    }

    public List<InstalledApp> execute() {
        return repository.getLaunchableApps();
    }
}
