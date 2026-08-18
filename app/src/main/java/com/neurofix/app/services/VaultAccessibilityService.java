package com.neurofix.app.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.neurofix.app.domain.model.EnforcementMode;
import com.neurofix.app.domain.model.VaultedApp;
import com.neurofix.app.domain.usecase.GetEnforcementModeUseCase;
import com.neurofix.app.domain.usecase.ObserveVaultedAppsUseCase;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Real Vault Engine implementation.
 *
 * ENFORCEMENT MECHANISM — GLOBAL_ACTION_HOME, not startActivity():
 * Directly launching an Activity from an AccessibilityService's
 * onAccessibilityEvent() is a textbook Background Activity Launch (BAL) —
 * the service has no visible window, and per Android's own "Activity
 * security" documentation, AccessibilityService is not among the
 * conditions that exempt a background start. The OS drops the launch
 * silently (no exception on our side, only a system-logcat message), which
 * is exactly what earlier device testing showed: BLOCK_ACTIVITY_LAUNCHED
 * logged, VaultBlockActivity.onCreate() never called.
 *
 * performGlobalAction(GLOBAL_ACTION_HOME) is a fundamentally different,
 * BAL-exempt operation — it doesn't start an Activity at all, it's a core
 * AccessibilityService capability (used by every screen reader on every
 * OEM since API 16). This is the actual enforcement. The optional
 * explanation notification, and VaultBlockActivity reachable only via that
 * notification's PendingIntent (a documented, legitimate BAL exemption:
 * "activity started from a PendingIntent sent by the system, e.g. a
 * notification tap"), are secondary UX — never a dependency for
 * enforcement itself.
 *
 * ARCHITECTURE: no Room/business logic in this class — see
 * ObserveVaultedAppsUseCase (vault data) and GetEnforcementModeUseCase
 * (the one Settings toggle) for where that lives. Cache/debounce/
 * self-exclusion logic below is unchanged from the previously-verified
 * detection pipeline.
 */
@AndroidEntryPoint
public class VaultAccessibilityService extends AccessibilityService {

    // TEMP DIAGNOSTIC — REMOVE AFTER STEP 8 VERIFICATION
    private static final String DIAG_TAG = "NEUROFIX_VAULT_DIAG";

    @Inject
    ObserveVaultedAppsUseCase observeVaultedAppsUseCase;

    @Inject
    GetEnforcementModeUseCase getEnforcementModeUseCase;

    private VaultNotificationHelper notificationHelper;

    private LiveData<List<VaultedApp>> vaultedAppsLiveData;
    private final Observer<List<VaultedApp>> vaultedAppsObserver = this::onVaultedAppsChanged;

    private volatile Set<String> activeVaultedPackageNames = new HashSet<>();

    private String lastForegroundPackageName = null;
    private String defaultHomePackageName = null;

    // Fix for a narrow edge case (e.g. Screen Pinning): if GLOBAL_ACTION_HOME
    // is accepted but doesn't actually evict the app, a repeated identical-
    // package event would otherwise be debounced forever, permanently
    // ignoring an app that's still genuinely in the foreground. This lets
    // the SAME package be re-evaluated after a short cooldown, while still
    // collapsing the normal burst of duplicate events one real window
    // transition produces.
    //
    // 300ms (not a larger value like 1000ms): a real single-transition
    // event burst typically resolves in well under 100-200ms, so 300ms is
    // generous debounce coverage. Keeping it short also bounds a separate,
    // narrower risk: if the OS ever coalesces the intervening launcher
    // event on a very fast re-open (not guaranteed, but possible), a
    // longer cooldown would delay re-enforcement noticeably; a short one
    // keeps that delay imperceptible.
    private static final long RE_ENFORCEMENT_COOLDOWN_MILLIS = 300L;
    private long lastEnforcementAttemptMillis = 0L;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        // TEMP DIAGNOSTIC — REMOVE AFTER STEP 8 VERIFICATION
        Log.d(DIAG_TAG, "TEMP DIAGNOSTIC: SERVICE_CONNECTED");

        defaultHomePackageName = resolveDefaultHomePackageName();
        notificationHelper = new VaultNotificationHelper(this);

        // Defensive: if onServiceConnected() ever fires more than once
        // within the same process lifetime (a transient unbind/rebind not
        // involving full process death), remove any previous subscription
        // first — otherwise a second one would be created without the
        // first ever being cleaned up, leaking an observer.
        if (vaultedAppsLiveData != null) {
            vaultedAppsLiveData.removeObserver(vaultedAppsObserver);
        }

