package com.neurofix.app.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.neurofix.app.database.entity.FocusModeAppCrossRefEntity;
import com.neurofix.app.database.entity.FocusModeEntity;

import java.util.List;

@Dao
public interface FocusModeDao {

    @Insert
    long insertFocusMode(FocusModeEntity mode);

    @Delete
    void deleteFocusMode(FocusModeEntity mode);

    @Query("SELECT * FROM focus_modes ORDER BY is_preset DESC, id ASC")
    LiveData<List<FocusModeEntity>> observeAllFocusModes();

    @Query("SELECT COUNT(*) FROM focus_modes")
    int countFocusModes();

    /**
     * The single query the enforcement engine (VaultAccessibilityService)
     * actually needs: package names belonging to whichever mode is
     * currently active, or an empty list if none is. Deliberately a plain
     * String list, not the full entity — this is a read path for
     * enforcement, not for the UI.
     */
    @Query("SELECT fma.package_name FROM focus_mode_apps fma " +
            "INNER JOIN focus_modes fm ON fm.id = fma.focus_mode_id " +
            "WHERE fm.is_active = 1")
    LiveData<List<String>> observeActiveModePackageNames();

    @Query("SELECT * FROM focus_mode_apps WHERE focus_mode_id = :modeId ORDER BY display_name ASC")
    LiveData<List<FocusModeAppCrossRefEntity>> observeAppsForMode(long modeId);

    @Query("SELECT package_name FROM focus_mode_apps WHERE focus_mode_id = :modeId")
    List<String> getPackageNamesForMode(long modeId);

    @Insert
    void insertAppToMode(FocusModeAppCrossRefEntity crossRef);

    @Query("DELETE FROM focus_mode_apps WHERE focus_mode_id = :modeId AND package_name = :packageName")
    void removeAppFromMode(long modeId, String packageName);

    /**
     * Activates exactly one mode (or none, when modeId is null) in a single
     * transaction — clears every row's is_active first, so at most one is
     * ever true, without relying on a DB-level constraint Room can't express
     * for a cross-row invariant like this.
     */
    @Transaction
    default void setActiveMode(Long modeId) {
        deactivateAllModes();
        if (modeId != null) {
            activateMode(modeId);
        }
    }

    @Query("UPDATE focus_modes SET is_active = 0")
    void deactivateAllModes();

    @Query("UPDATE focus_modes SET is_active = 1 WHERE id = :modeId")
    void activateMode(long modeId);
}
