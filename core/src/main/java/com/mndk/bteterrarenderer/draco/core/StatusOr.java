package com.mndk.bteterrarenderer.draco.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
public class StatusOr<T> {

    public static <T> StatusOr<T> ok(T value) { return new StatusOr<>(value); }
    public static <T> StatusOr<T> error(Status status) { return new StatusOr<>(status); }
    public static <T> StatusOr<T> error(StatusChain status) { return new StatusOr<>(status.get()); }
    public static <T> StatusOr<T> dracoError(String message) { return new StatusOr<>(Status.dracoError(message)); }
    public static <T> StatusOr<T> ioError(String message) { return new StatusOr<>(Status.ioError(message)); }
    public static <T> StatusOr<T> invalidParameter(String message) { return new StatusOr<>(Status.invalidParameter(message)); }
    public static <T> StatusOr<T> unsupportedVersion(String message) { return new StatusOr<>(Status.unsupportedVersion(message)); }
    public static <T> StatusOr<T> unknownVersion(String message) { return new StatusOr<>(Status.unknownVersion(message)); }
    public static <T> StatusOr<T> unsupportedFeature(String message) { return new StatusOr<>(Status.unsupportedFeature(message)); }
    public static <T> StatusOr<T> error(Status.Code code, String message) {
        return new StatusOr<>(new Status(code, message));
    }

    @Getter
    private final Status status;
    private final T value;

    private StatusOr(Status status) {
        this(status, null);
    }

    private StatusOr(T value) {
        this(Status.ok(), value);
    }

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
        if(status.isError()) throw new DracoCompressionRuntimeException(status);
        return value;
    }
}
