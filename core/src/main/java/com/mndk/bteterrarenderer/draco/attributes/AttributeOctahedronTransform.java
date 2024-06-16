package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.draco.compression.attributes.OctahedronToolBox;
import com.mndk.bteterrarenderer.draco.core.*;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Getter
public class AttributeOctahedronTransform extends AttributeTransform {

    private int quantizationBits = -1;

    @Override
    public AttributeTransformType getType() {
        return AttributeTransformType.ATTRIBUTE_OCTAHEDRON_TRANSFORM;
    }

    @Override
    public Status initFromAttribute(PointAttribute attribute) {
        AttributeTransformData transformData = attribute.getAttributeTransformData();
        if(transformData == null || transformData.getTransformType() != AttributeTransformType.ATTRIBUTE_OCTAHEDRON_TRANSFORM) {
            return new Status(Status.Code.INVALID_PARAMETER, "Wrong transform type");
        }
        quantizationBits = transformData.getParameterValue(0, DataType.INT32);
        return Status.OK;
    }

    @Override
    public void copyToAttributeTransformData(AttributeTransformData outData) {
        outData.setTransformType(AttributeTransformType.ATTRIBUTE_OCTAHEDRON_TRANSFORM);
        outData.appendParameterValue(DataType.INT32, quantizationBits);
    }

    @Override
    public Status transformAttribute(PointAttribute attribute, List<PointIndex> pointIds, PointAttribute targetAttribute) {
        return generatePortableAttribute(attribute, pointIds, targetAttribute.size(), targetAttribute);
    }

    @Override
    public Status inverseTransformAttribute(PointAttribute attribute, PointAttribute targetAttribute) {
        StatusChain chain = Status.newChain();

        if(targetAttribute.getDataType() != DataType.FLOAT32) {
            return new Status(Status.Code.INVALID_PARAMETER, "Target attribute must have FLOAT32 data type");
        }

        int numPoints = targetAttribute.size();
        int numComponents = targetAttribute.getNumComponents();
        if(numComponents != 3) {
            return new Status(Status.Code.INVALID_PARAMETER, "Attribute must have 3 components");
        }
        float[] attVal = new float[3];
        int[] sourceAttributeData = new int[numPoints * 2];
        attribute.getValue(AttributeValueIndex.of(0), DataType.INT32, sourceAttributeData);
        OctahedronToolBox octahedronToolBox = new OctahedronToolBox();
        if(octahedronToolBox.setQuantizationBits(quantizationBits).isError(chain)) return chain.get();

        for(int i = 0; i < numPoints; i++) {
            int s = sourceAttributeData[i * 2];
            int t = sourceAttributeData[i * 2 + 1];
            octahedronToolBox.quantizedOctahedralCoordsToUnitVector(s, t, attVal);
            targetAttribute.setAttributeValue(AttributeValueIndex.of(0), DataType.FLOAT32, i * 3, attVal[0]);
            targetAttribute.setAttributeValue(AttributeValueIndex.of(0), DataType.FLOAT32, i * 3 + 1, attVal[1]);
            targetAttribute.setAttributeValue(AttributeValueIndex.of(0), DataType.FLOAT32, i * 3 + 2, attVal[2]);
        }
        return Status.OK;
    }

    public void setParameters(int quantizationBits) {
        this.quantizationBits = quantizationBits;
    }

    @Override
    public Status encodeParameters(EncoderBuffer encoderBuffer) {
        if(!isInitialized()) {
            return new Status(Status.Code.INVALID_PARAMETER, "Octahedron transform not initialized");
        }
        encoderBuffer.encode(DataType.UINT8, (short) quantizationBits);
        return Status.OK;
    }

    @Override
    public Status decodeParameters(PointAttribute attribute, DecoderBuffer decoderBuffer) {
        return decoderBuffer.decode(DataType.UINT8, val -> quantizationBits = val);
    }

    public boolean isInitialized() {
        return quantizationBits != -1;
    }

    @Override
    protected DataType<?> getTransformedDataType(PointAttribute attribute) {
        return DataType.UINT32;
    }

    @Override
    protected int getTransformedNumComponents(PointAttribute attribute) {
        return 2;
    }

    protected Status generatePortableAttribute(PointAttribute attribute, List<PointIndex> pointIds,
                                                int numPoints, PointAttribute targetAttribute) {
        StatusChain chain = Status.newChain();

        if(!isInitialized()) {
            return new Status(Status.Code.INVALID_PARAMETER, "Octahedron transform not initialized");
        }

        int[] portableAttributeData = new int[numPoints * 2];
        float[] attVal = new float[3];
        AtomicInteger dstIndex = new AtomicInteger();
        OctahedronToolBox converter = new OctahedronToolBox();
        if(converter.setQuantizationBits(quantizationBits).isError(chain)) return chain.get();

        (pointIds.isEmpty() ? IntStream.range(0, numPoints).mapToObj(PointIndex::of) : pointIds.stream())
                .map(attribute::getMappedIndex)
                .forEach(attValId -> {
                    attribute.getValue(attValId, DataType.FLOAT32, attVal);
                    AtomicInteger s = new AtomicInteger();
                    AtomicInteger t = new AtomicInteger();
                    converter.floatVectorToQuantizedOctahedralCoords(index -> attVal[index], s, t);
                    portableAttributeData[dstIndex.getAndIncrement()] = s.get();
                    portableAttributeData[dstIndex.getAndIncrement()] = t.get();
                });

        targetAttribute.setAttributeValue(AttributeValueIndex.of(0), DataType.INT32, portableAttributeData);
        return Status.OK;
    }
}
