package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.draco.core.DataBuffer;
import com.mndk.bteterrarenderer.datatype.DataIOManager;
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
    private AttributeTransformType transformType = AttributeTransformType.ATTRIBUTE_INVALID_TRANSFORM;
    private final DataBuffer buffer = new DataBuffer();

    public AttributeTransformData() {}
    public AttributeTransformData(AttributeTransformData data) {
        this.transformType = data.transformType;
        this.buffer.update(data.buffer);
    }

    /** Returns a parameter value at a given byte offset. */
    public <T> T getParameterValue(DataIOManager<T> dataType, long byteOffset) {
        return buffer.read(dataType, byteOffset);
    }

    /** Sets a parameter value at a given byte offset. */
    public <T> void setParameterValue(DataIOManager<T> dataType, long byteOffset, T data) {
        if(byteOffset + dataType.size() > buffer.size()) {
            buffer.resize(byteOffset + dataType.size());
        }
        buffer.write(dataType, byteOffset, data);
    }

    /** Sets a parameter value at the end of the buffer. */
    public <T> void appendParameterValue(DataIOManager<T> dataType, T data) {
        setParameterValue(dataType, buffer.size(), data);
    }
}
