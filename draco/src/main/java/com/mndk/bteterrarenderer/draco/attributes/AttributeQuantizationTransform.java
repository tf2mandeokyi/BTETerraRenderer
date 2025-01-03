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
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.core.*;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import lombok.Getter;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Attribute transform for quantized attributes.
 */
@Getter
public class AttributeQuantizationTransform extends AttributeTransform {

    private int quantizationBits = -1;
    private final CppVector<Float> minValues = new CppVector<>(DataType.float32());
    private float range = 0;

    public AttributeQuantizationTransform() {
        super();
    }

    @Override
    public AttributeTransformType getType() {
        return AttributeTransformType.QUANTIZATION;
    }

    @Override
    public Status initFromAttribute(PointAttribute attribute) {
        AttributeTransformData transformData = attribute.getAttributeTransformData();
        if (transformData == null || transformData.getTransformType() != AttributeTransformType.QUANTIZATION) {
            return Status.invalidParameter("Wrong transform type");
        }
        int byteOffset = 0;
        quantizationBits = transformData.getParameterValue(DataType.int32(), byteOffset);
        byteOffset += 4;
        minValues.resize(attribute.getNumComponents().intValue(), 0f);
        for (int i = 0, until = attribute.getNumComponents().intValue(); i < until; i++) {
            minValues.set(i, transformData.getParameterValue(DataType.float32(), byteOffset));
            byteOffset += 4;
        }
        range = transformData.getParameterValue(DataType.float32(), byteOffset);
        return Status.ok();
    }

    @Override
    public void copyToAttributeTransformData(AttributeTransformData outData) {
        outData.setTransformType(AttributeTransformType.QUANTIZATION);
        outData.appendParameterValue(DataType.int32(), quantizationBits);
        for (int i = 0; i < minValues.size(); i++) {
            outData.appendParameterValue(DataType.float32(), minValues.get(i));
        }
        outData.appendParameterValue(DataType.float32(), range);
    }

    @Override
    public Status transformAttribute(PointAttribute attribute, CppVector<PointIndex> pointIds, PointAttribute targetAttribute) {
        if (!isInitialized()) {
            return Status.invalidParameter("AttributeQuantizationTransform is not initialized");
        }
        int numComponents = attribute.getNumComponents().intValue();

        // Quantize all values using the order given by point_ids.
        Pointer<Integer> portableAttributeData = targetAttribute.getAddress(AttributeValueIndex.of(0)).toInt();
        int maxQuantizedValue = (1 << quantizationBits) - 1;
        Quantizer quantizer = new Quantizer();
        quantizer.init(range, maxQuantizedValue);
        int dstIndex = 0;
        float[] attVal = new float[numComponents];

        Stream<PointIndex> pointStream = pointIds.isEmpty() ?
                IntStream.range(0, attribute.size()).mapToObj(PointIndex::of) :
                pointIds.stream();
        Stream<AttributeValueIndex> attributeStream = pointStream.map(attribute::getMappedIndex);
        for (AttributeValueIndex attValId : (Iterable<AttributeValueIndex>) attributeStream::iterator) {
            attribute.getValue(attValId, Pointer.wrap(attVal), numComponents);
            for (int c = 0; c < numComponents; c++) {
                float value = attVal[c] - minValues.get(c);
                int qVal = quantizer.quantizeFloat(value);
                portableAttributeData.set(dstIndex++, qVal);
            }
        }
        return Status.ok();
    }

    @Override
    public Status inverseTransformAttribute(PointAttribute attribute, PointAttribute targetAttribute) {
        StatusChain chain = new StatusChain();

        if (targetAttribute.getDataType() != DracoDataType.FLOAT32) {
            return Status.invalidParameter("Target attribute data type is not FLOAT32");
        }

        // Convert all quantized values back to floats.
        int maxQuantizedValue = (1 << quantizationBits) - 1;
        int numComponents = targetAttribute.getNumComponents().intValue();
        Pointer<Float> attVal = Pointer.newFloatArray(numComponents);
        int quantValId = 0;
        int outBytePos = 0;
        Dequantizer dequantizer = new Dequantizer();
        if (dequantizer.init(range, maxQuantizedValue).isError(chain)) return chain.get();
        Pointer<Integer> sourceAttributeData = attribute.getAddress(AttributeValueIndex.of(0)).toInt();

        int numValues = targetAttribute.size();

        for (int i = 0; i < numValues; i++) {
            for (int c = 0; c < numComponents; c++) {
                float value = dequantizer.dequantizeFloat(sourceAttributeData.get(quantValId++));
                value += minValues.get(c);
                attVal.set(c, value);
            }
            targetAttribute.getBuffer().write(outBytePos, attVal, numComponents);
            outBytePos += numComponents * 4;
        }
        return Status.ok();
    }

