package com.neurofix.app.core;

/**
 * One-shot outcome of a domain operation (a use case call). Distinct from
 * {@link Resource}, which represents ongoing UI-observable state (Loading/
 * Success/Error) for streams — Result is for single request/response calls,
 * e.g. "save this VaultedApp", "verify this PIN".
 *
 * Modeled as an abstract class with two static factories rather than an enum
 * or interface, since Java has no sealed types pre-17-with-preview and this
 * keeps exhaustive handling simple via instanceof checks at the call site.
 */
public abstract class Result<T> {

    private Result() {
    }

    public static <T> Result<T> success(T data) {
        return new Success<>(data);
    }

    public static <T> Result<T> error(Throwable throwable) {
        return new Error<>(throwable);
    }

    public boolean isSuccess() {
        return this instanceof Success;
    }

    public static final class Success<T> extends Result<T> {
        public final T data;

        private Success(T data) {
            this.data = data;
        }
    }

    public static final class Error<T> extends Result<T> {
        public final Throwable throwable;

        private Error(Throwable throwable) {
            this.throwable = throwable;
        }
    }
}
