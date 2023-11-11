package com.mndk.bteterrarenderer.core.util.accessor;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.function.*;

public interface RangedIntPropertyAccessor extends PropertyAccessor.Ranged<Integer> {

    static RangedIntPropertyAccessor of(IntSupplier getter, IntConsumer setter,
                                        int min, int max) {
        return new RangedIntPropertyAccessorImpl(getter, setter, v -> v >= min && v <= max, min, max);
    }

    static RangedIntPropertyAccessor of(IntSupplier getter, IntConsumer setter, IntPredicate predicate,
                                        int min, int max) {
        return new RangedIntPropertyAccessorImpl(getter, setter, v -> v >= min && v <= max && predicate.test(v), min, max);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class RangedIntPropertyAccessorImpl implements RangedIntPropertyAccessor {

        private final IntSupplier getter;
        private final IntConsumer setter;
        private final IntPredicate predicate;
        private final int min, max;

        @Override
        public Class<Integer> getPropertyClass() {
            return Integer.class;
        }

        @Override
        public Integer get() {
            return getter.getAsInt();
        }

        @Override
        public void setWithoutCheck(Integer value) {
            setter.accept(value);
        }

        @Override
        public boolean available(Integer value) {
            return predicate.test(value);
        }

        @Override
        public Integer min() {
            return min;
        }

        @Override
        public Integer max() {
            return max;
        }
    }
}
