package com.mndk.bteterrarenderer.util.function;

@FunctionalInterface
public interface ThrowableBiConsumer<T, U, E extends Throwable> {
    void accept(T t, U u) throws E;
}
