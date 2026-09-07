package com.neurofix.app.domain.model;

/**
 * Plain domain representation of one app belonging to a specific Focus
 * Mode. Distinct from VaultedApp — a mode's app list is independent from
 * the Step 7 base Vault, per the project's explicit design choice, so this
 * is intentionally its own type rather than reusing VaultedApp.
 */
public class FocusModeApp {

    private final String packageName;
    private final String displayName;

    public FocusModeApp(String packageName, String displayName) {
        this.packageName = packageName;
        this.displayName = displayName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
