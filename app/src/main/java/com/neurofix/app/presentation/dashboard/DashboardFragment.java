package com.neurofix.app.presentation.dashboard;

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
import com.neurofix.app.databinding.FragmentDashboardBinding;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Dashboard is now the home screen after onboarding. Purely observational —
 * every value shown comes from DashboardViewModel's LiveData; this class
 * contains no business logic, per the architecture rule.
 */
@AndroidEntryPoint
public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        viewModel.getCurrentDate().observe(getViewLifecycleOwner(), binding.textCurrentDate::setText);
        viewModel.getFocusStatus().observe(getViewLifecycleOwner(), binding.textFocusStatusValue::setText);
        viewModel.getVaultedAppCount().observe(getViewLifecycleOwner(),
                count -> binding.textVaultedAppsCountValue.setText(String.valueOf(count)));
        viewModel.getTodaysFocus().observe(getViewLifecycleOwner(), binding.textTodaysFocusValue::setText);
        viewModel.getCurrentStreak().observe(getViewLifecycleOwner(), binding.textCurrentStreakValue::setText);
        viewModel.getLongestStreak().observe(getViewLifecycleOwner(), binding.textLongestStreakValue::setText);
        viewModel.getEmergencyBudget().observe(getViewLifecycleOwner(), binding.textEmergencyBudgetValue::setText);

        binding.buttonManageVault.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_dashboard_to_manage_vault));
        binding.buttonStatistics.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_dashboard_to_statistics));
        binding.buttonSettings.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_dashboard_to_settings));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
