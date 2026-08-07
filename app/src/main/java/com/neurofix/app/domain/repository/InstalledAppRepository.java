package com.neurofix.app.domain.repository;

import com.neurofix.app.domain.model.InstalledApp;

import java.util.List;

/**
 * Domain contract for reading the device's launchable apps. Implemented by
 * data/repository/InstalledAppRepositoryImpl using PackageManager — the
 * domain layer knows nothing about PackageManager or Intent.
 */
public interface InstalledAppRepository {
    List<InstalledApp> getLaunchableApps();
}
