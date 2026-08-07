package com.neurofix.app.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents a single application the user has placed inside the Vault.
 * packageName is the natural key — an app can only be vaulted once.
 */
@Entity(tableName = "vaulted_apps")
public class VaultedAppEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "package_name")
    private String packageName;

    @ColumnInfo(name = "display_name")
    private String displayName;

    @ColumnInfo(name = "date_vaulted")
    private long dateVaultedEpochMillis;

    @ColumnInfo(name = "is_active")
    private boolean isActive;

    public VaultedAppEntity(@NonNull String packageName, String displayName,
                             long dateVaultedEpochMillis, boolean isActive) {
        this.packageName = packageName;
        this.displayName = displayName;
        this.dateVaultedEpochMillis = dateVaultedEpochMillis;
        this.isActive = isActive;
    }

    @NonNull
    public String getPackageName() {
        return packageName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getDateVaultedEpochMillis() {
        return dateVaultedEpochMillis;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
