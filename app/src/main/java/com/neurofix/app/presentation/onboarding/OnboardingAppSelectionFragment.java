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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.neurofix.app.R;
import com.neurofix.app.databinding.FragmentOnboardingAppSelectionBinding;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Final onboarding step: choose which apps become Vaulted Apps. Loads the
 * installed-app list once (guarded so rotation/back-navigation doesn't
 * re-trigger a PackageManager scan), and completes onboarding on Finish.
 */
@AndroidEntryPoint
public class OnboardingAppSelectionFragment extends Fragment {

    private FragmentOnboardingAppSelectionBinding binding;
    private OnboardingViewModel viewModel;
    private InstalledAppAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentOnboardingAppSelectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);

        adapter = new InstalledAppAdapter(
                requireContext().getPackageManager(),
                viewModel::toggleAppSelection
        );
        binding.recyclerInstalledApps.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerInstalledApps.setAdapter(adapter);

        binding.buttonFinishSetup.setOnClickListener(v -> viewModel.completeOnboarding());

        viewModel.getInstallableApps().observe(getViewLifecycleOwner(), apps -> {
            binding.progressLoadingApps.setVisibility(View.GONE);
            adapter.submitList(apps);
        });

        viewModel.getSelectedPackageNames().observe(getViewLifecycleOwner(), selected -> {
            adapter.setSelectedPackageNames(selected);
            binding.buttonFinishSetup.setEnabled(!selected.isEmpty());
        });

        viewModel.getOnboardingCompleted().observe(getViewLifecycleOwner(), completed -> {
            if (Boolean.TRUE.equals(completed)) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_app_selection_to_dashboard);
            }
        });

        if (viewModel.getInstallableApps().getValue() == null) {
            viewModel.loadInstallableApps();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
