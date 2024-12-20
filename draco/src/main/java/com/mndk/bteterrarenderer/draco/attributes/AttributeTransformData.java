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
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.core.DataBuffer;
import lombok.Getter;
import lombok.Setter;

/**
 * Class for holding parameter values for an attribute transform of a
 * {@link PointAttribute}. This can be for example quantization data for an attribute
 * that holds quantized values. This class provides only a basic storage for
 * attribute transform parameters, and it should be accessed only through wrapper
 * classes for a specific transform (e.g. {@link AttributeQuantizationTransform}).
 */
public class AttributeTransformData {

    @Getter @Setter
    private AttributeTransformType transformType = AttributeTransformType.INVALID;
    private final DataBuffer buffer = new DataBuffer();

    public AttributeTransformData() {}
    public AttributeTransformData(AttributeTransformData data) {
        this.transformType = data.transformType;
        this.buffer.update(data.buffer);
    }

    /** Returns a parameter value at a given byte offset. */
    public <T> T getParameterValue(DataType<T> dataType, long byteOffset) {
        Pointer<T> outData = dataType.newOwned();
        buffer.read(byteOffset, outData);
        return outData.get();
    }

    /** Sets a parameter value at a given byte offset. */
    public <T> void setParameterValue(DataType<T> dataType, long byteOffset, T data) {
        if (byteOffset + dataType.byteSize() > buffer.size()) {
            buffer.resize(byteOffset + dataType.byteSize());
        }
        buffer.write(byteOffset, dataType, data);
    }

    /** Sets a parameter value at the end of the buffer. */
    public <T> void appendParameterValue(DataType<T> dataType, T data) {
        setParameterValue(dataType, buffer.size(), data);
    }
}
