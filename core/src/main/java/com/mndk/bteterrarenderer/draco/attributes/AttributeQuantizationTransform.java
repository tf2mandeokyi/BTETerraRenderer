package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.draco.core.*;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Attribute transform for quantized attributes.
 */
@Getter
public class AttributeQuantizationTransform extends AttributeTransform {

    private int quantizationBits = -1;
    private final CppVector<Float> minValues = CppVector.create(DataType.float32());
    private float range = 0;

    public AttributeQuantizationTransform() {
        super();
    }

    @Override
    public AttributeTransformType getType() {
        return AttributeTransformType.ATTRIBUTE_QUANTIZATION_TRANSFORM;
    }

    @Override
    public Status initFromAttribute(PointAttribute attribute) {
        AttributeTransformData transformData = attribute.getAttributeTransformData();
        if(transformData == null || transformData.getTransformType() != AttributeTransformType.ATTRIBUTE_QUANTIZATION_TRANSFORM) {
            return new Status(Status.Code.INVALID_PARAMETER, "Wrong transform type");
        }
        int byteOffset = 0;
        quantizationBits = transformData.getParameterValue(DataType.int32(), byteOffset);
        byteOffset += 4;
        minValues.resize(attribute.getNumComponents(), 0f);
        for(int i = 0; i < attribute.getNumComponents(); i++) {
            minValues.set(i, transformData.getParameterValue(DataType.float32(), byteOffset));
            byteOffset += 4;
        }
        range = transformData.getParameterValue(DataType.float32(), byteOffset);
        return Status.OK;
    }

    @Override
    public void copyToAttributeTransformData(AttributeTransformData outData) {
        outData.setTransformType(AttributeTransformType.ATTRIBUTE_QUANTIZATION_TRANSFORM);
        outData.appendParameterValue(DataType.int32(), quantizationBits);
        for(int i = 0; i < minValues.size(); i++) {
            outData.appendParameterValue(DataType.float32(), minValues.get(i));
        }
        outData.appendParameterValue(DataType.float32(), range);
    }

    @Override
    public Status transformAttribute(PointAttribute attribute, List<PointIndex> pointIds, PointAttribute targetAttribute) {
        if(!isInitialized()) {
            return new Status(Status.Code.INVALID_PARAMETER, "AttributeQuantizationTransform is not initialized");
        }
        int numComponents = attribute.getNumComponents();

        // Quantize all values using the order given by point_ids.
        int pointCount = pointIds.isEmpty() ? attribute.size() : pointIds.size();
        int[] portableAttributeData = new int[numComponents * pointCount];
        int maxQuantizedValue = (1 << quantizationBits) - 1;
        Quantizer quantizer = new Quantizer();
        quantizer.init(range, maxQuantizedValue);
        int dstIndex = 0;
        float[] attVal = new float[numComponents];

        Stream<PointIndex> pointStream = pointIds.isEmpty() ?
                IntStream.range(0, attribute.size()).mapToObj(PointIndex::of) :
                pointIds.stream();
        Stream<AttributeValueIndex> attributeStream = pointStream.map(attribute::getMappedIndex);
        for(AttributeValueIndex attValId : (Iterable<AttributeValueIndex>) attributeStream::iterator) {
            attribute.getValue(attValId, DataType.float32(), attVal, numComponents);
            for(int c = 0; c < numComponents; c++) {
                float value = attVal[c] - minValues.get(c);
                int qVal = quantizer.quantizeFloat(value);
                portableAttributeData[dstIndex++] = qVal;
            }
        }

        targetAttribute.setAttributeValues(AttributeValueIndex.of(0), DataType.int32(), portableAttributeData);
        return Status.OK;
    }

    @Override
    public Status inverseTransformAttribute(PointAttribute attribute, PointAttribute targetAttribute) {
        return new Status(Status.Code.UNSUPPORTED_FEATURE, "Inverse transform is not supported");
    }

    public Status setParameters(int quantizationBits, float[] minValues, int numComponents, float range) {
        if(!isQuantizationValid(quantizationBits)) {
            return new Status(Status.Code.INVALID_PARAMETER, "Invalid quantization bits: " + quantizationBits);
        }
        this.quantizationBits = quantizationBits;
        this.minValues.assign(i -> minValues[i], 0, numComponents);
        this.range = range;
        return Status.OK;
    }

