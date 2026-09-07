package com.neurofix.app.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.neurofix.app.database.dao.FocusModeDao;
import com.neurofix.app.database.dao.VaultedAppDao;
import com.neurofix.app.database.entity.FocusModeAppCrossRefEntity;
import com.neurofix.app.database.entity.FocusModeEntity;
import com.neurofix.app.database.entity.VaultedAppEntity;

/**
 * Single Room database for the app. Offline First / Privacy First means this
 * is the only persistence mechanism for structured data — no remote sync,
 * no cloud-backed tables.
 *
 * v1 -> v2 (Step 9): added FocusModeEntity + FocusModeAppCrossRefEntity.
 * ADD-only migration (MIGRATION_1_2 in DatabaseModule) — vaulted_apps is
 * untouched, so existing Vault data survives the upgrade unmodified.
 *
 * New entities (Schedule, StreakHistory, ...) are added here only when the
 * feature that owns them is actually built, each with its own migration —
 * not speculatively.
 */
@Database(
        entities = {VaultedAppEntity.class, FocusModeEntity.class, FocusModeAppCrossRefEntity.class},
        version = 2,
        exportSchema = true
)
public abstract class NeuroFixDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "neurofix.db";

    public abstract VaultedAppDao vaultedAppDao();

    public abstract FocusModeDao focusModeDao();
}
