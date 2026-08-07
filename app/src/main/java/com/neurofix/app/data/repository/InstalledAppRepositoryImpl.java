package com.neurofix.app.data.repository;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import androidx.annotation.NonNull;

import com.neurofix.app.domain.model.InstalledApp;
import com.neurofix.app.domain.repository.InstalledAppRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Enumerates launchable apps via ACTION_MAIN / CATEGORY_LAUNCHER instead of
 * getInstalledApplications() — the latter returns system components with no
 * UI and, on modern Android, requires the Play-Store-restricted
 * QUERY_ALL_PACKAGES permission. This approach needs no special permission.
 */
public class InstalledAppRepositoryImpl implements InstalledAppRepository {

    private final Context context;

    @Inject
    public InstalledAppRepositoryImpl(@ApplicationContext Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public List<InstalledApp> getLaunchableApps() {
        PackageManager packageManager = context.getPackageManager();
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolvedApps = packageManager.queryIntentActivities(launcherIntent, 0);
        String ownPackageName = context.getPackageName();

        List<InstalledApp> apps = new ArrayList<>();
        for (ResolveInfo resolveInfo : resolvedApps) {
            String packageName = resolveInfo.activityInfo.packageName;
            if (packageName.equals(ownPackageName)) {
                continue; // NeuroFix cannot vault itself
            }
            String displayName = resolveInfo.loadLabel(packageManager).toString();
            apps.add(new InstalledApp(packageName, displayName));
        }

        apps.sort(Comparator.comparing(InstalledApp::getDisplayName, String.CASE_INSENSITIVE_ORDER));
        return apps;
    }
}
