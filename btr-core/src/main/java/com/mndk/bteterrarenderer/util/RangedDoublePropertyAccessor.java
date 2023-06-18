package com.mndk.bteterrarenderer.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.function.*;

public interface RangedDoublePropertyAccessor extends PropertyAccessor.Ranged<Double> {

    static RangedDoublePropertyAccessor of(DoubleSupplier getter, DoubleConsumer setter,
                                           double min, double max) {
        return new RangedDoublePropertyAccessorImpl(getter, setter, v -> v >= min && v <= max, min, max);
    }

    static RangedDoublePropertyAccessor of(DoubleSupplier getter, DoubleConsumer setter, DoublePredicate predicate,
                                           double min, double max) {
        return new RangedDoublePropertyAccessorImpl(getter, setter, v -> v >= min && v <= max && predicate.test(v), min, max);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class RangedDoublePropertyAccessorImpl implements RangedDoublePropertyAccessor {

        private final DoubleSupplier getter;
        private final DoubleConsumer setter;
        private final DoublePredicate predicate;
        private final double min, max;

        @Override
        public Class<Double> getPropertyClass() {
            return Double.class;
        }

        @Override
        public Double get() {
            return getter.getAsDouble();
        }

        @Override
        public void setWithoutCheck(Double value) {
            setter.accept(value);
        }

        @Override
        public boolean available(Double value) {
            return predicate.test(value);
        }

        @Override
        public Double min() {
            return min;
        }

        @Override
        public Double max() {
            return max;
        }
    }
}
