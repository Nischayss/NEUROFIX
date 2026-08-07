package com.neurofix.app.domain.model;

/**
 * Plain domain representation of an installed launchable app. Deliberately
 * does not carry an icon (Drawable is a Bitmap/PackageManager concept) —
 * the View re-resolves the icon from packageName via PackageManager when
 * rendering, keeping this model framework-free.
 */
public class InstalledApp {

    private final String packageName;
    private final String displayName;

    public InstalledApp(String packageName, String displayName) {
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
