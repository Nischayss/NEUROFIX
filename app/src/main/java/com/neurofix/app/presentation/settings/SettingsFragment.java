package com.neurofix.app.presentation.settings;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.neurofix.app.databinding.FragmentSettingsBinding;
import com.neurofix.app.domain.model.EnforcementMode;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * The only Settings screen built so far — deliberately scoped to the one
 * Step 8 toggle. Requesting POST_NOTIFICATIONS here (API 33+) is purely to
 * make Mode 2's explanation notification visible; declining it never
 * prevents the mode from being selected or enforcement from working —
 * VaultAccessibilityService always performs GLOBAL_ACTION_HOME regardless.
 *
 * FIX: previously the initial radio-button state was driven by observing
 * SettingsViewModel's LiveData, which called RadioGroup.check() — and
 * .check() synchronously fires the SAME OnCheckedChangeListener that also
 * calls back into that same LiveData mid-dispatch. That reentrant pattern
 * is fragile and has shown version-dependent AndroidX behavior. Now the
 * initial state is set ONCE, directly from the already-resolved ViewModel
 * value, BEFORE the click listener is attached at all — no reentrancy,
 * no LiveData observer needed for this screen (nothing external changes
 * this setting while Settings is open, so "live" updates aren't needed).
 */
@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted ->
                    updateNotificationHint());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        // Set the initial checked state directly, once, from the already-
        // resolved value — SettingsViewModel's constructor reads it
        // synchronously, so getValue() is guaranteed non-null here.
        EnforcementMode currentMode = viewModel.getEnforcementMode().getValue();
        int idToCheck = currentMode == EnforcementMode.RETURN_HOME_WITH_NOTIFICATION
                ? binding.radioReturnHomeWithNotification.getId()
                : binding.radioReturnHome.getId();
        binding.radioGroupEnforcementMode.check(idToCheck);

        // Listener attached AFTER the initial check() above, so that one
        // programmatic call never triggers this listener at all.
        binding.radioGroupEnforcementMode.setOnCheckedChangeListener((group, checkedId) -> {
            EnforcementMode mode = checkedId == binding.radioReturnHomeWithNotification.getId()
                    ? EnforcementMode.RETURN_HOME_WITH_NOTIFICATION
                    : EnforcementMode.RETURN_HOME;
            viewModel.setEnforcementMode(mode);
            if (mode == EnforcementMode.RETURN_HOME_WITH_NOTIFICATION) {
                requestNotificationPermissionIfNeeded();
            }
            updateNotificationHint();
        });

        updateNotificationHint();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return; // permission doesn't exist pre-API 33, nothing to request
        }
        boolean granted = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void updateNotificationHint() {
        boolean notificationModeSelected = binding.radioGroupEnforcementMode.getCheckedRadioButtonId()
                == binding.radioReturnHomeWithNotification.getId();

        boolean notificationPermissionDenied = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED;

        binding.textNotificationPermissionHint.setVisibility(
                notificationModeSelected && notificationPermissionDenied ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Permission may have been changed in system Settings since this
        // screen was last visible.
        updateNotificationHint();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
