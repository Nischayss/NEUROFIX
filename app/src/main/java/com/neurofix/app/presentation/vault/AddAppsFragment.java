package com.neurofix.app.presentation.vault;

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

import com.neurofix.app.databinding.FragmentAddAppsBinding;
import com.neurofix.app.presentation.onboarding.InstalledAppAdapter;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Reuses the existing InstalledAppAdapter from presentation.onboarding
 * rather than duplicating it — it's already exactly what's needed (icon,
 * name, multi-select checkbox), and duplicating working code was explicitly
 * ruled out. This does mean presentation.vault depends on a class in
 * presentation.onboarding, a deliberate, flagged trade-off.
 */
@AndroidEntryPoint
public class AddAppsFragment extends Fragment {

    private FragmentAddAppsBinding binding;
    private AddAppsViewModel viewModel;
    private InstalledAppAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentAddAppsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddAppsViewModel.class);

        adapter = new InstalledAppAdapter(
                requireContext().getPackageManager(),
                viewModel::toggleAppSelection
        );
        binding.recyclerInstalledApps.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerInstalledApps.setAdapter(adapter);

        binding.buttonConfirmAdd.setOnClickListener(v -> viewModel.confirmAdd());

        viewModel.getAddableApps().observe(getViewLifecycleOwner(), apps -> {
            binding.progressLoadingApps.setVisibility(View.GONE);
            adapter.submitList(apps);
        });

        viewModel.getSelectedPackageNames().observe(getViewLifecycleOwner(), selected -> {
            adapter.setSelectedPackageNames(selected);
            binding.buttonConfirmAdd.setEnabled(!selected.isEmpty());
        });

        viewModel.getAddCompleted().observe(getViewLifecycleOwner(), completed -> {
            if (Boolean.TRUE.equals(completed)) {
                NavHostFragment.findNavController(this).popBackStack();
            }
        });

        if (viewModel.getAddableApps().getValue() == null) {
            viewModel.loadAddableApps();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
