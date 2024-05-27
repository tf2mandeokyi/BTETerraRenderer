package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.draco.core.DataType;
import com.mndk.bteterrarenderer.draco.util.DracoCompressionException;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * This class provides access to a specific attribute which is stored in a
 * {@link ByteBuf}, such as normals or coordinates. However, the {@link GeometryAttribute}
 * class does not own the buffer and the buffer itself may store other data
 * unrelated to this attribute (such as data for other attributes in which case
 * we can have multiple {@link GeometryAttribute}s accessing one buffer). Typically,
 * all attributes for a point (or corner, face) are stored in one block, which
 * is advantageous in terms of memory access. The length of the entire block is
 * given by the {@link GeometryAttribute#byteStride}, the position where the attribute starts is given by
 * the {@link GeometryAttribute#byteOffset}, the actual number of bytes that the attribute occupies is
 * given by the {@link GeometryAttribute#dataType} and the number of components.
 *
 * TODO: Add bufferDescriptor field
 */
@Getter
@Setter
public class GeometryAttribute {

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        INVALID(-1, "INVALID"),
        POSITION(0, "POSITION"),
        NORMAL(1, "NORMAL"),
        COLOR(2, "COLOR"),
        TEX_COORD(3, "TEX_COORD"),
        GENERIC(4, "GENERIC"),
        NAMED_ATTRIBUTES(5, "NAMED_ATTRIBUTES");

        private final int value;
        private final String string;

        // GeometryAttribute.TypeToString()
        @Override
        public String toString() {
            return this.string;
        }
    }

    private ByteBuf buffer = null;
    private short numComponents = 1;
    private DataType<?> dataType = DataType.FLOAT32;
    private boolean normalized;
    private int byteStride = 0;
    private int byteOffset = 0;
    private Type attributeType = Type.INVALID;
    private int uniqueId = 0;

    public GeometryAttribute() {}

    /** Initializes and enables the attribute. */
    public void init(Type attributeType, ByteBuf buffer, short numComponents, DataType<?> dataType,
                     boolean normalized, int byteStride, int byteOffset)
    {
        this.buffer = buffer;
        this.numComponents = numComponents;
        this.dataType = dataType;
        this.normalized = normalized;
        this.byteStride = byteStride;
        this.byteOffset = byteOffset;
        this.attributeType = attributeType;
    }

    public boolean isValid() {
        return buffer != null;
    }

    /**
     * Copies data from the source attribute to this attribute.
     * This attribute must have a valid buffer allocated otherwise the operation is going to fail.
     */
    public void copyFrom(GeometryAttribute srcAtt) throws DracoCompressionException {
        this.numComponents = srcAtt.numComponents;
        this.dataType = srcAtt.dataType;
        this.normalized = srcAtt.normalized;
        this.byteStride = srcAtt.byteStride;
        this.byteOffset = srcAtt.byteOffset;
        this.attributeType = srcAtt.attributeType;
        if(srcAtt.buffer == null) {
            this.buffer = null;
        }
        else {
            if(buffer == null) {
                throw new DracoCompressionException("this buffer is null");
            }
            buffer.setBytes(0, srcAtt.buffer, srcAtt.buffer.readableBytes());
        }
    }

    /**
     * Function for getting attribute value with a specific format.
     * Unsafe. Caller must ensure the accessed memory is valid.
     * @param <T> attribute data type.
     * @param attComponents the number of attribute components.
     */
    public <T> T[] getValue(AttributeValueIndex attIndex, DataType<T> outType, int attComponents)
            throws DracoCompressionException
    {
        T[] out = outType.newArray(attComponents);
        this.getValue(attIndex, outType, out);
        return out;
    }

    /**
     * Function for getting attribute value with a specific format.
     * @param <T> the attribute data type.
     * @param out Preset array of which the length should be equal to
     *           the number of attribute components.
     */
    public <T> void getValue(AttributeValueIndex attIndex, DataType<T> outType, T[] out)
            throws DracoCompressionException
    {
        int bytePos = this.getBytePos(attIndex);
        int byteSize = outType.getByteSize();
        if(bytePos + byteSize * out.length > buffer.readableBytes()) {
            throw new DracoCompressionException("data overflow");
        }
        for(int i = 0; i < out.length; i++) {
            out[i] = outType.getBuf(buffer, i * byteSize);
        }
    }

    /** Returns the byte position of the attribute entry in the data buffer. */
    public int getBytePos(AttributeValueIndex attIndex) {
        return byteOffset + byteStride * attIndex.getValue();
    }

    /**
     * Returns data with the value of the requested attribute entry.
     * outType must be less or equal to {@link GeometryAttribute#byteStride} in size.
     */
    public <T> T getValue(AttributeValueIndex attIndex, DataType<T> outType) throws DracoCompressionException {
        int bytePos = this.getBytePos(attIndex);
        return outType.getBuf(buffer, bytePos);
    }

    /**
     * Sets a value of an attribute entry. The input value must be allocated (done in Java by default) to
     * cover all components of a single attribute entry.
     */
    public <T> void setAttributeValue(AttributeValueIndex entryIndex, DataType<T> outType, T value)
            throws DracoCompressionException
    {
        int bytePos = this.getBytePos(entryIndex);
        outType.setBuf(buffer, bytePos, value);
    }

    /**
     * Function for conversion of an attribute to a specific output format.
     *
     * @param outType the desired data type of the attribute.
     * @param outVal  needs to be able to store outNumComponents values.
     * @throws DracoCompressionException when the conversion fails.
     */
    public <OutT> void convertValue(AttributeValueIndex attId, short outNumComponents,
                                    DataType<OutT> outType, OutT[] outVal)
            throws DracoCompressionException
    {
        if(outVal == null) throw new DracoCompressionException("out value is null");
        this.convertTypedValue(attId, outNumComponents, outType, outVal);
    }

    /**
     * Function for conversion of an attribute to a specific output format.
     * @param outValue must be able to store all components of a single attribute entry.
     * @param outType the desired data type of the attribute.
     * @throws DracoCompressionException when the conversion fails.
     */
    public <OutT> void convertValue(AttributeValueIndex attIndex, DataType<OutT> outType, OutT[] outValue)
            throws DracoCompressionException
    {
        this.convertValue(attIndex, this.numComponents, outType, outValue);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(!(obj instanceof GeometryAttribute)) return false;

        GeometryAttribute other = (GeometryAttribute) obj;
        if(this.numComponents != other.numComponents) return false;
        if(this.dataType != other.dataType) return false;
        if(this.byteStride != other.byteStride) return false;
        return this.byteOffset == other.byteOffset;
    }

    /** Sets a new internal storage for the attribute. */
    protected void resetBuffer(ByteBuf buffer, int byteStride, int byteOffset) {
        this.buffer = buffer;
        this.byteStride = byteStride;
        this.byteOffset = byteOffset;
    }

    /**
     * Function for conversion of an attribute to a specific output format given a
     * format of the stored attribute.
     * @param outType the desired data type of the attribute.
     */
    private <OutT> void convertTypedValue(AttributeValueIndex attId, short outNumComponents,
                                          DataType<OutT> outType, OutT[] outValue)
            throws DracoCompressionException
    {
        int byteSize = this.dataType.getByteSize();
        // Convert all components available in both the original and output formats.
        for(int i = 0; i < Math.min(this.numComponents, outNumComponents); i++) {
            int bytePos = this.getBytePos(attId);
            outValue[i] = convertComponentValue(this.dataType, bytePos + i * byteSize, outType, this.normalized);
        }
        // Fill empty data for unused output components if needed.
        for(int i = this.numComponents; i < outNumComponents; i++) {
            outValue[i] = outType.staticCast(0);
        }
    }

    /**
     * Returns value of type OutT(outType) from inValue of type T(inType). If
     * normalized is true, any conversion between floating point and integer
     * values will be treating integers as normalized types (the entire integer
     * range will be used to represent 0-1 floating point range).
     */
    private <T, OutT> OutT convertComponentValue(DataType<T> inType, int byteIndex,
                                                 DataType<OutT> outType, boolean normalized)
            throws DracoCompressionException
    {
        T inValue = inType.getBuf(buffer, byteIndex);
        // Make sure inValue can be represented as an integral type U.
        if(outType.isIntegral()) {
            // Make sure inValue fits thin the range of values that U
            // is able to represent. Perform the check only for integral types.
            if(outType != DataType.BOOL && inType.isIntegral()) {
                OutT kOutMin = inType.isSigned() ? outType.min() : outType.staticCast(0);
                if(inType.lt(inValue, kOutMin) || inType.gt(inValue, outType.max())) {
                    throw new DracoCompressionException("inValue out of range");
                }
            }

            // Check conversion of floating point inValue to integral value OutT.
            if(inType.isFloatingPoint()) {
                // Make sure the floating point inValue is not NaN and not Infinity as
                // integral type OutT is unable to represent these values.
                // Since there's no float128 in java, we'll skip checking the type for it.
                if(inType.getByteSize() == DataType.FLOAT64.getByteSize()) {
                    double value = DataType.FLOAT64.unsafeCast(inValue);
                    if(Double.isNaN(value) || Double.isInfinite(value)) {
                        throw new DracoCompressionException("not a valid floating point value");
                    }
                }
                else if(inType.getByteSize() == DataType.FLOAT32.getByteSize()) {
                    float value = DataType.FLOAT32.unsafeCast(inValue);
                    if(Float.isNaN(value) || Float.isInfinite(value)) {
                        throw new DracoCompressionException("not a valid floating point value");
                    }
                }
                else throw new DracoCompressionException("unsupported floating point size");

                // Make sure the floating point inValue fits within the range of
                // values that integral type OutT is able to present.
                if(inType.lt(inValue, outType.min()) || inType.gte(inValue, outType.max())) {
                    throw new DracoCompressionException("inValue out of range");
                }
            }
        }

        if(inType.isIntegral() && outType.isFloatingPoint() && normalized) {
            // When converting integer to floating point, normalize the value if
            // necessary.
            OutT outValue = outType.staticCast(inValue);
            return outType.divide(outValue, outType.staticCast(inType.max()));
        }
        else if(inType.isFloatingPoint() && outType.isIntegral() && normalized) {
            // Converting from floating point to a normalized integer.
            if(inType.gt(inValue, inType.staticCast(1)) || inType.lt(inValue, inType.staticCast(0))) {
                // Normalized float values need to be between 0 and 1.
                throw new DracoCompressionException("input value outside normalized range: " + inValue);
            }
            // From Google's repository: "Consider allowing float to normalized integer conversion
            // for 64-bit integer types. It doesn't work currently because we don't
            // have a floating point type that could store all 64-bit integers."
            if(outType.getByteSize() > 4) {
                throw new DracoCompressionException("output type size bigger than 4");
            }
            // Expand the float to the range of the output integer and round it to the
            // nearest representable value. Use doubles for the math to ensure the
            // integer values are represented properly during the conversion process.
            return outType.floor(outType.add(
                    outType.multiply(outType.staticCast(inValue), outType.max()),
                    outType.staticCast(0.5)
            ));
        }
        else {
            return outType.staticCast(inType);
        }
        // From Google's repository: "Add handling of normalized attributes when converting
        // between different integer representations. If the attribute is
        // normalized, integer values should be converted as if they represent 0-1
        // range. E.g. when we convert UINT16 to UINT8, the range <0, 65535>
        // should be converted to range <0, 255>"
    }
}
