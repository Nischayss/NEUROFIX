package com.neurofix.app.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.neurofix.app.database.entity.VaultedAppEntity;

import java.util.List;

@Dao
public interface VaultedAppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(VaultedAppEntity app);

    @Update
    void update(VaultedAppEntity app);

    @Delete
    void delete(VaultedAppEntity app);

    @Query("SELECT * FROM vaulted_apps WHERE is_active = 1 ORDER BY display_name ASC")
    LiveData<List<VaultedAppEntity>> observeActiveVaultedApps();

    @Query("SELECT * FROM vaulted_apps WHERE package_name = :packageName LIMIT 1")
    VaultedAppEntity findByPackageName(String packageName);

    @Query("SELECT COUNT(*) FROM vaulted_apps WHERE is_active = 1")
    LiveData<Integer> observeActiveVaultedAppCount();

    @Query("SELECT COUNT(*) FROM vaulted_apps WHERE is_active = 1")
    int getActiveVaultedAppCount();

    @Query("SELECT * FROM vaulted_apps ORDER BY display_name ASC")
    LiveData<List<VaultedAppEntity>> observeAllVaultedApps();

    @Query("SELECT package_name FROM vaulted_apps")
    List<String> getAllVaultedPackageNames();

    @Query("UPDATE vaulted_apps SET is_active = :isActive WHERE package_name = :packageName")
    void updateActiveState(String packageName, boolean isActive);

    @Query("DELETE FROM vaulted_apps WHERE package_name = :packageName")
    void deleteByPackageName(String packageName);
}
