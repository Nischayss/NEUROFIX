package com.neurofix.app.core;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Central background/main-thread executor pair. Room's own I/O already runs
 * off the main thread automatically via LiveData, so this is NOT for
 * database access — it exists for work outside Room's scope that later
 * features need: PIN hashing, Keystore encrypt/decrypt (Step 10+), and any
 * AccessibilityService callback processing that must not block the system's
 * accessibility event thread. Onboarding's Use Case calls (installed-app
 * scan, Room writes) also run through this.
 *
 * @Singleton + @Inject constructor: Hilt provides this automatically with
 * no separate module needed, since a scoped class with an injectable
 * constructor doesn't require a @Provides method.
 */
@Singleton
public class AppExecutors {

    private final ExecutorService diskIO = Executors.newSingleThreadExecutor();
    private final Executor mainThread = new MainThreadExecutor();

    @Inject
    public AppExecutors() {
    }

    public Executor diskIO() {
        return diskIO;
    }

    public Executor mainThread() {
        return mainThread;
    }

    private static class MainThreadExecutor implements Executor {
        private final Handler mainHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            mainHandler.post(command);
        }
    }
}
