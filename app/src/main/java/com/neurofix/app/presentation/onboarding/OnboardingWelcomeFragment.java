package com.neurofix.app.presentation.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.neurofix.app.R;
import com.neurofix.app.databinding.FragmentOnboardingWelcomeBinding;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Purely static content — philosophy explanation, no data, no ViewModel
 * needed. The View layer stays honest here: a screen with nothing to
 * observe shouldn't be forced to wire up a ViewModel it doesn't use.
 */
@AndroidEntryPoint
public class OnboardingWelcomeFragment extends Fragment {

    private FragmentOnboardingWelcomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentOnboardingWelcomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.buttonGetStarted.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_welcome_to_permissions));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
