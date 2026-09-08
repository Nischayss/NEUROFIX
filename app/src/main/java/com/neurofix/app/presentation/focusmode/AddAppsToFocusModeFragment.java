package com.neurofix.app.presentation.focusmode;

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

/**
 * Reuses FragmentAddAppsBinding (same layout as the base-Vault Add Apps
 * screen — identical shape: list + confirm button + loading indicator) and
 * InstalledAppAdapter directly, rather than duplicating either. Only the
 * ViewModel differs (scoped to one mode's list instead of the base Vault),
 * same reuse principle AddAppsFragment's own javadoc already states.
 */
@dagger.hilt.android.AndroidEntryPoint
public class AddAppsToFocusModeFragment extends Fragment {

    private FragmentAddAppsBinding binding;
    private AddAppsToFocusModeViewModel viewModel;
    private InstalledAppAdapter adapter;
    private long modeId;

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
        viewModel = new ViewModelProvider(this).get(AddAppsToFocusModeViewModel.class);
        modeId = requireArguments().getLong(FocusModeDetailFragment.ARG_MODE_ID);

        adapter = new InstalledAppAdapter(
                requireContext().getPackageManager(),
                viewModel::toggleAppSelection
        );
        binding.recyclerInstalledApps.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerInstalledApps.setAdapter(adapter);

        binding.buttonConfirmAdd.setOnClickListener(v -> viewModel.confirmAdd(modeId));

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
            viewModel.loadAddableApps(modeId);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
