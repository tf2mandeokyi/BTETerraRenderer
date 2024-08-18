package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.PointerHelper;
import com.mndk.bteterrarenderer.draco.compression.attributes.OctahedronToolBox;
import com.mndk.bteterrarenderer.draco.core.*;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import lombok.Getter;

import java.util.stream.IntStream;
import java.util.stream.Stream;

@Getter
public class AttributeOctahedronTransform extends AttributeTransform {

    private int quantizationBits = -1;

    @Override
    public AttributeTransformType getType() {
        return AttributeTransformType.OCTAHEDRON;
    }

    @Override
    public Status initFromAttribute(PointAttribute attribute) {
        AttributeTransformData transformData = attribute.getAttributeTransformData();
        if(transformData == null || transformData.getTransformType() != AttributeTransformType.OCTAHEDRON) {
            return Status.invalidParameter("Wrong transform type");
        }
        quantizationBits = transformData.getParameterValue(DataType.int32(), 0);
        return Status.ok();
    }

    @Override
    public void copyToAttributeTransformData(AttributeTransformData outData) {
        outData.setTransformType(AttributeTransformType.OCTAHEDRON);
        outData.appendParameterValue(DataType.int32(), quantizationBits);
    }

    @Override
    public Status transformAttribute(PointAttribute attribute, CppVector<PointIndex> pointIds, PointAttribute targetAttribute) {
        return generatePortableAttribute(attribute, pointIds, targetAttribute.size(), targetAttribute);
    }

    @Override
    public Status inverseTransformAttribute(PointAttribute attribute, PointAttribute targetAttribute) {
        StatusChain chain = new StatusChain();

        if(targetAttribute.getDataType() != DracoDataType.FLOAT32) {
            return Status.invalidParameter("Target attribute must have FLOAT32 data type");
        }

        int numPoints = targetAttribute.size();
        int numComponents = targetAttribute.getNumComponents().intValue();
        if(numComponents != 3) {
            return Status.invalidParameter("Attribute must have 3 components");
        }
        Pointer<Float> attVal = Pointer.newFloatArray(3);
        Pointer<Integer> sourceAttributeData = attribute.getAddress(AttributeValueIndex.of(0)).toInt();
        Pointer<Float> targetAddress = targetAttribute.getAddress(AttributeValueIndex.of(0)).toFloat();
        OctahedronToolBox octahedronToolBox = new OctahedronToolBox();
        if(octahedronToolBox.setQuantizationBits(quantizationBits).isError(chain)) return chain.get();

        for(int i = 0; i < numPoints; i++) {
            int s = sourceAttributeData.get(i * 2L);
            int t = sourceAttributeData.get(i * 2L + 1);
            octahedronToolBox.quantizedOctahedralCoordsToUnitVector(s, t, attVal);
            PointerHelper.copyMultiple(attVal, targetAddress.add(3L * i), 3);
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
        encoderBuffer.encode(UByte.of(quantizationBits));
        return Status.ok();
    }

    @Override
    public Status decodeParameters(PointAttribute attribute, DecoderBuffer decoderBuffer) {
        Pointer<UByte> quantizationBitsRef = Pointer.newUByte();
        Status status = decoderBuffer.decode(quantizationBitsRef);
        if(status.isError()) return status;
        quantizationBits = quantizationBitsRef.get().intValue();
        return Status.ok();
    }

    public boolean isInitialized() {
        return quantizationBits != -1;
    }

    @Override
    protected DracoDataType getTransformedDataType(PointAttribute attribute) {
        return DracoDataType.UINT32;
    }

    @Override
    protected int getTransformedNumComponents(PointAttribute attribute) {
        return 2;
    }

    protected Status generatePortableAttribute(PointAttribute attribute, CppVector<PointIndex> pointIds,
                                                int numPoints, PointAttribute targetAttribute) {
        StatusChain chain = new StatusChain();

        if(!isInitialized()) {
            return Status.invalidParameter("Octahedron transform not initialized");
        }

        Pointer<Integer> portableAttributeData = targetAttribute.getAddress(AttributeValueIndex.of(0)).toInt();
        Pointer<Float> attVal = Pointer.newFloatArray(3);
        int dstIndex = 0;
        OctahedronToolBox converter = new OctahedronToolBox();
        if(converter.setQuantizationBits(quantizationBits).isError(chain)) return chain.get();

        Stream<PointIndex> pointStream = pointIds.isEmpty() ?
                IntStream.range(0, numPoints).mapToObj(PointIndex::of) :
                pointIds.stream();
        Stream<AttributeValueIndex> attributeStream = pointStream.map(attribute::getMappedIndex);
        for(AttributeValueIndex attValId : (Iterable<AttributeValueIndex>) attributeStream::iterator) {
            attribute.getValue(attValId, attVal);
            Pointer<Integer> s = Pointer.newInt();
            Pointer<Integer> t = Pointer.newInt();
            converter.floatVectorToQuantizedOctahedralCoords(attVal, s, t);
            portableAttributeData.set(dstIndex++, s.get());
            portableAttributeData.set(dstIndex++, t.get());
        }
        return Status.ok();
    }
}
