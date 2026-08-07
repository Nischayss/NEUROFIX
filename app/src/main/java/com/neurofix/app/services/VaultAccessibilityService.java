package com.neurofix.app.services;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

/**
 * Intentionally empty. This class exists solely so the Accessibility
 * settings screen has a real, declared service to grant permission to —
 * Android has no generic "accessibility" permission separate from a
 * specific service.
 *
 * Foreground-app monitoring logic (evaluating focus rules, showing the
 * Vault Screen) is Step 7 (Vault Engine) and does not belong here yet.
 */
public class VaultAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Implemented in Step 7.
    }

    @Override
    public void onInterrupt() {
        // Implemented in Step 7.
    }
}
