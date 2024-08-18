package com.mndk.bteterrarenderer.draco.compression.attributes;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.AttributeValueIndex;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.attributes.scheme.PSchemeDecoderFactory;
import com.mndk.bteterrarenderer.draco.compression.attributes.scheme.PSchemeTypedDecoderInterface;
import com.mndk.bteterrarenderer.draco.compression.attributes.scheme.PSchemeWrapDecodingTransform;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeTransformType;
import com.mndk.bteterrarenderer.draco.compression.entropy.SymbolDecoding;
import com.mndk.bteterrarenderer.draco.compression.pointcloud.PointCloudDecoder;
import com.mndk.bteterrarenderer.draco.core.*;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;

public class SequentialIntegerAttributeDecoder extends SequentialAttributeDecoder {

    private PSchemeTypedDecoderInterface<Integer, Integer> predictionScheme;

    @Override
    public Status init(PointCloudDecoder decoder, int attributeId) {
        return super.init(decoder, attributeId);
    }

    @Override
    public Status transformAttributeToOriginalFormat(CppVector<PointIndex> pointIds) {
        PointCloudDecoder decoder = this.getDecoder();
        if(decoder != null && this.getDecoder().getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            return Status.ok();  // Don't revert the transform here for older files.
        }
        return this.storeValues(UInt.of(pointIds.size()));
    }

    @Override
    protected Status decodeValues(CppVector<PointIndex> pointIds, DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();

        // Decode prediction scheme.
        Pointer<Byte> methodRef = Pointer.newByte();
        if(inBuffer.decode(methodRef).isError(chain)) return chain.get();
        PredictionSchemeMethod predictionSchemeMethod = PredictionSchemeMethod.valueOf(methodRef.get());

        // Check that decoded prediction scheme method type is valid.
        if (predictionSchemeMethod == null) return Status.ioError("Invalid prediction scheme method type");
        if(predictionSchemeMethod != PredictionSchemeMethod.NONE) {
            Pointer<Byte> typeRef = Pointer.newByte();
            if(inBuffer.decode(typeRef).isError(chain)) return chain.get();
            PredictionSchemeTransformType predictionTransformType =
                    PredictionSchemeTransformType.valueOf(typeRef.get());

            // Check that decoded prediction scheme transform type is valid.
            if (predictionTransformType == null) {
                return Status.ioError("Invalid prediction scheme transform type");
            }
            this.predictionScheme = this.createIntPredictionScheme(predictionSchemeMethod, predictionTransformType);
        }

        if (this.predictionScheme != null) {
            if (this.initPredictionScheme(this.predictionScheme).isError(chain)) return chain.get();
        }

        if (this.decodeIntegerValues(pointIds, inBuffer).isError(chain)) return chain.get();

        int numValues = (int) pointIds.size();
        PointCloudDecoder decoder = this.getDecoder();
        if (decoder != null && decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            // For older files, revert the transform right after we decode the data.
            if (this.storeValues(UInt.of(numValues)).isError(chain)) return chain.get();
        }
        return Status.ok();
    }

