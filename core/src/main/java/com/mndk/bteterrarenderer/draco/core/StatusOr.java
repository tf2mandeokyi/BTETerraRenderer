package com.mndk.bteterrarenderer.draco.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StatusOr<T> {

    public static <T> StatusOr<T> ok(T value) {
        return new StatusOr<>(value);
    }
    public static <T> StatusOr<T> error(Status status) {
        return new StatusOr<>(status);
    }
    public static <T> StatusOr<T> error(Status.Code code, String message) {
        return new StatusOr<>(new Status(code, message));
    }

    @Getter
    private final Status status;
    private final T value;

    public StatusOr() {
        this(Status.OK, null);
    }

    public StatusOr(StatusOr<T> other) {
        this(other.status, other.value);
    }

    public StatusOr(Status status) {
        this(status, null);
    }

    public StatusOr(T value) {
        this(Status.OK, value);
    }

    public T getValue() throws DracoCompressionException {
        if(status.isError(null)) throw new DracoCompressionException(status);
        return value;
    }
}
