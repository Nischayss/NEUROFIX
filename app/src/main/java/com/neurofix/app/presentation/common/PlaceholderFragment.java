package com.neurofix.app.presentation.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.neurofix.app.R;
import com.neurofix.app.databinding.FragmentPlaceholderBinding;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Single reusable "coming soon" screen for not-yet-built destinations. Reads
 * its title from a Navigation argument instead of existing as three
 * near-identical fragment classes (Manage Vault, Statistics, Settings) —
 * the same minimal UI repeated three times doesn't justify three files.
 */
@AndroidEntryPoint
public class PlaceholderFragment extends Fragment {

    private static final String ARG_TITLE = "title";

    private FragmentPlaceholderBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentPlaceholderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String title = getArguments() != null ? getArguments().getString(ARG_TITLE) : null;
        binding.textPlaceholderTitle.setText(title != null ? title : getString(R.string.placeholder_default_title));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
