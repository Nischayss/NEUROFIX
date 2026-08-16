package com.neurofix.app.domain.model;

/**
 * How VaultAccessibilityService responds when it detects an active vaulted
 * app in the foreground. Both modes always perform the actual enforcement
 * (GLOBAL_ACTION_HOME) — the difference is purely whether an explanatory
 * notification is also shown. Notification permission state never affects
 * whether enforcement happens, only whether the explanation is visible.
 */
public enum EnforcementMode {
    RETURN_HOME,
    RETURN_HOME_WITH_NOTIFICATION
}
