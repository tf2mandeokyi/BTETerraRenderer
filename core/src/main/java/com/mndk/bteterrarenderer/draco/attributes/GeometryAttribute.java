package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.datatype.DataIOManager;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.draco.core.*;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
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
            .filter(type -> type != Type.INVALID)
            .count();

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

        @Override
        public String toString() {
            return this.string;
        }

        public static Type valueOf(UByte value) {
            for(Type type : values()) {
                if(value.equals(type.index)) return type;
            }
            return INVALID;
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
    private UByte numComponents = UByte.of(1);
    private DracoDataType dataType = DracoDataType.DT_FLOAT32;
    private boolean normalized;
    private long byteStride = 0;
    private long byteOffset = 0;
    private Type attributeType = Type.INVALID;
    private UInt uniqueId = UInt.ZERO;

    public GeometryAttribute() {}
    public GeometryAttribute(GeometryAttribute att) {
        this.copyFrom(att);
        this.uniqueId = att.uniqueId;
    }

    /** Initializes and enables the attribute. */
    public final void init(Type attributeType, DataBuffer buffer, UByte numComponents, DracoDataType dataType,
                           boolean normalized, long byteStride, long byteOffset)
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
                return Status.invalidParameter("buffer is null");
            }
            // Copy buffer data
            this.buffer.update(srcAtt.buffer);
        }
        return Status.ok();
    }

    /**
     * Function for getting attribute value with a specific format.
     * @param <T> the attribute data type.
     * @param out Preset array of which the length should be equal to
     *           the number of attribute components.
     */
    public <T> Status getValue(AttributeValueIndex attIndex, DataIOManager<T> outType,
                               BiConsumer<Integer, T> out, int attComponents) {
        long bytePos = this.getBytePos(attIndex);
        long byteSize = outType.size();
        if(bytePos + byteSize * attComponents > buffer.size()) {
            return Status.ioError("buffer capacity exceeded");
        }
        for(int i = 0; i < attComponents; i++) {
            T value = buffer.read(outType, bytePos + i * byteSize);
            out.accept(i, value);
        }
        return Status.ok();
    }
    public final <T, TArray> Status getValue(AttributeValueIndex attIndex, DataType<T, TArray> outType, TArray out,
                                             int attComponents) {
        return this.getValue(attIndex, outType, outType.setter(out), attComponents);
    }
    public final <T, TArray> Status getValue(AttributeValueIndex attIndex, DataType<T, TArray> outType, TArray out) {
        return this.getValue(attIndex, outType, out, outType.length(out));
    }
    public final <T, TArray> CppVector<T> getValue(AttributeValueIndex attIndex, DataType<T, TArray> outType,
                                                   int attComponents) {
        TArray out = outType.newArray(attComponents);
        this.getValue(attIndex, outType, outType.setter(out), attComponents);
        return CppVector.view(outType, out);
    }

    /** Returns the byte position of the attribute entry in the data buffer. */
    public final long getBytePos(AttributeValueIndex attIndex) {
        return byteOffset + byteStride * attIndex.getValue();
    }

    /**
     * Returns data with the value of the requested attribute entry.
     * outType must be less or equal to {@link GeometryAttribute#byteStride} in size.
     */
    public final <T> T getValue(AttributeValueIndex attIndex, DataIOManager<T> outType) {
        long bytePos = this.getBytePos(attIndex);
        return buffer.read(outType, bytePos);
    }

    /**
     * Sets a value of an attribute entry. The input value must be allocated (done in Java by default) to
     * cover all components of a single attribute entry.
     */
    public final <T> void setAttributeValue(AttributeValueIndex entryIndex, DataIOManager<T> type, T value) {
        long bytePos = this.getBytePos(entryIndex);
        buffer.write(type, bytePos, value);
    }

    public <T> void setAttributeValues(AttributeValueIndex entryIndex, DataIOManager<T> inType, Function<Integer, T> in,
                                       int attComponents) {
        long bytePos = this.getBytePos(entryIndex);
        long byteSize = inType.size();
        for(int i = 0; i < attComponents; i++) {
            buffer.write(inType, bytePos + i * byteSize, in.apply(i));
        }
    }
    public <T, TArray>
    void setAttributeValues(AttributeValueIndex entryIndex, DataType<T, TArray> inType, TArray in, int attComponents) {
        this.setAttributeValues(entryIndex, inType, inType.getter(in), attComponents);
    }
    public <TArray>
    void setAttributeValues(AttributeValueIndex entryIndex, DataType<?, TArray> inType, TArray in) {
        this.setAttributeValues(entryIndex, inType, in, inType.length(in));
    }
    public final <T> void setAttributeValue(AttributeValueIndex entryIndex, DataIOManager<T> inType, long index, T value) {
        long bytePos = this.getBytePos(entryIndex);
        buffer.write(inType, bytePos + index * inType.size(), value);
    }

    /**
     * Function for conversion of an attribute to a specific output format.
     *
     * @param outType the desired data type of the attribute.
     * @param outVal  needs to be able to store outNumComponents values.
     */
    public final <TArray> void convertValue(AttributeValueIndex attId, UByte outNumComponents,
                                            DataNumberType<?, TArray> outType, TArray outVal)
    {
        if(outVal == null) throw new IllegalArgumentException("out value is null");
        this.convertTypedValue(attId, outNumComponents, outType, outVal);
    }

    /**
     * Function for conversion of an attribute to a specific output format.
     * @param outValue must be able to store all components of a single attribute entry.
     * @param outType the desired data type of the attribute.
     * @throws IllegalArgumentException when the conversion fails.
     */
    public final <TArray>
    void convertValue(AttributeValueIndex attIndex, DataNumberType<?, TArray> outType, TArray outValue)
    {
        this.convertValue(attIndex, this.numComponents, outType, outValue);
    }

    public final <TArray>
    void convertValue(AttributeValueIndex attIndex, VectorD<?, TArray, ?> outValue)
    {
        this.convertValue(attIndex, this.numComponents, outValue.getElementType(), outValue.getArray());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(!(obj instanceof GeometryAttribute)) return false;

        GeometryAttribute other = (GeometryAttribute) obj;
        if(!this.numComponents.equals(other.numComponents)) return false;
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
    protected final void resetBuffer(DataBuffer buffer, long byteStride, long byteOffset) {
        this.buffer = buffer;
        this.byteStride = byteStride;
        this.byteOffset = byteOffset;
    }

    /**
     * Function for conversion of an attribute to a specific output format given a
     * format of the stored attribute.
     * @param outType the desired data type of the attribute.
     */
    private <T, TArray> void convertTypedValue(AttributeValueIndex attId, UByte outNumComponents,
                                               DataNumberType<T, TArray> outType, TArray outValue)
    {
        long bytePos = this.getBytePos(attId);
        long byteSize = this.dataType.getDataTypeLength();
        // Convert all components available in both the original and output formats.
        for(int i = 0, until = UByte.min(this.numComponents, outNumComponents).intValue(); i < until; i++) {
            DataNumberType<?, ?> inType = this.dataType.getDataType();
            T value = convertComponentValue(inType, bytePos + i * byteSize, outType, this.normalized);
            outType.set(outValue, i, value);
        }
        // Fill empty data for unused output components if needed.
        for(int i = this.numComponents.intValue(), until = outNumComponents.intValue(); i < until; i++) {
            outType.set(outValue, i, outType.from(0));
        }
    }

    /**
     * Returns value of type OutT(outType) from inValue of type T(inType). If
     * normalized is true, any conversion between floating point and integer
     * values will be treating integers as normalized types (the entire integer
     * range will be used to represent 0-1 floating point range).
     */
    private <T, OutT> OutT convertComponentValue(DataNumberType<T, ?> inType, long byteIndex,
                                                 DataNumberType<OutT, ?> outType, boolean normalized)
    {
        T inValue = buffer.read(inType, byteIndex);
        // Make sure inValue can be represented as an integral type U.
        if(outType.isIntegral()) {
            // Make sure inValue fits thin the range of values that U
            // is able to represent. Perform the check only for integral types.
            if(outType != DataType.bool() && inType.isIntegral()) {
                OutT kOutMin = inType.isSigned() ? outType.min() : outType.from(0);
                if(inType.lt(inValue, outType, kOutMin) || inType.gt(inValue, outType, outType.max())) {
                    throw new IllegalArgumentException("inValue out of range");
                }
            }

            // Check conversion of floating point inValue to integral value OutT.
            if(inType.isFloatingPoint()) {
                // Make sure the floating point inValue is not NaN and not Infinity as
                // integral type OutT is unable to represent these values.
                // Since there's no float128 in java, we'll skip checking the type for it.
                if(inType.size() == DataType.float64().size()) {
                    double value = inType.toDouble(inValue);
                    if(Double.isNaN(value) || Double.isInfinite(value)) {
                        throw new IllegalArgumentException("not a valid floating point value");
                    }
                }
                else if(inType.size() == DataType.float32().size()) {
                    float value = inType.toFloat(inValue);
                    if(Float.isNaN(value) || Float.isInfinite(value)) {
                        throw new IllegalArgumentException("not a valid floating point value");
                    }
                }
                else throw new IllegalArgumentException("unsupported floating point size");

                // Make sure the floating point inValue fits within the range of
                // values that integral type OutT is able to present.
                if(inType.lt(inValue, outType, outType.min()) || inType.ge(inValue, outType, outType.max())) {
                    throw new IllegalArgumentException("inValue out of range");
                }
            }
        }

        if(inType.isIntegral() && outType.isFloatingPoint() && normalized) {
            // When converting integer to floating point, normalize the value if
            // necessary.
            return outType.div(inType, inValue, inType, inType.max());
        }
        else if(inType.isFloatingPoint() && outType.isIntegral() && normalized) {
            // Converting from floating point to a normalized integer.
            if(inType.gt(inValue, 1) || inType.lt(inValue, 0)) {
                // Normalized float values need to be between 0 and 1.
                throw new IllegalArgumentException("input value outside normalized range: " + inValue);
            }
            // From Google's repository: "Consider allowing float to normalized integer conversion
            // for 64-bit integer types. It doesn't work currently because we don't
            // have a floating point type that could store all 64-bit integers."
            if(outType.size() > 4) {
                throw new IllegalArgumentException("output type size bigger than 4");
            }
            // Expand the float to the range of the output integer and round it to the
            // nearest representable value. Use doubles for the math to ensure the
            // integer values are represented properly during the conversion process.
            DataNumberType<Double, ?> d = DataType.float64();
            return outType.floor(d, d.add(d.mul(inType, inValue, outType, outType.max()), 0.5));
        }
        else {
            return outType.from(inType, inValue);
        }
        // From Google's repository: "Add handling of normalized attributes when converting
        // between different integer representations. If the attribute is
        // normalized, integer values should be converted as if they represent 0-1
        // range. E.g. when we convert UINT16 to UINT8, the range <0, 65535>
        // should be converted to range <0, 255>"
    }
}
