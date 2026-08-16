package com.neurofix.app.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.neurofix.app.R;
import com.neurofix.app.presentation.vault.VaultBlockActivity;

/**
 * Posts the optional Mode 2 explanation notification. Deliberately NOT the
 * enforcement mechanism — VaultAccessibilityService always performs
 * GLOBAL_ACTION_HOME regardless of whether this succeeds, is skipped due to
 * denied permission, or throws. A single fixed notification ID is reused
 * (not a new ID per event) so repeated triggers UPDATE the existing
 * notification instead of stacking duplicates.
 */
public class VaultNotificationHelper {

    private static final String CHANNEL_ID = "neurofix_vault_enforcement";
    private static final int NOTIFICATION_ID = 1001;

    private final Context context;

    public VaultNotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        createChannelIfNeeded();
    }

    /**
     * Returns true if the notification was actually posted, false if
     * skipped (e.g. permission denied). The caller must never treat a
     * false return as an enforcement failure — only as "explanation not shown".
     */
    public boolean showBlockedNotification(String vaultedAppDisplayName) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return false;
        }

        Intent intent = new Intent(context, VaultBlockActivity.class);
        // This PendingIntent is only ever fired by the user tapping the
        // notification — a genuine, Android-documented BAL-exempt path
        // ("activity started from a PendingIntent... e.g. notification tap").
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setContentTitle(context.getString(R.string.vault_notification_title))
                .setContentText(context.getString(R.string.vault_notification_body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build());
            return true;
        } catch (SecurityException e) {
            // Defensive: areNotificationsEnabled() already checked above,
            // but a race with the user revoking permission mid-call is
            // still theoretically possible. Never let this crash enforcement.
            return false;
        }
    }

    private void createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.vault_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        manager.createNotificationChannel(channel);
    }
}
