package com.neurofix.app.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

/**
 * One (Focus Mode, app) pairing. Composite primary key — an app can only
 * appear once within the same mode, but the same app CAN appear in
 * multiple different modes independently (e.g. a messaging app vaulted in
 * both Study and Sleep), since each mode's list is its own, per the user's
 * explicit choice for this feature.
 *
 * ON DELETE CASCADE: deleting a (custom) Focus Mode automatically removes
 * its app entries — no orphaned rows possible, no separate cleanup query
 * needed in the repository.
 */
@Entity(
        tableName = "focus_mode_apps",
        primaryKeys = {"focus_mode_id", "package_name"},
        foreignKeys = @ForeignKey(
                entity = FocusModeEntity.class,
                parentColumns = "id",
                childColumns = "focus_mode_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("focus_mode_id")}
)
public class FocusModeAppCrossRefEntity {

    @ColumnInfo(name = "focus_mode_id")
    private long focusModeId;

    @NonNull
    @ColumnInfo(name = "package_name")
    private String packageName;

    @ColumnInfo(name = "display_name")
    private String displayName;

    public FocusModeAppCrossRefEntity(long focusModeId, @NonNull String packageName, String displayName) {
        this.focusModeId = focusModeId;
        this.packageName = packageName;
        this.displayName = displayName;
    }

    public long getFocusModeId() {
        return focusModeId;
    }

    @NonNull
    public String getPackageName() {
        return packageName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
