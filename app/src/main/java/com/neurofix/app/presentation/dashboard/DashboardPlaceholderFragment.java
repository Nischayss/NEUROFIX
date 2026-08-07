package com.neurofix.app.presentation.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.neurofix.app.databinding.FragmentDashboardPlaceholderBinding;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Deliberately minimal — exists only to prove onboarding hands off
 * successfully and to give the completed onboarding flow a landing spot
 * with its back stack cleared. Real Dashboard content (focus status,
 * streaks, quick actions) is Step 6 and is out of scope here.
 */
@AndroidEntryPoint
public class DashboardPlaceholderFragment extends Fragment {

    private FragmentDashboardPlaceholderBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardPlaceholderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
