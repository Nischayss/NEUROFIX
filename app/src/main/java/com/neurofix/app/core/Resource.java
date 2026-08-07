package com.neurofix.app.core;

/**
 * Represents ongoing UI state for a LiveData stream (e.g. the list of
 * VaultedApps on the Vault screen): Loading while Room/DataStore is read,
 * Success once data arrives, Error if the read fails. Unlike {@link Result},
 * this is meant to be re-emitted multiple times over a stream's lifetime.
 */
public abstract class Resource<T> {

    private Resource() {
    }

    public static <T> Resource<T> loading() {
        return new Loading<>();
    }

    public static <T> Resource<T> success(T data) {
        return new Success<>(data);
    }

    public static <T> Resource<T> error(String message) {
        return new Error<>(message);
    }

    public static final class Loading<T> extends Resource<T> {
    }

    public static final class Success<T> extends Resource<T> {
        public final T data;

        private Success(T data) {
            this.data = data;
        }
    }

    public static final class Error<T> extends Resource<T> {
        public final String message;

        private Error(String message) {
            this.message = message;
        }
    }
}
