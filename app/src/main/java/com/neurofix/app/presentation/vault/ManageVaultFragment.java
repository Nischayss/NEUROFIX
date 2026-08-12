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

import com.neurofix.app.R;
import com.neurofix.app.databinding.FragmentManageVaultBinding;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Real Manage Vault screen, replacing the PlaceholderFragment previously
 * wired to action_dashboard_to_manage_vault. Purely observational — no
 * business logic here, per the architecture rule.
 */
@AndroidEntryPoint
public class ManageVaultFragment extends Fragment {

    private FragmentManageVaultBinding binding;
    private ManageVaultViewModel viewModel;
    private VaultedAppAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentManageVaultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ManageVaultViewModel.class);

        adapter = new VaultedAppAdapter(
                requireContext().getPackageManager(),
                viewModel::setActive,
                viewModel::remove
        );
        binding.recyclerVaultedApps.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerVaultedApps.setAdapter(adapter);

        viewModel.getVaultedApps().observe(getViewLifecycleOwner(), apps -> {
            adapter.submitList(apps);
            boolean isEmpty = apps == null || apps.isEmpty();
            binding.textEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        });

        binding.buttonAddApps.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_manage_vault_to_add_apps));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
