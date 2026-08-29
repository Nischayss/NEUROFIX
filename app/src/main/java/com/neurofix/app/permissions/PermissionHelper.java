package com.neurofix.app.permissions;

import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.Process;
import android.provider.Settings;
import android.text.TextUtils;

import java.util.Locale;

/**
 * Wraps the Android permission checks Onboarding and Settings need.
 * Deliberately a plain utility class, not routed through domain/Use Cases:
 * reading OS permission state is not a business rule, and the earlier
 * attempt to force everything through custom abstractions is exactly what
 * we just removed. Called directly from ViewModels, per the permissions/
 * package's stated purpose.
 */
public final class PermissionHelper {

    private PermissionHelper() {
    }

    public static boolean isUsageAccessGranted(Context context) {
        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.getPackageName()
        );
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public static Intent buildUsageAccessSettingsIntent() {
        return new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
    }

    /**
     * Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES is a colon-separated
     * string of "package/ServiceClass" component names — there is no direct
     * "is my service enabled" API, so this is the only supported check.
     */
    public static boolean isAccessibilityServiceEnabled(Context context) {
        String expectedComponent = context.getPackageName() + "/"
                + "com.neurofix.app.services.VaultAccessibilityService";

        String enabledServices = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );

        if (TextUtils.isEmpty(enabledServices)) {
            return false;
        }

        for (String component : enabledServices.split(":")) {
            if (component.equalsIgnoreCase(expectedComponent)) {
                return true;
            }
        }
        return false;
    }

    public static Intent buildAccessibilitySettingsIntent() {
        return new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
    }

    // --- Reliability (Step 8 continuation): none of these gate enforcement
    // itself — GLOBAL_ACTION_HOME never depends on them. They only reduce
    // how often the OS/OEM kills the hosting process after NeuroFix is
    // cleared from Recents, which is a background-process-lifetime concern,
    // not a permission the vault engine needs to function.

    public static boolean isIgnoringBatteryOptimizations(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return powerManager != null
                && powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
    }

    /**
     * Standard public Android API (Settings.ACTION_REQUEST_IGNORE_BATTERY_
     * OPTIMIZATIONS) — requires the normal, auto-granted
     * REQUEST_IGNORE_BATTERY_OPTIMIZATIONS manifest permission, no runtime
     * prompt beyond the system dialog this Intent itself shows.
     */
    public static Intent buildIgnoreBatteryOptimizationsIntent(Context context) {
        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        return intent;
    }

    /**
     * True on manufacturers documented to apply their own aggressive,
     * package-scoped process killing beyond stock Android's — Xiaomi/
     * Redmi/POCO all ship MIUI/HyperOS, where standard battery-optimization
     * exemption alone is often not enough; Autostart also needs to be
     * enabled. This is a device-model check only, not a permission or a
     * behavior change on other manufacturers.
     */
    public static boolean isKnownAggressiveOem() {
        String manufacturer = Build.MANUFACTURER == null
                ? "" : Build.MANUFACTURER.toLowerCase(Locale.ROOT);
        return manufacturer.contains("xiaomi")
                || manufacturer.contains("redmi")
                || manufacturer.contains("poco");
    }

    /**
     * MIUI/HyperOS's Autostart manager has no public Settings action —
     * this is an explicit component name, not a documented API, and is not
     * guaranteed stable across MIUI versions. Always resolve-checked before
     * use; explicit-component intents are exempt from Android 11+ package
     * visibility filtering, so no <queries> manifest entry is needed. Falls
     * back to this app's own details screen if it can't be resolved, so the
     * user still lands somewhere useful rather than at a crash.
     */
    public static Intent buildAutostartSettingsIntent(Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"));
        PackageManager packageManager = context.getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            return intent;
        }
        return buildAppDetailsSettingsIntent(context);
    }

    public static Intent buildAppDetailsSettingsIntent(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        return intent;
    }
}