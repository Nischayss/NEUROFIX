package com.neurofix.app.domain.model;

/**
 * Plain domain representation of an app currently in the Vault (active or
 * inactive). Distinct from VaultedAppEntity (a Room class, data layer) and
 * from InstalledApp (has no vaulted/active concept) — same separation
 * pattern already used elsewhere in this codebase. Carries no icon: icons
 * are resolved from packageName by the UI layer, same as InstalledApp.
 */
public class VaultedApp {

    private final String packageName;
    private final String displayName;
    private final boolean active;

    public VaultedApp(String packageName, String displayName, boolean active) {
        this.packageName = packageName;
        this.displayName = displayName;
        this.active = active;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return active;
    }
}
