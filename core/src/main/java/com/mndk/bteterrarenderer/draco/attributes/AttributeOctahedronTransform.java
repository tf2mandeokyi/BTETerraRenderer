package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.*;
import com.mndk.bteterrarenderer.draco.compression.attributes.OctahedronToolBox;
import com.mndk.bteterrarenderer.draco.core.*;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
            return Status.invalidParameter("Wrong transform type");
        }
        quantizationBits = transformData.getParameterValue(DataType.int32(), 0);
        return Status.ok();
    }

    @Override
    public void copyToAttributeTransformData(AttributeTransformData outData) {
        outData.setTransformType(AttributeTransformType.ATTRIBUTE_OCTAHEDRON_TRANSFORM);
        outData.appendParameterValue(DataType.int32(), quantizationBits);
    }

    @Override
    public Status transformAttribute(PointAttribute attribute, List<PointIndex> pointIds, PointAttribute targetAttribute) {
        return generatePortableAttribute(attribute, pointIds, targetAttribute.size(), targetAttribute);
    }

    @Override
    public Status inverseTransformAttribute(PointAttribute attribute, PointAttribute targetAttribute) {
        StatusChain chain = new StatusChain();

        if(targetAttribute.getDataType() != DracoDataType.DT_FLOAT32) {
            return Status.invalidParameter("Target attribute must have FLOAT32 data type");
        }

        int numPoints = targetAttribute.size();
        int numComponents = targetAttribute.getNumComponents().intValue();
        if(numComponents != 3) {
            return Status.invalidParameter("Attribute must have 3 components");
        }
        float[] attVal = new float[3];
        int[] sourceAttributeData = new int[numPoints * 2];
        attribute.getValue(AttributeValueIndex.of(0), DataType.int32(), sourceAttributeData);
        OctahedronToolBox octahedronToolBox = new OctahedronToolBox();
        if(octahedronToolBox.setQuantizationBits(quantizationBits).isError(chain)) return chain.get();

        DataNumberType<Float, ?> f = DataType.float32();
        for(int i = 0; i < numPoints; i++) {
            int s = sourceAttributeData[i * 2];
            int t = sourceAttributeData[i * 2 + 1];
            octahedronToolBox.quantizedOctahedralCoordsToUnitVector(s, t, attVal);
            targetAttribute.setAttributeValue(AttributeValueIndex.of(0), f, i * 3L,     attVal[0]);
            targetAttribute.setAttributeValue(AttributeValueIndex.of(0), f, i * 3L + 1, attVal[1]);
            targetAttribute.setAttributeValue(AttributeValueIndex.of(0), f, i * 3L + 2, attVal[2]);
        }
        return Status.ok();
    }

    public void setParameters(int quantizationBits) {
        this.quantizationBits = quantizationBits;
    }

    @Override
    public Status encodeParameters(EncoderBuffer encoderBuffer) {
        if(!isInitialized()) {
            return Status.invalidParameter("Octahedron transform not initialized");
        }
        encoderBuffer.encode(DataType.uint8(), UByte.of(quantizationBits));
        return Status.ok();
    }

    @Override
    public Status decodeParameters(PointAttribute attribute, DecoderBuffer decoderBuffer) {
        return decoderBuffer.decode(DataType.uint8(), val -> quantizationBits = val.intValue());
    }

    public boolean isInitialized() {
        return quantizationBits != -1;
    }

    @Override
    protected DracoDataType getTransformedDataType(PointAttribute attribute) {
        return DracoDataType.DT_UINT32;
    }

    @Override
    protected int getTransformedNumComponents(PointAttribute attribute) {
        return 2;
    }

    protected Status generatePortableAttribute(PointAttribute attribute, List<PointIndex> pointIds,
                                                int numPoints, PointAttribute targetAttribute) {
        StatusChain chain = new StatusChain();

        if(!isInitialized()) {
            return Status.invalidParameter("Octahedron transform not initialized");
        }

        int[] portableAttributeData = new int[2 * (pointIds.isEmpty() ? numPoints : pointIds.size())];
        float[] attVal = new float[3];
        int dstIndex = 0;
        OctahedronToolBox converter = new OctahedronToolBox();
        if(converter.setQuantizationBits(quantizationBits).isError(chain)) return chain.get();

        Stream<PointIndex> pointStream = pointIds.isEmpty() ?
                IntStream.range(0, numPoints).mapToObj(PointIndex::of) :
                pointIds.stream();
        Stream<AttributeValueIndex> attributeStream = pointStream.map(attribute::getMappedIndex);
        for(AttributeValueIndex attValId : (Iterable<AttributeValueIndex>) attributeStream::iterator) {
            attribute.getValue(attValId, DataType.float32(), attVal);
            AtomicInteger s = new AtomicInteger();
            AtomicInteger t = new AtomicInteger();
            converter.floatVectorToQuantizedOctahedralCoords(DataType.float32(), attVal, s, t);
            portableAttributeData[dstIndex++] = s.get();
            portableAttributeData[dstIndex++] = t.get();
        }

        targetAttribute.setAttributeValues(AttributeValueIndex.of(0), DataType.int32(), portableAttributeData);
        return Status.ok();
    }
}
