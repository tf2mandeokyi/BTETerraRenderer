package com.mndk.bteterrarenderer.util;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class PropertyAccessor<T> {

    private PropertyAccessor() {}

    public final void set(T value) {
        if(this.available(value)) {
            this.setValue(value);
        }
    }

    public abstract T get();
    protected abstract void setValue(T value);
    public abstract boolean available(T value);

    public static <T> PropertyAccessor<T> of(Supplier<T> getter, Consumer<T> setter) {
        return new PropertyAccessorImpl<>(getter, setter, t -> true);
    }

    public static <T> PropertyAccessor<T> of(Supplier<T> getter, Consumer<T> setter, Predicate<T> predicate) {
        return new PropertyAccessorImpl<>(getter, setter, predicate);
    }

    private static class PropertyAccessorImpl<T> extends PropertyAccessor<T> {

        private final Supplier<T> getter;
        private final Consumer<T> setter;
        private final Predicate<T> predicate;

        PropertyAccessorImpl(Supplier<T> getter, Consumer<T> setter, Predicate<T> predicate) {
            this.getter = getter;
            this.setter = setter;
            this.predicate = predicate;
        }

        @Override
        public T get() { return getter.get(); }

        @Override
        protected void setValue(T value) { setter.accept(value); }

        @Override
        public boolean available(T value) { return predicate.test(value); }
    }
}
