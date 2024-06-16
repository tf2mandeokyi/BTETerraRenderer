package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.draco.core.*;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

/**
 * Attribute transform for quantized attributes.
 */
@Getter
public class AttributeQuantizationTransform extends AttributeTransform {

    private int quantizationBits = -1;
    private final CppVector<Float> minValues = new CppVector<>();
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
        quantizationBits = transformData.getParameterValue(byteOffset, DataType.INT32);
        byteOffset += 4;
        minValues.resize(attribute.getNumComponents(), 0f);
        for(int i = 0; i < attribute.getNumComponents(); i++) {
            minValues.set(i, transformData.getParameterValue(byteOffset, DataType.FLOAT32));
            byteOffset += 4;
        }
        range = transformData.getParameterValue(byteOffset, DataType.FLOAT32);
        return Status.OK;
    }

    @Override
    public void copyToAttributeTransformData(AttributeTransformData outData) {
        outData.setTransformType(AttributeTransformType.ATTRIBUTE_QUANTIZATION_TRANSFORM);
        outData.appendParameterValue(DataType.INT32, quantizationBits);
        for(int i = 0; i < minValues.size(); i++) {
            outData.appendParameterValue(DataType.FLOAT32, minValues.get(i));
        }
        outData.appendParameterValue(DataType.FLOAT32, range);
    }

    @Override
    public Status transformAttribute(PointAttribute attribute, List<PointIndex> pointIds, PointAttribute targetAttribute) {
        if(!isInitialized()) {
            return new Status(Status.Code.INVALID_PARAMETER, "AttributeQuantizationTransform is not initialized");
        }
        int numComponents = attribute.getNumComponents();

        // Quantize all values using the order given by point_ids.
        int maxQuantizedValue = (1 << quantizationBits) - 1;
        Quantizer quantizer = new Quantizer();
        quantizer.init(range, maxQuantizedValue);
        AtomicInteger dstIndex = new AtomicInteger();
        float[] attVal = new float[numComponents];
        (pointIds.isEmpty() ? IntStream.range(0, attribute.size()).mapToObj(PointIndex::of) : pointIds.stream())
                .map(attribute::getMappedIndex)
                .forEach(attValId -> {
                    attribute.getValue(attValId, DataType.FLOAT32, attVal, numComponents);
                    for(int c = 0; c < numComponents; c++) {
                        float value = attVal[c] - minValues.get(c);
                        int qVal = quantizer.quantizeFloat(value);
                        targetAttribute.setAttributeValue(AttributeValueIndex.of(0), DataType.INT32,
                                dstIndex.getAndIncrement(), qVal);
                    }
                });
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
        attribute.getValue(AttributeValueIndex.of(0), DataType.FLOAT32, attVal, numComponents);
        attribute.getValue(AttributeValueIndex.of(0), DataType.FLOAT32, minValues::set, numComponents);
        attribute.getValue(AttributeValueIndex.of(0), DataType.FLOAT32, maxValues, numComponents);
        for(int i = 1; i < attribute.size(); i++) {
            attribute.getValue(AttributeValueIndex.of(i), DataType.FLOAT32, attVal, numComponents);
            for(int c = 0; c < numComponents; c++) {
                if(Float.isNaN(attVal[c])) {
                    return new Status(Status.Code.INVALID_PARAMETER, "Attribute value is NaN");
                }
                if(minValues.get(c) > attVal[c]) {
                    ((List<Float>) minValues).set(c, attVal[c]);
                }
                if(maxValues[c] < attVal[c]) {
                    maxValues[c] = attVal[c];
                }
            }
        }
        for(int c = 0; c < numComponents; c++) {
            if(Float.isNaN(minValues.get(c)) || Float.isInfinite(minValues.get(c)) ||
                    Float.isNaN(maxValues[c]) || Float.isInfinite(maxValues[c])) {
                return new Status(Status.Code.INVALID_PARAMETER, "Attribute value is NaN or infinite");
            }
            float dif = maxValues[c] - minValues.get(c);
            if(dif > range) {
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
            encoderBuffer.encode(DataType.FLOAT32, minValues::get, minValues.size());
            encoderBuffer.encode(DataType.FLOAT32, range);
            encoderBuffer.encode(DataType.UINT8, (short) quantizationBits);
            return Status.OK;
        }
        return new Status(Status.Code.INVALID_PARAMETER, "AttributeQuantizationTransform is not initialized");
    }

    @Override
    public Status decodeParameters(PointAttribute attribute, DecoderBuffer decoderBuffer) {
        StatusChain chain = Status.newChain();

        minValues.resize(attribute.getNumComponents());
        if(decoderBuffer.decode(DataType.FLOAT32, minValues::set, minValues.size()).isError(chain)) return chain.get();

        if(decoderBuffer.decode(DataType.FLOAT32, val -> range = val).isError(chain)) return chain.get();

        AtomicReference<Short> quantizationBitsRef = new AtomicReference<>((short) 0);
        if(decoderBuffer.decode(DataType.UINT8, quantizationBitsRef::set).isError(chain)) return chain.get();

        short quantizationBits = quantizationBitsRef.get();
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
    protected DataType<?> getTransformedDataType(PointAttribute attribute) {
        return DataType.UINT32;
    }

    @Override
    protected int getTransformedNumComponents(PointAttribute attribute) {
        return attribute.getNumComponents();
    }
}
