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

public class AttributeValueIndex extends IndexTypeImpl<AttributeValueIndex> {
    // kInvalidAttributeValueIndex
    public static final AttributeValueIndex INVALID = new AttributeValueIndex(-1);

    private static final IndexTypeManager<AttributeValueIndex> ARRAY_MANAGER = AttributeValueIndex::of;
    public static DataType<AttributeValueIndex> type() { return ARRAY_MANAGER; }

    public static AttributeValueIndex of(int value) {
        return value == -1 ? INVALID : new AttributeValueIndex(value);
    }
    public static Iterable<AttributeValueIndex> range(int start, int until) {
        AttributeValueIndex startIdx = of(start);
        AttributeValueIndex untilIdx = of(until);
        return () -> startIdx.until(untilIdx);
    }

    private AttributeValueIndex(int value) { super(value); }
    @Override protected AttributeValueIndex newInstance(int value) { return of(value); }
    @Override public boolean isInvalid() { return this.getValue() == INVALID.getValue(); }
}
