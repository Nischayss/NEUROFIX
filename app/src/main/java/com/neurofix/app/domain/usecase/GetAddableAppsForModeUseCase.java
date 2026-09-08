package com.neurofix.app.domain.usecase;

import com.neurofix.app.domain.model.InstalledApp;
import com.neurofix.app.domain.repository.FocusModeRepository;
import com.neurofix.app.domain.repository.InstalledAppRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Installed apps NOT already in the given Focus Mode's own list. Mirrors
 * GetAddableAppsUseCase exactly, but filters against a specific mode's app
 * list rather than the base Vault — deliberately NOT excluding apps that
 * happen to be in the base Vault or in a different mode, since each mode's
 * list is independent per the project's explicit design.
 */
public class GetAddableAppsForModeUseCase {

    private final InstalledAppRepository installedAppRepository;
    private final FocusModeRepository focusModeRepository;

    @Inject
    public GetAddableAppsForModeUseCase(InstalledAppRepository installedAppRepository,
                                         FocusModeRepository focusModeRepository) {
        this.installedAppRepository = installedAppRepository;
        this.focusModeRepository = focusModeRepository;
    }

    public List<InstalledApp> execute(long modeId) {
        List<InstalledApp> allApps = installedAppRepository.getLaunchableApps();
        List<String> alreadyInMode = focusModeRepository.getPackageNamesForMode(modeId);

        List<InstalledApp> addable = new ArrayList<>();
        for (InstalledApp app : allApps) {
            if (!alreadyInMode.contains(app.getPackageName())) {
                addable.add(app);
            }
        }
        return addable;
    }
}