        // Registered here (not onCreate) because onServiceConnected() is
        // called every time the system (re)binds this service — including
        // after the hosting process is killed and restarted, or after
        // device reboot — so the cache is guaranteed fresh, never stale.
        vaultedAppsLiveData = observeVaultedAppsUseCase.execute();
        vaultedAppsLiveData.observeForever(vaultedAppsObserver);
    }

    private void onVaultedAppsChanged(List<VaultedApp> vaultedApps) {
        Set<String> active = new HashSet<>();
        if (vaultedApps != null) {
            for (VaultedApp app : vaultedApps) {
                if (app.isActive()) {
                    active.add(app.getPackageName());
                }
            }
        }
        activeVaultedPackageNames = active;
        // TEMP DIAGNOSTIC — REMOVE AFTER STEP 8 VERIFICATION
        Log.d(DIAG_TAG, "TEMP DIAGNOSTIC: CACHE_SIZE=" + active.size());
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return;
        }

        CharSequence packageNameSequence = event.getPackageName();
        if (packageNameSequence == null) {
            return;
        }
        String packageName = packageNameSequence.toString();
        // TEMP DIAGNOSTIC — REMOVE AFTER STEP 8 VERIFICATION
        Log.d(DIAG_TAG, "TEMP DIAGNOSTIC: EVENT_PACKAGE=" + packageName);

        // Debounce: only act when the foreground package actually changes —
        // a single app can fire multiple TYPE_WINDOW_STATE_CHANGED events
        // for internal window transitions while still being the same
        // foreground app. EXCEPTION: if this exact package was already
        // reported and a cooldown period has elapsed, allow it through
        // again — otherwise a vaulted app that GLOBAL_ACTION_HOME failed to
        // evict would be silently ignored forever, since every subsequent
        // event for it looks identical to the debounce.
        if (packageName.equals(lastForegroundPackageName)) {
            boolean cooldownElapsed = System.currentTimeMillis() - lastEnforcementAttemptMillis
                    >= RE_ENFORCEMENT_COOLDOWN_MILLIS;
            if (!cooldownElapsed) {
                return;
            }
        }
        lastForegroundPackageName = packageName;

        if (packageName.equals(getPackageName())) {
            return; // never block NeuroFix itself
        }
        if (packageName.equals(defaultHomePackageName)) {
            return; // never block the device's home/launcher, even if it were somehow vaulted
        }

        boolean isMatch = activeVaultedPackageNames.contains(packageName);
        // TEMP DIAGNOSTIC — REMOVE AFTER STEP 8 VERIFICATION
        Log.d(DIAG_TAG, "TEMP DIAGNOSTIC: MATCH=" + isMatch);

        if (isMatch) {
            enforceVault();
        }
    }

    private void enforceVault() {
        lastEnforcementAttemptMillis = System.currentTimeMillis();

        // The enforcement action. This alone is the entire security
        // boundary — everything below it is optional explanation UX.
        boolean sentHome = performGlobalAction(GLOBAL_ACTION_HOME);
        // TEMP DIAGNOSTIC — REMOVE AFTER STEP 8 VERIFICATION
        Log.d(DIAG_TAG, "TEMP DIAGNOSTIC: GLOBAL_ACTION_HOME=" + sentHome);

        EnforcementMode mode = getEnforcementModeUseCase.execute();
        // TEMP DIAGNOSTIC — REMOVE AFTER STEP 8 VERIFICATION
        Log.d(DIAG_TAG, "TEMP DIAGNOSTIC: ENFORCEMENT_MODE=" + mode);

        if (mode == EnforcementMode.RETURN_HOME_WITH_NOTIFICATION) {
            boolean posted = notificationHelper.showBlockedNotification(lastForegroundPackageName);
            // TEMP DIAGNOSTIC — REMOVE AFTER STEP 8 VERIFICATION
            Log.d(DIAG_TAG, posted
                    ? "TEMP DIAGNOSTIC: NOTIFICATION_POSTED"
                    : "TEMP DIAGNOSTIC: NOTIFICATION_PERMISSION_DENIED");
        }
    }

    /**
     * Resolves the device's current default launcher package, so it's never
     * treated as blockable even in the edge case where it was vaulted (it
     * does expose ACTION_MAIN/CATEGORY_LAUNCHER like any other app, so
     * InstalledAppRepository's existing filtering wouldn't exclude it).
     */
    private String resolveDefaultHomePackageName() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = getPackageManager().resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo != null && resolveInfo.activityInfo != null
                ? resolveInfo.activityInfo.packageName
                : null;
    }

    @Override
    public void onInterrupt() {
        // No ongoing operation to cancel — each event is handled synchronously and independently.
    }

    @Override
    public void onDestroy() {
        if (vaultedAppsLiveData != null) {
            vaultedAppsLiveData.removeObserver(vaultedAppsObserver);
        }
        super.onDestroy();
    }
}
