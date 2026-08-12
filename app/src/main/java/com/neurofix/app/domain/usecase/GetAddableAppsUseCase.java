package com.neurofix.app.domain.usecase;

import com.neurofix.app.domain.model.InstalledApp;
import com.neurofix.app.domain.repository.InstalledAppRepository;
import com.neurofix.app.domain.repository.VaultedAppRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Returns installed apps that are NOT already in the Vault (active or
 * inactive) — the candidate list for the Add Apps screen. This is a real
 * filtering rule spanning two repositories, which is exactly the kind of
 * logic that belongs in domain rather than being duplicated in a ViewModel.
 *
 * The database's packageName primary key remains the final duplicate
 * safeguard (via REPLACE in vaultApps) — this filter only improves the UX
 * by not offering already-vaulted apps for re-selection in the first place.
 */
public class GetAddableAppsUseCase {

    private final InstalledAppRepository installedAppRepository;
    private final VaultedAppRepository vaultedAppRepository;

    @Inject
    public GetAddableAppsUseCase(InstalledAppRepository installedAppRepository,
                                  VaultedAppRepository vaultedAppRepository) {
        this.installedAppRepository = installedAppRepository;
        this.vaultedAppRepository = vaultedAppRepository;
    }

    public List<InstalledApp> execute() {
        List<InstalledApp> allApps = installedAppRepository.getLaunchableApps();
        List<String> vaultedPackageNames = vaultedAppRepository.getVaultedPackageNames();

        List<InstalledApp> addable = new ArrayList<>();
        for (InstalledApp app : allApps) {
            if (!vaultedPackageNames.contains(app.getPackageName())) {
                addable.add(app);
            }
        }
        return addable;
    }
}
