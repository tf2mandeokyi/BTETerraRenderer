package com.mndk.bteterrarenderer.core.util.accessor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Range;

import javax.annotation.Nullable;
import java.util.function.*;

public interface PropertyAccessor<T> {

    default void set(T value) {
        if(this.isAvailable(value)) {
            this.setWithoutCheck(value);
        }
    }

    Class<T> getPropertyClass();
    T get();
    void setWithoutCheck(T value);
    /** Returns whether the given value can be set to the property */
    boolean isAvailable(T value);
    @Nullable Range<T> getRange();

    static PropertyAccessor<Integer> of(IntSupplier getter, IntConsumer setter) {
        return new PropertyAccessorImpl<>(int.class, getter::getAsInt, setter::accept, t -> true, null);
    }

    static PropertyAccessor<Integer> ranged(IntSupplier getter, IntConsumer setter, int min, int max) {
        return new PropertyAccessorImpl<>(int.class, getter::getAsInt, setter::accept, t -> true, Range.between(min, max));
    }

    static PropertyAccessor<Integer> ranged(IntSupplier getter, IntConsumer setter, Predicate<Integer> predicate, int min, int max) {
        return new PropertyAccessorImpl<>(int.class, getter::getAsInt, setter::accept, predicate, Range.between(min, max));
    }

    static PropertyAccessor<Double> of(DoubleSupplier getter, DoubleConsumer setter) {
        return new PropertyAccessorImpl<>(double.class, getter::getAsDouble, setter::accept, t -> true, null);
    }

    static PropertyAccessor<Double> ranged(DoubleSupplier getter, DoubleConsumer setter, double min, double max) {
        return new PropertyAccessorImpl<>(double.class, getter::getAsDouble, setter::accept, t -> true, Range.between(min, max));
    }

    static PropertyAccessor<Double> ranged(DoubleSupplier getter, DoubleConsumer setter, double min, double max, Predicate<Double> predicate) {
        return new PropertyAccessorImpl<>(double.class, getter::getAsDouble, setter::accept, predicate, Range.between(min, max));
    }

    static PropertyAccessor<Boolean> of(Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return new PropertyAccessorImpl<>(boolean.class, getter, setter, t -> true, null);
    }

    static <T> PropertyAccessor<T> of(Class<T> type, Supplier<T> getter, Consumer<T> setter) {
        return new PropertyAccessorImpl<>(type, getter, setter, t -> true, null);
    }

    static <T> PropertyAccessor<T> of(Class<T> type, Supplier<T> getter, Consumer<T> setter, Predicate<T> predicate) {
        return new PropertyAccessorImpl<>(type, getter, setter, predicate, null);
    }

    static <T> PropertyAccessor.Localized<T> localized(String key, String i18nKey, PropertyAccessor<T> delegate) {
        return new Localized<>(key, i18nKey, delegate);
    }

    @Deprecated
    interface Ranged<T extends Number> extends PropertyAccessor<T> {
        T min();
        T max();
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class PropertyAccessorImpl<T> implements PropertyAccessor<T> {
        private final Class<T> propertyClass;
        private final Supplier<T> getter;
        private final Consumer<T> setter;
        private final Predicate<T> predicate;
        @Nullable private final Range<T> range;

        @Override public T get() { return getter.get(); }
        @Override public void setWithoutCheck(T value) { setter.accept(value); }
        @Override public boolean isAvailable(T value) {
            if(range != null && !range.contains(value)) return false;
            return predicate.test(value);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class Localized<T> implements PropertyAccessor<T> {
        @Getter private final String key;
        @Getter private final String i18nKey;
        public final PropertyAccessor<T> delegate;

        @Override public Class<T> getPropertyClass() { return delegate.getPropertyClass(); }
        @Override public T get() { return delegate.get(); }
        @Override public void set(T value) { delegate.set(value); }
        @Override public void setWithoutCheck(T value) { delegate.setWithoutCheck(value); }
        @Override public boolean isAvailable(T value) { return delegate.isAvailable(value); }
        @Override public Range<T> getRange() { return delegate.getRange(); }
    }
}
