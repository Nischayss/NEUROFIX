package com.neurofix.app.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A Focus Mode — Study/Work/Sleep/Gym (presets, seeded once, not user-
 * deletable) or a user-created Custom mode. Each mode has its own
 * independent list of vaulted apps (see FocusModeAppCrossRefEntity) —
 * deliberately not shared with the Step 7 base Vault.
 *
 * is_active: manual on/off only in this step. At most one row should be
 * true at a time — enforced in FocusModeRepositoryImpl via a single
 * transaction (clear-all-then-set-one), not by a DB constraint, since Room
 * doesn't express "at most one true" as a column-level check. Automatic
 * activation by schedule is Step 10, not this entity's concern.
 */
@Entity(tableName = "focus_modes")
public class FocusModeEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "is_preset")
    private boolean isPreset;

    @ColumnInfo(name = "is_active")
    private boolean isActive;

    public FocusModeEntity(long id, @NonNull String name, boolean isPreset, boolean isActive) {
        this.id = id;
        this.name = name;
        this.isPreset = isPreset;
        this.isActive = isActive;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public boolean isPreset() {
        return isPreset;
    }

    public boolean isActive() {
        return isActive;
    }
}
