package com.neurofix.app.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.view.accessibility.AccessibilityEvent;

import androidx.core.app.ServiceCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.neurofix.app.domain.model.EnforcementMode;
import com.neurofix.app.domain.model.VaultedApp;
import com.neurofix.app.domain.usecase.GetEnforcementModeUseCase;
import com.neurofix.app.domain.usecase.ObserveActiveModePackageNamesUseCase;
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
 *
 * FOREGROUND SERVICE (Step 8 continuation): startForeground() is called in
 * onServiceConnected() with a mandatory, always-visible, non-dismissible
 * notification — this is the standard, documented Android API for a
 * long-lived background service, the same one music/navigation/fitness
 * apps use. It is not SYSTEM_ALERT_WINDOW, not a watchdog, and grants no
 * elevated privilege: it only changes this process's OOM priority and
 * makes it eligible for MIUI's "Lock this app" Recents protection, which
 * MIUI does not offer to plain bound services. It does not, and cannot,
 * prevent the OS from killing the process outright if the user explicitly
 * force-stops the app from system Settings — that remains a genuine,
 * un-closeable limit given the no-root/no-Shizuku/no-hack constraint.
 *
 * DIAGNOSTIC LOGGING REMOVED: the NEUROFIX_VAULT_DIAG Log.d() calls used to
 * verify Step 8's detection-to-enforcement pipeline (visible in the earlier
 * logcat captures) have been removed now that the pipeline is verified
 * working. Nothing in enforcement itself ever depended on them.
 *
 * FOCUS MODES (Step 9) — UNION, not replace: while a Focus Mode is active,
 * its apps are enforced IN ADDITION TO the base Vault, never instead of it.
 * This was an explicit user decision, made for a security reason: Union
 * means activating a mode can only ever ADD restrictions, never silently
 * remove one the base Vault was supposed to always catch. Two independent
 * LiveData caches (activeVaultedPackageNames, activeFocusModePackageNames)
 * are checked with OR in onAccessibilityEvent — see enforceVault() call
 * site below.
 */
@AndroidEntryPoint
public class VaultAccessibilityService extends AccessibilityService {

    @Inject
    ObserveVaultedAppsUseCase observeVaultedAppsUseCase;

    @Inject
    ObserveActiveModePackageNamesUseCase observeActiveModePackageNamesUseCase;

    @Inject
    GetEnforcementModeUseCase getEnforcementModeUseCase;

    private VaultNotificationHelper notificationHelper;

    private LiveData<List<VaultedApp>> vaultedAppsLiveData;
    private final Observer<List<VaultedApp>> vaultedAppsObserver = this::onVaultedAppsChanged;

    private LiveData<List<String>> activeModePackageNamesLiveData;
    private final Observer<List<String>> activeModePackageNamesObserver = this::onActiveModePackageNamesChanged;

    private volatile Set<String> activeVaultedPackageNames = new HashSet<>();
    private volatile Set<String> activeFocusModePackageNames = new HashSet<>();

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

        defaultHomePackageName = resolveDefaultHomePackageName();
        notificationHelper = new VaultNotificationHelper(this);

        // Promote to foreground service — see class doc for why. Uses
        // ServiceCompat so the FOREGROUND_SERVICE_TYPE_SPECIAL_USE argument
        // is only actually required/passed on API levels that need it,
        // without an SDK-version branch here.
        ServiceCompat.startForeground(
                this,
                VaultNotificationHelper.FOREGROUND_NOTIFICATION_ID,
                notificationHelper.buildForegroundNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        );

        // Defensive: if onServiceConnected() ever fires more than once
        // within the same process lifetime (a transient unbind/rebind not
        // involving full process death), remove any previous subscription
        // first — otherwise a second one would be created without the
        // first ever being cleaned up, leaking an observer.
        if (vaultedAppsLiveData != null) {
            vaultedAppsLiveData.removeObserver(vaultedAppsObserver);
        }
        if (activeModePackageNamesLiveData != null) {
            activeModePackageNamesLiveData.removeObserver(activeModePackageNamesObserver);
        }

        // Registered here (not onCreate) because onServiceConnected() is
        // called every time the system (re)binds this service — including
        // after the hosting process is killed and restarted, or after
        // device reboot — so the cache is guaranteed fresh, never stale.
        vaultedAppsLiveData = observeVaultedAppsUseCase.execute();
        vaultedAppsLiveData.observeForever(vaultedAppsObserver);

        activeModePackageNamesLiveData = observeActiveModePackageNamesUseCase.execute();
        activeModePackageNamesLiveData.observeForever(activeModePackageNamesObserver);
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
    }

    private void onActiveModePackageNamesChanged(List<String> packageNames) {
        activeFocusModePackageNames = packageNames != null
                ? new HashSet<>(packageNames)
                : new HashSet<>();
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

        // UNION rule (Step 9): an app is blocked if it's in the base Vault
        // OR in the currently active Focus Mode's list — either is
        // sufficient, matching the explicit "additive only" security
        // decision documented in the class doc above.
        if (activeVaultedPackageNames.contains(packageName)
                || activeFocusModePackageNames.contains(packageName)) {
            enforceVault();
        }
    }

    private void enforceVault() {
        lastEnforcementAttemptMillis = System.currentTimeMillis();

        // The enforcement action. This alone is the entire security
        // boundary — everything below it is optional explanation UX.
        performGlobalAction(GLOBAL_ACTION_HOME);

        EnforcementMode mode = getEnforcementModeUseCase.execute();

        if (mode == EnforcementMode.RETURN_HOME_WITH_NOTIFICATION) {
            notificationHelper.showBlockedNotification(lastForegroundPackageName);
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
        if (activeModePackageNamesLiveData != null) {
            activeModePackageNamesLiveData.removeObserver(activeModePackageNamesObserver);
        }
        // Best-effort cleanup on a clean shutdown. Not reachable on the
        // MIUI kill path this whole feature targets — that's a hard
        // process kill, not a graceful onDestroy() — so this is here for
        // correctness on normal service teardown, not as the reliability fix.
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
        super.onDestroy();
    }
}
