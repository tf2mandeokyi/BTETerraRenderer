package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.draco.core.DataBuffer;
import com.mndk.bteterrarenderer.draco.core.DataType;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.util.DracoCompressionException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * This class provides access to a specific attribute which is stored in a
 * {@link DataBuffer}, such as normals or coordinates. However, the {@link GeometryAttribute}
 * class does not own the buffer and the buffer itself may store other data
 * unrelated to this attribute (such as data for other attributes in which case
 * we can have multiple {@link GeometryAttribute}s accessing one buffer). Typically,
 * all attributes for a point (or corner, face) are stored in one block, which
 * is advantageous in terms of memory access. The length of the entire block is
 * given by the {@link GeometryAttribute#byteStride}, the position where the attribute starts is given by
 * the {@link GeometryAttribute#byteOffset}, the actual number of bytes that the attribute occupies is
 * given by the {@link GeometryAttribute#dataType} and the number of components.<br>
 * <br>
 * TODO: Add bufferDescriptor field
 */
@Getter
@Setter
public class GeometryAttribute {

    public static final int NAMED_ATTRIBUTES_COUNT = (int) Arrays.stream(Type.values())
            .filter(type -> type != Type.INVALID).count();

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        INVALID(-1, "INVALID"),
        POSITION(0, "POSITION"),
        NORMAL(1, "NORMAL"),
        COLOR(2, "COLOR"),
        TEX_COORD(3, "TEX_COORD"),
        GENERIC(4, "GENERIC");

        private final int index;
        private final String string;

        // GeometryAttribute.TypeToString()
        @Override
        public String toString() {
            return this.string;
        }

        // Search by string
        public static Type fromString(String string) {
            for(Type type : values()) {
                if(type.string.equals(string)) return type;
            }
            return INVALID;
        }
    }

    protected DataBuffer buffer = null;
    private short numComponents = 1;
    private DataType<?> dataType = DataType.FLOAT32;
    private boolean normalized;
    private int byteStride = 0;
    private int byteOffset = 0;
    private Type attributeType = Type.INVALID;
    private int uniqueId = 0;

    public GeometryAttribute() {}
    public GeometryAttribute(GeometryAttribute att) {
        this.copyFrom(att);
        this.uniqueId = att.uniqueId;
    }

    /** Initializes and enables the attribute. */
    public final void init(Type attributeType, DataBuffer buffer, short numComponents, DataType<?> dataType,
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

    public final boolean isValid() {
        return buffer != null;
    }

    /**
     * Copies data from the source attribute to this attribute.
     * This attribute must have a valid buffer allocated otherwise the operation is going to fail.
     */
    public final Status copyFrom(GeometryAttribute srcAtt) {
        this.numComponents = srcAtt.numComponents;
        this.dataType = srcAtt.dataType;
        this.normalized = srcAtt.normalized;
        this.byteStride = srcAtt.byteStride;
        this.byteOffset = srcAtt.byteOffset;
        this.attributeType = srcAtt.attributeType;
        this.uniqueId = srcAtt.uniqueId;
        if(srcAtt.buffer == null) {
            this.buffer = null;
        }
        else {
            if(this.buffer == null) {
                return new Status(Status.Code.INVALID_PARAMETER, "buffer is null");
            }
            // Copy buffer data
            this.buffer.update(srcAtt.buffer);
        }
        return Status.OK;
    }

    /**
     * Function for getting attribute value with a specific format.
     * @param <T> the attribute data type.
     * @param out Preset array of which the length should be equal to
     *           the number of attribute components.
     */
    public <T> Status getValue(AttributeValueIndex attIndex, DataType<T> outType,
                                 BiConsumer<Integer, T> out, int attComponents) {
        int bytePos = this.getBytePos(attIndex);
        int byteSize = outType.size();
        if(bytePos + byteSize * attComponents > buffer.size()) {
            return new Status(Status.Code.IO_ERROR, "buffer capacity exceeded");
        }
        for(int i = 0; i < attComponents; i++) out.accept(i, outType.getBuf(buffer, bytePos + i * byteSize));
        return Status.OK;
    }
    public final <T> Status getValue(AttributeValueIndex attIndex, DataType<T> outType, T[] out) {
        return this.getValue(attIndex, outType, out, out.length);
    }
    public final <T> Status getValue(AttributeValueIndex attIndex, DataType<T> outType, T[] out, int outLength) {
        return this.getValue(attIndex, outType, (i, val) -> out[i] = val, outLength);
    }
    public final Status getValue(AttributeValueIndex attIndex, DataType<Byte> outType, byte[] out) {
        return this.getValue(attIndex, outType, out, out.length);
    }
    public final Status getValue(AttributeValueIndex attIndex, DataType<Byte> outType, byte[] out, int outLength) {
        return this.getValue(attIndex, outType, (i, val) -> out[i] = val, outLength);
    }
    public final Status getValue(AttributeValueIndex attIndex, DataType<Short> outType, short[] out) {
        return this.getValue(attIndex, outType, out, out.length);
    }
    public final Status getValue(AttributeValueIndex attIndex, DataType<Short> outType, short[] out, int outLength) {
        return this.getValue(attIndex, outType, (i, val) -> out[i] = val, outLength);
    }
    public final Status getValue(AttributeValueIndex attIndex, DataType<Integer> outType, int[] out) {
        return this.getValue(attIndex, outType, out, out.length);
    }
    public final Status getValue(AttributeValueIndex attIndex, DataType<Integer> outType, int[] out, int outLength) {
        return this.getValue(attIndex, outType, (i, val) -> out[i] = val, outLength);
    }
    public final Status getValue(AttributeValueIndex attIndex, DataType<Long> outType, long[] out) {
        return this.getValue(attIndex, outType, out, out.length);
    }
    public final Status getValue(AttributeValueIndex attIndex, DataType<Long> outType, long[] out, int outLength) {
        return this.getValue(attIndex, outType, (i, val) -> out[i] = val, outLength);
    }
    public final Status getValue(AttributeValueIndex attIndex, DataType<Float> outType, float[] out) {
        return this.getValue(attIndex, outType, out, out.length);
    }
    public final Status getValue(AttributeValueIndex attIndex, DataType<Float> outType, float[] out, int outLength) {
        return this.getValue(attIndex, outType, (i, val) -> out[i] = val, outLength);
    }
    public final Status getValue(AttributeValueIndex attIndex, DataType<Double> outType, double[] out) {
        return this.getValue(attIndex, outType, out, out.length);
    }
    public final Status getValue(AttributeValueIndex attIndex, DataType<Double> outType, double[] out, int outLength) {
        return this.getValue(attIndex, outType, (i, val) -> out[i] = val, outLength);
    }
    public final <T> List<T> getValue(AttributeValueIndex attIndex, DataType<T> outType, int attComponents) {
        List<T> out = new ArrayList<>();
        for(int i = 0; i < attComponents; i++) out.add(null);
        this.getValue(attIndex, outType, out::set, attComponents);
        return out;
    }

    /** Returns the byte position of the attribute entry in the data buffer. */
    public final int getBytePos(AttributeValueIndex attIndex) {
        return byteOffset + byteStride * attIndex.getValue();
    }

    // No getAddress() here!!
    // No isAddressValid() here!!

    /**
     * Returns data with the value of the requested attribute entry.
     * outType must be less or equal to {@link GeometryAttribute#byteStride} in size.
     */
    public final <T> T getValue(AttributeValueIndex attIndex, DataType<T> outType) {
        int bytePos = this.getBytePos(attIndex);
        return outType.getBuf(buffer, bytePos);
    }

    /**
     * Sets a value of an attribute entry. The input value must be allocated (done in Java by default) to
     * cover all components of a single attribute entry.
     */
    public final <T> void setAttributeValue(AttributeValueIndex entryIndex, DataType<T> outType, T value) {
        int bytePos = this.getBytePos(entryIndex);
        outType.setBuf(buffer, bytePos, value);
    }

    public <T> void setAttributeValue(AttributeValueIndex entryIndex, DataType<T> inType,
                                      Function<Integer, T> in, int attComponents) {
        int bytePos = this.getBytePos(entryIndex);
        int byteSize = inType.size();
        for(int i = 0; i < attComponents; i++) {
            inType.setBuf(buffer, bytePos + i * byteSize, in.apply(i));
        }
    }
    public final <T> void setAttributeValue(AttributeValueIndex entryIndex, DataType<T> inType, int index, T value) {
        int bytePos = this.getBytePos(entryIndex);
        inType.setBuf(buffer, bytePos + index * inType.size(), value);
    }
    public final void setAttributeValue(AttributeValueIndex entryIndex, DataType<Byte> inType, byte[] value) {
        this.setAttributeValue(entryIndex, inType, i -> value[i], value.length);
    }
    public final void setAttributeValue(AttributeValueIndex entryIndex, DataType<Short> inType, short[] value) {
        this.setAttributeValue(entryIndex, inType, i -> value[i], value.length);
    }
    public final void setAttributeValue(AttributeValueIndex entryIndex, DataType<Integer> inType, int[] value) {
        this.setAttributeValue(entryIndex, inType, i -> value[i], value.length);
    }
    public final void setAttributeValue(AttributeValueIndex entryIndex, DataType<Long> inType, long[] value) {
        this.setAttributeValue(entryIndex, inType, i -> value[i], value.length);
    }
    public final void setAttributeValue(AttributeValueIndex entryIndex, DataType<BigInteger> inType, BigInteger[] value) {
        this.setAttributeValue(entryIndex, inType, i -> value[i], value.length);
    }
    public final void setAttributeValue(AttributeValueIndex entryIndex, DataType<Float> inType, float[] value) {
        this.setAttributeValue(entryIndex, inType, i -> value[i], value.length);
    }
    public final void setAttributeValue(AttributeValueIndex entryIndex, DataType<Double> inType, double[] value) {
        this.setAttributeValue(entryIndex, inType, i -> value[i], value.length);
    }

    /**
     * Function for conversion of an attribute to a specific output format.
     *
     * @param outType the desired data type of the attribute.
     * @param outVal  needs to be able to store outNumComponents values.
     * @throws DracoCompressionException when the conversion fails.
     */
    public final <OutT> void convertValue(AttributeValueIndex attId, short outNumComponents,
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
    public final <OutT> void convertValue(AttributeValueIndex attIndex, DataType<OutT> outType, OutT[] outValue)
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

    @Override
    public int hashCode() {
        return Objects.hash(numComponents, dataType, attributeType, byteStride, byteOffset);
    }

    /** Sets a new internal storage for the attribute. */
    @SuppressWarnings("SameParameterValue")
    protected final void resetBuffer(DataBuffer buffer, int byteStride, int byteOffset) {
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
        int bytePos = this.getBytePos(attId);
        int byteSize = this.dataType.size();
        // Convert all components available in both the original and output formats.
        for(int i = 0; i < Math.min(this.numComponents, outNumComponents); i++) {
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
                if(inType.size() == DataType.FLOAT64.size()) {
                    double value = DataType.FLOAT64.unsafeCast(inValue);
                    if(Double.isNaN(value) || Double.isInfinite(value)) {
                        throw new DracoCompressionException("not a valid floating point value");
                    }
                }
                else if(inType.size() == DataType.FLOAT32.size()) {
                    float value = DataType.FLOAT32.unsafeCast(inValue);
                    if(Float.isNaN(value) || Float.isInfinite(value)) {
                        throw new DracoCompressionException("not a valid floating point value");
                    }
                }
                else throw new DracoCompressionException("unsupported floating point size");

                // Make sure the floating point inValue fits within the range of
                // values that integral type OutT is able to present.
                if(inType.lt(inValue, outType.min()) || inType.ge(inValue, outType.max())) {
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
            if(outType.size() > 4) {
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
