package com.neurofix.app.presentation.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.neurofix.app.R;
import com.neurofix.app.databinding.ActivityMainBinding;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Single-Activity host. All screens (Onboarding, Dashboard, Vault, Focus Modes,
 * Emergency Session, Statistics, Settings) are Fragments navigated via the
 * Navigation Component graph — chosen so the Vault Screen can be reached via
 * deep link from the Vault Engine (AccessibilityService) without recreating
 * app state.
 *
 * STARTUP DESTINATION: nav_graph.xml's app:startDestination is a static
 * XML attribute (onboardingWelcomeFragment) — correct for a genuinely fresh
 * launch, but wrong once onboarding is already complete.
 *
 * History of this fix, kept here deliberately so the reasoning isn't lost:
 *   v1: gated the correction on savedInstanceState == null. Assumption
 *       didn't hold on the real device — still landed on onboarding.
 *   v2: switched to checking the NavController's ACTUAL current
 *       destination after the graph settles, and correcting it by
 *       mutating NavGraph.setStartDestination() + re-calling setGraph().
 *       Functionally should work, but graph-mutation-after-attach is a
 *       less common pattern with less predictable cross-version behavior.
 *   v3 (this version): same evidence-based detection as v2, but the
 *       correction itself now uses navController.navigate() with
 *       NavOptions.setPopUpTo(graphId, inclusive=true) — the same
 *       standard, well-tested API already used elsewhere in this project's
 *       own nav_graph.xml (the app_selection_to_dashboard action uses the
 *       identical popUpTo/popUpToInclusive pattern declaratively). This
 *       avoids ever mutating an already-attached graph object.
 */
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            return;
        }

        MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        if (!viewModel.isOnboardingComplete()) {
            return; // onboarding genuinely not done — never interfere with it
        }

        NavController navController = navHostFragment.getNavController();
        NavDestination currentDestination = navController.getCurrentDestination();
        if (currentDestination == null) {
            return;
        }

        int currentId = currentDestination.getId();
        boolean strandedOnOnboarding =
                currentId == R.id.onboardingWelcomeFragment
                        || currentId == R.id.onboardingPermissionsFragment
                        || currentId == R.id.onboardingAppSelectionFragment;

        if (strandedOnOnboarding) {
            NavOptions clearOnboardingBackStack = new NavOptions.Builder()
                    .setPopUpTo(navController.getGraph().getId(), true)
                    .build();
            navController.navigate(R.id.dashboardFragment, null, clearOnboardingBackStack);
        }
    }
}
