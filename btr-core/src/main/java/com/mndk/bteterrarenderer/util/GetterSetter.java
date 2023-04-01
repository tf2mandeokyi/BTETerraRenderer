package com.mndk.bteterrarenderer.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface GetterSetter<T> {
    T get();
    void set(T value);

    static <T> GetterSetter<T> from(Supplier<T> getter, Consumer<T> setter) {
        return new AbstractGetterSetter<>(getter, setter);
    }

    class AbstractGetterSetter<T> implements GetterSetter<T> {

        private final Supplier<T> getter;
        private final Consumer<T> setter;

        AbstractGetterSetter(Supplier<T> getter, Consumer<T> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public T get() { return getter.get(); }

        @Override
        public void set(T value) { setter.accept(value); }
    }
}
