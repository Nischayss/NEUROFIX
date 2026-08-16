package com.neurofix.app.presentation.vault;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.neurofix.app.databinding.ActivityVaultBlockBinding;
import com.neurofix.app.presentation.main.MainActivity;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * The NeuroFix Vault Screen — shown full-screen over a blocked app.
 *
 * WHY AN ACTIVITY: AccessibilityService cannot render UI directly, and a
 * Fragment cannot cover another app's window. Only a foreground Activity
 * reliably takes over the screen and input — this is standard, permission-
 * free Android behavior (the covered app is paused by the OS's own Activity
 * lifecycle, not by anything NeuroFix does to it).
 *
 * BACK BEHAVIOR: deliberately NOT the default pop-back-stack behavior,
 * since this Activity's task state (launched via FLAG_ACTIVITY_NEW_TASK
 * from a Service) makes default Back ambiguous. Back always goes Home
 * explicitly, so the blocked app is never one Back-press away.
 *
 * HONEST LIMITATION: the audio-focus request below is best-effort only.
 * A well-behaved app may pause playback on focus loss; nothing in the
 * Android accessibility APIs can force a poorly-behaved or foreground-
 * service-backed media app to stop. This is documented, not hidden.
 */
@AndroidEntryPoint
public class VaultBlockActivity extends AppCompatActivity {

    // TEMP DIAGNOSTIC — REMOVE AFTER STEP 8 VERIFICATION
    private static final String DIAG_TAG = "NEUROFIX_VAULT_DIAG";

    private ActivityVaultBlockBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TEMP DIAGNOSTIC — REMOVE AFTER STEP 8 VERIFICATION
        Log.d(DIAG_TAG, "TEMP DIAGNOSTIC: VAULT_BLOCK_ON_CREATE");

        binding = ActivityVaultBlockBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        requestAudioFocusBestEffort();

        binding.buttonReturnToNeurofix.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        registerBackPressedCallback();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TEMP DIAGNOSTIC — REMOVE AFTER STEP 8 VERIFICATION
        Log.d(DIAG_TAG, "TEMP DIAGNOSTIC: VAULT_BLOCK_ON_RESUME");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // TEMP DIAGNOSTIC — REMOVE AFTER STEP 8 VERIFICATION
        Log.d(DIAG_TAG, "TEMP DIAGNOSTIC: VAULT_BLOCK_ON_PAUSE");
    }

    private void registerBackPressedCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goHome();
            }
        });
    }

    private void goHome() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
        finish();
    }

    /**
     * Best-effort only — see class doc. Requests transient audio focus so a
     * cooperating app's audio may pause; does not guarantee it. No new
     * permission required, AudioManager focus requests are a normal API.
     */
    private void requestAudioFocusBestEffort() {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.media.AudioFocusRequest request = new android.media.AudioFocusRequest.Builder(
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).build();
            audioManager.requestAudioFocus(request);
        } else {
            audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
    }

    @Override
    protected void onDestroy() {
        // TEMP DIAGNOSTIC — REMOVE AFTER STEP 8 VERIFICATION
        Log.d(DIAG_TAG, "TEMP DIAGNOSTIC: VAULT_BLOCK_ON_DESTROY");
        binding = null;
        super.onDestroy();
    }
}