    public Status computeParameters(PointAttribute attribute, int quantizationBits) {
        if(this.quantizationBits != -1) {
            return new Status(Status.Code.INVALID_PARAMETER, "AttributeQuantizationTransform is already initialized");
        }
        if(!isQuantizationValid(quantizationBits)) {
            return new Status(Status.Code.INVALID_PARAMETER, "Invalid quantization bits: " + quantizationBits);
        }
        this.quantizationBits = quantizationBits;

        int numComponents = attribute.getNumComponents();
        range = 0;
        minValues.assign(numComponents, 0f);
        float[] maxValues = new float[numComponents];
        float[] attVal = new float[numComponents];
        attribute.getValue(AttributeValueIndex.of(0), DataType.float32(), attVal, numComponents);
        attribute.getValue(AttributeValueIndex.of(0), DataType.float32(), minValues.setter(), numComponents);
        attribute.getValue(AttributeValueIndex.of(0), DataType.float32(), maxValues, numComponents);
        for(int i = 1; i < attribute.size(); i++) {
            attribute.getValue(AttributeValueIndex.of(i), DataType.float32(), attVal, numComponents);
            for(int c = 0; c < numComponents; c++) {
                if(Float.isNaN(attVal[c])) {
                    return new Status(Status.Code.INVALID_PARAMETER, "Attribute value is NaN");
                }
                if(minValues.get(c) > attVal[c]) {
                    minValues.set(c, attVal[c]);
                }
                if(maxValues[c] < attVal[c]) {
                    maxValues[c] = attVal[c];
                }
            }
        }
        for(int c = 0; c < numComponents; c++) {
            if(minValues.get(c).isNaN() || minValues.get(c).isInfinite() ||
                    Float.isNaN(maxValues[c]) || Float.isInfinite(maxValues[c])) {
                return new Status(Status.Code.INVALID_PARAMETER, "Attribute value is NaN or infinite");
            }
            float dif = maxValues[c] - minValues.get(c);
            if(range < dif) {
                range = dif;
            }
        }
        if(range == 0) {
            range = 1;
        }
        return Status.OK;
    }

    @Override
    public Status encodeParameters(EncoderBuffer encoderBuffer) {
        if(isInitialized()) {
            encoderBuffer.encode(DataType.float32(), minValues.getter(), minValues.size());
            encoderBuffer.encode(DataType.float32(), range);
            encoderBuffer.encode(DataType.uint8(), UByte.of(quantizationBits));
            return Status.OK;
        }
        return new Status(Status.Code.INVALID_PARAMETER, "AttributeQuantizationTransform is not initialized");
    }

    @Override
    public Status decodeParameters(PointAttribute attribute, DecoderBuffer decoderBuffer) {
        StatusChain chain = Status.newChain();

        minValues.resize(attribute.getNumComponents());
        if(decoderBuffer.decode(DataType.float32(), minValues.setter(), minValues.size()).isError(chain)) return chain.get();

        if(decoderBuffer.decode(DataType.float32(), val -> this.range = val).isError(chain)) return chain.get();

        AtomicReference<UByte> quantizationBitsRef = new AtomicReference<>();
        if(decoderBuffer.decode(DataType.uint8(), quantizationBitsRef::set).isError(chain)) return chain.get();

        int quantizationBits = quantizationBitsRef.get().intValue();
        if(!isQuantizationValid(quantizationBits)) {
            return new Status(Status.Code.INVALID_PARAMETER, "Invalid quantization bits: " + quantizationBits);
        }
        this.quantizationBits = quantizationBits;
        return Status.OK;
    }

    public float getMinValue(int axis) {
        return minValues.get(axis);
    }
    public boolean isInitialized() {
        return quantizationBits != -1;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected static boolean isQuantizationValid(int quantizationBits) {
        return quantizationBits >= 1 && quantizationBits <= 30;
    }

    @Override
    protected DataNumberType<?, ?> getTransformedDataType(PointAttribute attribute) {
        return DataType.int32();
    }

    @Override
    protected int getTransformedNumComponents(PointAttribute attribute) {
        return attribute.getNumComponents();
    }
}
