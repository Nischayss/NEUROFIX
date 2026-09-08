package com.neurofix.app.presentation.focusmode;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.neurofix.app.R;
import com.neurofix.app.databinding.FragmentFocusModesBinding;
import com.neurofix.app.domain.model.FocusMode;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Lists all Focus Modes (4 seeded presets + any custom ones). Purely
 * observational, same rule as every other screen — no business logic here.
 * Create Custom Mode uses a plain AlertDialog rather than a new screen —
 * a single text field doesn't warrant a whole Fragment/ViewModel pair.
 */
@AndroidEntryPoint
public class FocusModesFragment extends Fragment {

    private FragmentFocusModesBinding binding;
    private FocusModesViewModel viewModel;
    private FocusModeAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentFocusModesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FocusModesViewModel.class);

        adapter = new FocusModeAdapter(
                viewModel::setActive,
                viewModel::deleteMode,
                this::openModeDetail
        );
        binding.recyclerFocusModes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerFocusModes.setAdapter(adapter);

        viewModel.getFocusModes().observe(getViewLifecycleOwner(), modes -> {
            adapter.submitList(modes);
            boolean isEmpty = modes == null || modes.isEmpty();
            binding.textEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        });

        binding.buttonCreateCustomMode.setOnClickListener(v -> showCreateModeDialog());
    }

    private void openModeDetail(FocusMode mode) {
        Bundle args = new Bundle();
        args.putLong(FocusModeDetailFragment.ARG_MODE_ID, mode.getId());
        args.putString(FocusModeDetailFragment.ARG_MODE_NAME, mode.getName());
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_focus_modes_to_detail, args);
    }

    private void showCreateModeDialog() {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(R.string.focus_modes_create_dialog_hint);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.focus_modes_create_dialog_title)
                .setView(input)
                .setPositiveButton(android.R.string.ok, (dialog, which) ->
                        viewModel.createCustomMode(input.getText().toString()))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
