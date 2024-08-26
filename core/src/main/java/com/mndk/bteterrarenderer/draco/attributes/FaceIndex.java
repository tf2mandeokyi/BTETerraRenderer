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

public class FaceIndex extends IndexTypeImpl<FaceIndex> {
    // kInvalidFaceIndex
    public static final FaceIndex INVALID = new FaceIndex(-1);

    private static final IndexTypeManager<FaceIndex> ARRAY_MANAGER = FaceIndex::of;
    public static DataType<FaceIndex> type() { return ARRAY_MANAGER; }

    public static FaceIndex of(int value) {
        return value == -1 ? INVALID : new FaceIndex(value);
    }
    public static Iterable<FaceIndex> range(int start, int until) {
        FaceIndex startIdx = of(start);
        FaceIndex untilIdx = of(until);
        return () -> startIdx.until(untilIdx);
    }

    private FaceIndex(int value) { super(value); }
    @Override protected FaceIndex newInstance(int value) { return of(value); }
    @Override public boolean isInvalid() { return this.getValue() == INVALID.getValue(); }
}
