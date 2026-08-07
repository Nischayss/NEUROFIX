package com.neurofix.app.presentation.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
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
        // navHostFragment.getNavController() will be wired to the bottom
        // navigation / toolbar once the Dashboard feature defines the
        // top-level destinations.
    }
}
