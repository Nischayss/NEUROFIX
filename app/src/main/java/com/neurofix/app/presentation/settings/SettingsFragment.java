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

import com.neurofix.app.R;
import com.neurofix.app.databinding.FragmentSettingsBinding;
import com.neurofix.app.domain.model.EnforcementMode;
import com.neurofix.app.permissions.PermissionHelper;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * FIX (real root cause of "both options selected"): the two RadioButtons
 * are nested inside MaterialCardViews (for the card visual style), which
 * means they are NOT direct children of a RadioGroup — and RadioGroup's
 * automatic mutual-exclusion ONLY works between its direct children. The
 * RadioGroup wrapper was therefore never actually enforcing anything;
 * tapping one button never unchecked the other. This is now handled
 * explicitly in code instead, which works correctly regardless of how
 * deeply each button is nested in the layout.
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

        binding.radioReturnHome.setOnClickListener(v -> selectMode(EnforcementMode.RETURN_HOME));
        binding.radioReturnHomeWithNotification.setOnClickListener(v ->
                selectMode(EnforcementMode.RETURN_HOME_WITH_NOTIFICATION));

        applyModeToUi(viewModel.getEnforcementMode().getValue());
        updateNotificationHint();

// minimal replacement code
        binding.buttonEnableAccessibility.setOnClickListener(v ->
                startActivity(PermissionHelper.buildAccessibilitySettingsIntent()));
        binding.buttonEnableUsageAccess.setOnClickListener(v ->
                startActivity(PermissionHelper.buildUsageAccessSettingsIntent()));
        binding.buttonFixReliability.setOnClickListener(v -> startActivity(
                PermissionHelper.isKnownAggressiveOem()
                        ? PermissionHelper.buildAutostartSettingsIntent(requireContext())
                        : PermissionHelper.buildIgnoreBatteryOptimizationsIntent(requireContext())));

        viewModel.getAccessibilityEnabled().observe(getViewLifecycleOwner(), this::updateAccessibilityStatus);
        viewModel.getUsageAccessGranted().observe(getViewLifecycleOwner(), this::updateUsageAccessStatus);
        viewModel.getBatteryOptimizationIgnored().observe(getViewLifecycleOwner(), this::updateReliabilityStatus);
    }

    @Override
    public void onResume() {
        super.onResume();
        applyModeToUi(viewModel.refreshEnforcementMode());
        updateNotificationHint();
        viewModel.refreshPermissionStatus();
    }

    /** User tapped one of the two options — explicitly makes it exclusive. */
    private void selectMode(EnforcementMode mode) {
        applyModeToUi(mode);
        viewModel.setEnforcementMode(mode);
        if (mode == EnforcementMode.RETURN_HOME_WITH_NOTIFICATION) {
            requestNotificationPermissionIfNeeded();
        }
        updateNotificationHint();
    }

    /** Sets both buttons' checked state directly — never relies on RadioGroup. */
    private void applyModeToUi(EnforcementMode mode) {
        binding.radioReturnHome.setChecked(mode == EnforcementMode.RETURN_HOME);
        binding.radioReturnHomeWithNotification.setChecked(mode == EnforcementMode.RETURN_HOME_WITH_NOTIFICATION);
    }

    private void updateAccessibilityStatus(boolean enabled) {
        binding.textAccessibilityStatus.setText(enabled
                ? getString(R.string.permission_status_granted)
                : getString(R.string.permission_status_not_granted));
        binding.buttonEnableAccessibility.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }

    private void updateUsageAccessStatus(boolean granted) {
        binding.textUsageAccessStatus.setText(granted
                ? getString(R.string.permission_status_granted)
                : getString(R.string.permission_status_not_granted));
        binding.buttonEnableUsageAccess.setVisibility(granted ? View.GONE : View.VISIBLE);
    }

// minimal replacement code
    private void updateReliabilityStatus(boolean batteryOptimizationIgnored) {
        binding.textReliabilityStatus.setText(batteryOptimizationIgnored
                ? getString(R.string.settings_reliability_status_ok)
                : getString(R.string.settings_reliability_status_restricted));
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
        boolean notificationModeSelected = binding.radioReturnHomeWithNotification.isChecked();

        boolean notificationPermissionDenied = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED;

        binding.textNotificationPermissionHint.setVisibility(
                notificationModeSelected && notificationPermissionDenied ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
