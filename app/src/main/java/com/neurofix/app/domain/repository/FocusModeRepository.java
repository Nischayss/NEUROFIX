package com.neurofix.app.domain.repository;

import androidx.lifecycle.LiveData;

import com.neurofix.app.domain.model.FocusMode;
import com.neurofix.app.domain.model.FocusModeApp;

import java.util.List;

/**
 * Domain contract for Focus Modes. Implemented by
 * data/repository/FocusModeRepositoryImpl, which is Room-only (FocusModeDao)
 * — no SharedPreferences involved here, unlike VaultedAppRepositoryImpl.
 */
public interface FocusModeRepository {

    LiveData<List<FocusMode>> observeAllFocusModes();

    /**
     * Package names belonging to whichever mode is currently active, or an
     * empty list if none is. This is the stream the enforcement engine
     * will consume (wired in a later step) to union with the base Vault.
     */
    LiveData<List<String>> observeActiveModePackageNames();

    LiveData<List<FocusModeApp>> observeAppsForMode(long modeId);

    /** One-shot package name list for the "add apps" candidate filter — mirrors VaultedAppRepository.getVaultedPackageNames(). */
    List<String> getPackageNamesForMode(long modeId);

    int getFocusModeCount();

    /**
     * Creates a mode. isPreset=true is only ever used by
     * SeedDefaultFocusModesUseCase on first run — user-created modes from
     * the UI always go through createCustomMode below instead.
     */
    long createMode(String name, boolean isPreset);

    long createCustomMode(String name);

    /**
     * @throws IllegalStateException if mode.isPreset() — presets are never
     * user-deletable, enforced here (not just in the UI layer) as a real
     * data-integrity guard, not a UI-only convenience check.
     */
    void deleteCustomMode(FocusMode mode);

    void addAppToMode(long modeId, String packageName, String displayName);

    void removeAppFromMode(long modeId, String packageName);

    /**
     * Activates exactly one mode. Pass null to deactivate all (no mode
     * active) — matches FocusModeDao.setActiveMode's contract exactly.
     */
    void setActiveMode(Long modeId);
}
