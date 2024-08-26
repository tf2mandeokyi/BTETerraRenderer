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

package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.draco.core.IndexTypeImpl;

public class PointIndex extends IndexTypeImpl<PointIndex> {
    // kInvalidPointIndex
    public static final PointIndex INVALID = new PointIndex(-1);

    private static final IndexTypeManager<PointIndex> ARRAY_MANAGER = PointIndex::of;
    public static DataType<PointIndex> type() { return ARRAY_MANAGER; }

    public static PointIndex of(int value) {
        return value == -1 ? INVALID : new PointIndex(value);
    }
    public static Iterable<PointIndex> range(int start, int until) {
        PointIndex startIdx = of(start);
        PointIndex untilIdx = of(until);
        return () -> startIdx.until(untilIdx);
    }

    private PointIndex(int value) { super(value); }
    @Override protected PointIndex newInstance(int value) { return of(value); }
    @Override public boolean isInvalid() { return this.getValue() == INVALID.getValue(); }
}