    public Status setParameters(int quantizationBits, Pointer<Float> minValues, int numComponents, float range) {
        if (!isQuantizationValid(quantizationBits)) {
            return Status.invalidParameter("Invalid quantization bits: " + quantizationBits);
        }
        this.quantizationBits = quantizationBits;
        this.minValues.assign(minValues, numComponents);
        this.range = range;
        return Status.ok();
    }

    public Status computeParameters(PointAttribute attribute, int quantizationBits) {
        if (this.quantizationBits != -1) {
            return Status.invalidParameter("AttributeQuantizationTransform is already initialized");
        }
        if (!isQuantizationValid(quantizationBits)) {
            return Status.invalidParameter("Invalid quantization bits: " + quantizationBits);
        }
        this.quantizationBits = quantizationBits;

        int numComponents = attribute.getNumComponents().intValue();
        range = 0;
        minValues.assign(numComponents, 0f);
        float[] maxValues = new float[numComponents];
        float[] attVal = new float[numComponents];
        attribute.getValue(AttributeValueIndex.of(0), Pointer.wrap(attVal), numComponents);
        attribute.getValue(AttributeValueIndex.of(0), minValues.getPointer(), numComponents);
        attribute.getValue(AttributeValueIndex.of(0), Pointer.wrap(maxValues), numComponents);
        for (int i = 1; i < attribute.size(); i++) {
            attribute.getValue(AttributeValueIndex.of(i), Pointer.wrap(attVal), numComponents);
            for (int c = 0; c < numComponents; c++) {
                if (Float.isNaN(attVal[c])) {
                    return Status.invalidParameter("Attribute value is NaN");
                }
                if (minValues.get(c) > attVal[c]) {
                    minValues.set(c, attVal[c]);
                }
                if (maxValues[c] < attVal[c]) {
                    maxValues[c] = attVal[c];
                }
            }
        }
        for (int c = 0; c < numComponents; c++) {
            if (minValues.get(c).isNaN() || minValues.get(c).isInfinite() ||
                    Float.isNaN(maxValues[c]) || Float.isInfinite(maxValues[c])) {
                return Status.invalidParameter("Attribute value is NaN or infinite");
            }
            float dif = maxValues[c] - minValues.get(c);
            if (range < dif) {
                range = dif;
            }
        }
        if (range == 0) {
            range = 1;
        }
        return Status.ok();
    }

    @Override
    public Status encodeParameters(EncoderBuffer encoderBuffer) {
        if (isInitialized()) {
            encoderBuffer.encode(minValues.getPointer(), minValues.size());
            encoderBuffer.encode(DataType.float32(), range);
            encoderBuffer.encode(UByte.of(quantizationBits));
            return Status.ok();
        }
        return Status.invalidParameter("AttributeQuantizationTransform is not initialized");
    }

    @Override
    public Status decodeParameters(PointAttribute attribute, DecoderBuffer decoderBuffer) {
        StatusChain chain = new StatusChain();

        minValues.resize(attribute.getNumComponents().intValue());
        if (decoderBuffer.decode(minValues.getPointer(), minValues.size()).isError(chain)) return chain.get();

        Pointer<Float> rangeRef = Pointer.newFloat();
        if (decoderBuffer.decode(rangeRef).isError(chain)) return chain.get();
        this.range = rangeRef.get();

        Pointer<UByte> quantizationBitsRef = Pointer.newUByte();
        if (decoderBuffer.decode(quantizationBitsRef).isError(chain)) return chain.get();

        int quantizationBits = quantizationBitsRef.get().intValue();
        if (!isQuantizationValid(quantizationBits)) {
            return Status.invalidParameter("Invalid quantization bits: " + quantizationBits);
        }
        this.quantizationBits = quantizationBits;
        return Status.ok();
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
    protected DracoDataType getTransformedDataType(PointAttribute attribute) {
        return DracoDataType.UINT32;
    }

    @Override
    protected int getTransformedNumComponents(PointAttribute attribute) {
        return attribute.getNumComponents().intValue();
    }
}