    protected Status decodeIntegerValues(CppVector<PointIndex> pointIds, DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();

        int numComponents = this.getNumValueComponents();
        if(numComponents <= 0) return Status.ioError("Invalid number of components: " + numComponents);

        long numEntries = pointIds.size();
        long numValues = numEntries * numComponents;
        this.preparePortableAttribute((int) numEntries, numComponents);
        Pointer<Integer> portableAttributeData = this.getPortableAttributeData();
        if(portableAttributeData == null) return Status.ioError("Portable attribute data is null");

        Pointer<UByte> compressedRef = Pointer.newUByte();
        if(inBuffer.decode(compressedRef).isError(chain)) return chain.get();
        UByte compressed = compressedRef.get();

        if(compressed.gt(0)) {
            // Decode compressed values.
            if(SymbolDecoding.decode(UInt.of(numValues), numComponents, inBuffer,
                    portableAttributeData.asRawToUInt()).isError(chain)) return chain.get();
        }
        else {
            // Decode the integer data directly.
            // Get the number of bytes for a given entry.
            Pointer<UByte> numBytesRef = Pointer.newUByte();
            if(inBuffer.decode(numBytesRef).isError(chain)) return chain.get();
            int numBytes = numBytesRef.get().intValue();

            long typeLength = DracoDataType.INT32.getDataTypeLength();
            if(numBytes == typeLength) {
                if(this.getPortableAttribute().getBuffer().size() < typeLength * numValues) {
                    return Status.ioError("Portable attribute data is too small");
                }
                if(inBuffer.decode(portableAttributeData, numValues).isError(chain)) {
                    return chain.get();
                }
            }
            else {
                if(this.getPortableAttribute().getBuffer().size() < (long) numBytes * numValues) {
                    return Status.ioError("Portable attribute data is too small");
                }
                if(inBuffer.getRemainingSize() < (long) numBytes * numValues) {
                    return Status.ioError("Not enough data in the buffer");
                }
                for(int i = 0; i < numValues; i++) {
                    if(inBuffer.decode(portableAttributeData.add(i), numBytes).isError(chain)) return chain.get();
                }
            }
        }

        if(numValues > 0 && (this.predictionScheme == null || !this.predictionScheme.areCorrectionsPositive())) {
            // Convert the values back to the original signed format.
            BitUtils.convertSymbolsToSignedInts(portableAttributeData.asRawToUInt(), (int) numValues, portableAttributeData);
        }

        // If the data was encoded with a prediction scheme, we must revert it.
        if(this.predictionScheme != null) {
            if(this.predictionScheme.decodePredictionData(inBuffer).isError(chain)) return chain.get();
            if(numValues > 0) {
                if(this.predictionScheme.computeOriginalValues(portableAttributeData, portableAttributeData,
                        (int) numValues, numComponents, pointIds.getPointer()).isError(chain)) return chain.get();
            }
        }
        return Status.ok();
    }

    protected PSchemeTypedDecoderInterface<Integer, Integer> createIntPredictionScheme(
            PredictionSchemeMethod method, PredictionSchemeTransformType transformType) {
        if(transformType != PredictionSchemeTransformType.WRAP) {
            return null;  // For now we support only wrap transform.
        }
        return PSchemeDecoderFactory.createPredictionSchemeForDecoder(
                method, this.getAttributeId(), this.getDecoder(),
                new PSchemeWrapDecodingTransform<>(DataType.int32(), DataType.int32()));
    }

    protected int getNumValueComponents() {
        return this.getAttribute().getNumComponents().intValue();
    }

    protected Status storeValues(UInt numValues) {
        DracoDataType dataType = this.getAttribute().getDataType();
        switch (dataType) {
            case UINT8:  this.storeTypedValues(DataType.uint8(),  numValues); break;
            case INT8:   this.storeTypedValues(DataType.int8(),   numValues); break;
            case UINT16: this.storeTypedValues(DataType.uint16(), numValues); break;
            case INT16:  this.storeTypedValues(DataType.int16(),  numValues); break;
            case UINT32: this.storeTypedValues(DataType.uint32(), numValues); break;
            case INT32:  this.storeTypedValues(DataType.int32(),  numValues); break;
            default: return Status.ioError("Invalid data type: " + dataType);
        }
        return Status.ok();
    }

    protected void preparePortableAttribute(int numEntries, int numComponents) {
        GeometryAttribute ga = new GeometryAttribute();
        DracoDataType dataType = DracoDataType.INT32;
        ga.init(this.getAttribute().getAttributeType(), null, UByte.of(numComponents), dataType,
                false, numComponents * dataType.getDataTypeLength(), 0);
        PointAttribute portAtt = new PointAttribute(ga);
        portAtt.setIdentityMapping();
        portAtt.reset(numEntries);
        portAtt.setUniqueId(this.getAttribute().getUniqueId());
        this.setPortableAttribute(portAtt);
    }

    protected Pointer<Integer> getPortableAttributeData() {
        PointAttribute portableAttribute = this.getPortableAttribute();
        if(portableAttribute.size() == 0) return null;
        return portableAttribute.getAddress(AttributeValueIndex.of(0)).toInt();
    }

    private <U> void storeTypedValues(DataNumberType<U> type, UInt numValues) {
        int numComponents = this.getAttribute().getNumComponents().intValue();
        Pointer<U> attVal = type.newArray(numComponents);
        Pointer<Integer> portableAttributeData = this.getPortableAttributeData();
        int valId = 0;
        int outBytePos = 0;
        for(int i = 0; i < numValues.intValue(); i++) {
            for(int c = 0; c < numComponents; c++) {
                U value = type.from(portableAttributeData.get(valId++));
                attVal.set(c, value);
            }
            this.getAttribute().getBuffer().write(outBytePos, attVal, numComponents);
            outBytePos += numComponents;
        }
    }
}
