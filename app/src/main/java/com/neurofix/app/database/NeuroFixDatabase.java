package com.neurofix.app.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.neurofix.app.database.dao.VaultedAppDao;
import com.neurofix.app.database.entity.VaultedAppEntity;

/**
 * Single Room database for the app. Offline First / Privacy First means this
 * is the only persistence mechanism for structured data — no remote sync,
 * no cloud-backed tables.
 *
 * New entities (VaultSession, Schedule, FocusMode, StreakHistory, ...) are
 * added here only when the feature that owns them is actually built, each
 * with its own migration — not speculatively.
 */
@Database(
        entities = {VaultedAppEntity.class},
        version = 1,
        exportSchema = true
)
public abstract class NeuroFixDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "neurofix.db";

    public abstract VaultedAppDao vaultedAppDao();
}
