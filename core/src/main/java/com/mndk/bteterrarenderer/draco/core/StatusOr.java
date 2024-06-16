package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.draco.util.DracoCompressionException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StatusOr<T> {

    @Getter
    private final Status status;
    private final T value;

    public StatusOr() {
        this(new Status(), null);
    }

    public StatusOr(StatusOr<T> other) {
        this(other.status, other.value);
    }

    public StatusOr(Status status) {
        this(status, null);
    }

    public StatusOr(T value) {
        this(new Status(), value);
    }

    public T getValue() throws DracoCompressionException {
        if(status.isError(null)) throw new DracoCompressionException(status);
        return value;
    }
}
