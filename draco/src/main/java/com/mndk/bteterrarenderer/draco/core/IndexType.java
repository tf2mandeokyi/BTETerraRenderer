/*
 * Copyright (C) 2024 The Draco Authors (for providing the original C++ code)
 * Copyright (C) 2024 m4ndeokyi (for translating the code into Java)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mndk.bteterrarenderer.draco.core;

import javax.annotation.Nonnull;
import java.util.Iterator;

public interface IndexType<I extends IndexType<I>> extends Comparable<I> {

    int getValue();
    I add(int other);
    I subtract(int other);
    boolean isInvalid();
    Iterator<I> until(I end);

    default I add(I other) { return this.add(other.getValue()); }
    default I subtract(I other) { return this.subtract(other.getValue()); }
    default I increment() { return this.add(1); }
    default boolean isValid() { return !isInvalid(); }

    @Override default int compareTo(@Nonnull I o) {
        return Integer.compare(getValue(), o.getValue());
    }
}
