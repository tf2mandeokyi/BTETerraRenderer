package com.mndk.bteterrarenderer.draco.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.mndk.bteterrarenderer.draco.core.Status.Code.*;

@RequiredArgsConstructor
public class StatusOr<T> {

    public static <T> StatusOr<T> ok(T value) { return of(value); }
    public static <T> StatusOr<T> error(Status status) { return of(status); }
    public static <T> StatusOr<T> error(StatusChain status) { return of(status.get()); }
    public static <T> StatusOr<T> dracoError(String message) { return of(DRACO_ERROR, message); }
    public static <T> StatusOr<T> dracoError(String message, Throwable t) { return of(DRACO_ERROR, message, t); }
    public static <T> StatusOr<T> ioError(String message) { return of(IO_ERROR, message); }
    public static <T> StatusOr<T> ioError(String message, Throwable t) { return of(IO_ERROR, message, t); }
    public static <T> StatusOr<T> invalidParameter(String message) { return of(INVALID_PARAMETER, message); }
    public static <T> StatusOr<T> invalidParameter(String message, Throwable t) { return of(INVALID_PARAMETER, message, t); }
    public static <T> StatusOr<T> unsupportedVersion(String message) { return of(UNSUPPORTED_VERSION, message); }
    public static <T> StatusOr<T> unsupportedVersion(String message, Throwable t) { return of(UNSUPPORTED_VERSION, message, t); }
    public static <T> StatusOr<T> unknownVersion(String message) { return of(UNKNOWN_VERSION, message); }
    public static <T> StatusOr<T> unknownVersion(String message, Throwable t) { return of(UNKNOWN_VERSION, message, t); }
    public static <T> StatusOr<T> unsupportedFeature(String message) { return of(UNSUPPORTED_FEATURE, message); }
    public static <T> StatusOr<T> unsupportedFeature(String message, Throwable t) { return of(UNSUPPORTED_FEATURE, message, t); }

    private static <T> StatusOr<T> of(T value) {
        return new StatusOr<>(Status.ok(), value);
    }
    private static <T> StatusOr<T> of(Status status) {
        return new StatusOr<>(status, null);
    }
    private static <T> StatusOr<T> of(Status.Code code, String message) {
        Status status = new Status(code, message, Status.generateStackTrace(), null);
        return new StatusOr<>(status, null);
    }
    private static <T> StatusOr<T> of(Status.Code code, String message, Throwable cause) {
        Status status = new Status(code, message, Status.generateStackTrace(), cause);
        return new StatusOr<>(status, null);
    }

    @Getter
    private final Status status;
    private final T value;

    public boolean isError(StatusChain chain) {
        return status.isError(chain);
    }

    public T getValueOr(Consumer<Status> consumer) {
        if(status.isError()) {
            consumer.accept(status);
            return null;
        }
        return value;
    }

    public T getValueOr(Function<Status, T> function) {
        if(status.isError()) return function.apply(status);
        return value;
    }

    public T getValue() {
        if(status.isError()) throw status.getException();
        return value;
    }
}
