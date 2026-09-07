package com.neurofix.app.di;

import android.content.Context;

import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.neurofix.app.database.NeuroFixDatabase;
import com.neurofix.app.database.dao.FocusModeDao;
import com.neurofix.app.database.dao.VaultedAppDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    /**
     * Step 9: adds focus_modes + focus_mode_apps only. vaulted_apps is not
     * referenced anywhere in this migration — existing Vault data survives
     * the upgrade completely unmodified. SQL here matches exactly what
     * @Entity/@ForeignKey on FocusModeEntity / FocusModeAppCrossRefEntity
     * generate, so Room's schema validation at startup passes.
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `focus_modes` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`is_preset` INTEGER NOT NULL, " +
                    "`is_active` INTEGER NOT NULL)");

            db.execSQL("CREATE TABLE IF NOT EXISTS `focus_mode_apps` (" +
                    "`focus_mode_id` INTEGER NOT NULL, " +
                    "`package_name` TEXT NOT NULL, " +
                    "`display_name` TEXT, " +
                    "PRIMARY KEY(`focus_mode_id`, `package_name`), " +
                    "FOREIGN KEY(`focus_mode_id`) REFERENCES `focus_modes`(`id`) " +
                    "ON UPDATE NO ACTION ON DELETE CASCADE)");

            db.execSQL("CREATE INDEX IF NOT EXISTS `index_focus_mode_apps_focus_mode_id` " +
                    "ON `focus_mode_apps` (`focus_mode_id`)");
        }
    };

    @Provides
    @Singleton
    public static NeuroFixDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, NeuroFixDatabase.class, NeuroFixDatabase.DATABASE_NAME)
                // No fallbackToDestructiveMigration in production: every schema
                // change from here on must ship a real Migration so user data
                // (their Vault, their streaks) is never silently wiped.
                .addMigrations(MIGRATION_1_2)
                .build();
    }

    @Provides
    @Singleton
    public static VaultedAppDao provideVaultedAppDao(NeuroFixDatabase database) {
        return database.vaultedAppDao();
    }

    @Provides
    @Singleton
    public static FocusModeDao provideFocusModeDao(NeuroFixDatabase database) {
        return database.focusModeDao();
    }
}
