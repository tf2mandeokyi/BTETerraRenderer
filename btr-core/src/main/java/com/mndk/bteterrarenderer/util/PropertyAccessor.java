package com.mndk.bteterrarenderer.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.*;

public interface PropertyAccessor<T> {

    default void set(T value) {
        if(this.available(value)) {
            this.setWithoutCheck(value);
        }
    }

    Class<T> getPropertyClass();
    T get();
    void setWithoutCheck(T value);
    boolean available(T value);

    static PropertyAccessor<Integer> of(IntSupplier getter, IntConsumer setter) {
        return new PropertyAccessorImpl<>(int.class, getter::getAsInt, setter::accept, t -> true);
    }

    static PropertyAccessor<Double> of(DoubleSupplier getter, DoubleConsumer setter) {
        return new PropertyAccessorImpl<>(double.class, getter::getAsDouble, setter::accept, t -> true);
    }

    static <T> PropertyAccessor<T> of(Class<T> type, Supplier<T> getter, Consumer<T> setter) {
        return new PropertyAccessorImpl<>(type, getter, setter, t -> true);
    }

    static <T> PropertyAccessor<T> of(Class<T> type, Supplier<T> getter, Consumer<T> setter, Predicate<T> predicate) {
        return new PropertyAccessorImpl<>(type, getter, setter, predicate);
    }

    interface Ranged<T extends Number> extends PropertyAccessor<T> {
        T min();
        T max();
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class PropertyAccessorImpl<T> implements PropertyAccessor<T> {
        @Getter
        private final Class<T> propertyClass;
        private final Supplier<T> getter;
        private final Consumer<T> setter;
        private final Predicate<T> predicate;

        @Override
        public T get() { return getter.get(); }

        @Override
        public void setWithoutCheck(T value) { setter.accept(value); }

        @Override
        public boolean available(T value) { return predicate.test(value); }
    }

    @RequiredArgsConstructor
    class Localized<T> {
        public final String key;
        public final String i18nKey;
        public final PropertyAccessor<T> delegate;
    }
}
