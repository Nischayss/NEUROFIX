package com.neurofix.app.presentation.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.neurofix.app.R;
import com.neurofix.app.databinding.FragmentOnboardingPermissionsBinding;
import com.neurofix.app.permissions.PermissionHelper;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Scoped to requireActivity() so this Fragment shares the same
 * OnboardingViewModel instance as Welcome and App Selection — selection
 * state and permission state live in one place for the whole flow.
 */
@AndroidEntryPoint
public class OnboardingPermissionsFragment extends Fragment {

    private FragmentOnboardingPermissionsBinding binding;
    private OnboardingViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentOnboardingPermissionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);

        binding.buttonGrantUsageAccess.setOnClickListener(v ->
                startActivity(PermissionHelper.buildUsageAccessSettingsIntent()));
        binding.buttonGrantAccessibility.setOnClickListener(v ->
                startActivity(PermissionHelper.buildAccessibilitySettingsIntent()));

        binding.buttonContinue.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_permissions_to_app_selection));

        viewModel.getUsageAccessGranted().observe(getViewLifecycleOwner(), granted -> {
            updateStatusText(binding.textUsageAccessStatus, granted);
            updateContinueButtonState();
        });
        viewModel.getAccessibilityGranted().observe(getViewLifecycleOwner(), granted -> {
            updateStatusText(binding.textAccessibilityStatus, granted);
            updateContinueButtonState();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // The grants happen in a separate system Settings screen, so we
        // can only learn the result by re-checking when the user returns.
        viewModel.refreshPermissionState();
    }

    private void updateStatusText(android.widget.TextView textView, Boolean granted) {
        boolean isGranted = Boolean.TRUE.equals(granted);
        textView.setText(isGranted
                ? getString(R.string.permission_status_granted)
                : getString(R.string.permission_status_not_granted));
    }

    private void updateContinueButtonState() {
        boolean usageGranted = Boolean.TRUE.equals(viewModel.getUsageAccessGranted().getValue());
        boolean accessibilityGrantedState = Boolean.TRUE.equals(viewModel.getAccessibilityGranted().getValue());
        binding.buttonContinue.setEnabled(usageGranted && accessibilityGrantedState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
