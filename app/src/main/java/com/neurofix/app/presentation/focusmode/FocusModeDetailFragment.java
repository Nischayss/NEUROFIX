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

import com.neurofix.app.R;
import com.neurofix.app.databinding.FragmentFocusModeDetailBinding;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Shows one Focus Mode's own app list (add/remove). Reached only from
 * FocusModesFragment, which always supplies both nav arguments.
 */
@AndroidEntryPoint
public class FocusModeDetailFragment extends Fragment {

    public static final String ARG_MODE_ID = "mode_id";
    public static final String ARG_MODE_NAME = "mode_name";

    private FragmentFocusModeDetailBinding binding;
    private FocusModeDetailViewModel viewModel;
    private FocusModeAppAdapter adapter;
    private long modeId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentFocusModeDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FocusModeDetailViewModel.class);

        modeId = requireArguments().getLong(ARG_MODE_ID);
        String modeName = requireArguments().getString(ARG_MODE_NAME);
        binding.textTitle.setText(modeName);

        adapter = new FocusModeAppAdapter(
                requireContext().getPackageManager(),
                app -> viewModel.removeApp(modeId, app)
        );
        binding.recyclerModeApps.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerModeApps.setAdapter(adapter);

        viewModel.getAppsForMode(modeId).observe(getViewLifecycleOwner(), apps -> {
            adapter.submitList(apps);
            boolean isEmpty = apps == null || apps.isEmpty();
            binding.textEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        });

        binding.buttonAddApps.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putLong(FocusModeDetailFragment.ARG_MODE_ID, modeId);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_focus_mode_detail_to_add_apps, args);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
