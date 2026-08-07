package com.neurofix.app.permissions;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.provider.Settings;
import android.text.TextUtils;

/**
 * Wraps the two Android permission checks Onboarding needs. Deliberately a
 * plain utility class, not routed through domain/Use Cases: reading OS
 * permission state is not a business rule, and the earlier attempt to force
 * everything through custom abstractions is exactly what we just removed.
 * Called directly from ViewModels, per the permissions/ package's stated
 * purpose.
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
}
