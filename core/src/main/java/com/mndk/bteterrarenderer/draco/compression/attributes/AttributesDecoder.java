package com.mndk.bteterrarenderer.draco.compression.attributes;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.UShort;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.pointcloud.PointCloudDecoder;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.DracoDataType;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.pointcloud.PointCloud;

public abstract class AttributesDecoder implements AttributesDecoderInterface {

    private final CppVector<Integer> pointAttributeIds = new CppVector<>(DataType.int32());
    private final CppVector<Integer> pointAttributeToLocalIdMap = new CppVector<>(DataType.int32());
    private PointCloudDecoder pointCloudDecoder = null;
    private PointCloud pointCloud = null;

    @Override
    public Status init(PointCloudDecoder decoder, PointCloud pc) {
        this.pointCloudDecoder = decoder;
        this.pointCloud = pc;
        return Status.ok();
    }

    @Override
    public Status decodeAttributesDecoderData(DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();

        // Decode and create attributes
        Pointer<UInt> numAttributesRef = Pointer.newUInt();
        if(pointCloudDecoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            if(inBuffer.decode(numAttributesRef).isError(chain)) return chain.get();
        } else {
            if(inBuffer.decodeVarint(numAttributesRef).isError(chain)) return chain.get();
        }
        UInt numAttributes = numAttributesRef.get();

        // Check that decoded number of attributes is valid
        if(numAttributes.equals(0)) {
            return Status.dracoError("Number of attributes is zero");
        }
        if(numAttributes.gt(5 * inBuffer.getRemainingSize())) {
            return Status.dracoError("Decoded number of attributes is unreasonably high");
        }

        // Decode attribute descriptor data
        pointAttributeIds.resize(numAttributes.intValue());
        PointCloud pc = this.pointCloud;
        for(UInt i = UInt.ZERO; i.lt(numAttributes); i = i.add(1)) {
            // Decode attribute descriptor data
            Pointer<UByte> attTypeRef = Pointer.newUByte();
            if(inBuffer.decode(attTypeRef).isError(chain)) return chain.get();
            GeometryAttribute.Type attType = GeometryAttribute.Type.valueOf(attTypeRef.get());

            Pointer<UByte> dataTypeRef = Pointer.newUByte();
            if(inBuffer.decode(dataTypeRef).isError(chain)) return chain.get();
            DracoDataType dataType = DracoDataType.valueOf(dataTypeRef.get());

            Pointer<UByte> numComponentsRef = Pointer.newUByte();
            if(inBuffer.decode(numComponentsRef).isError(chain)) return chain.get();
            UByte numComponents = numComponentsRef.get();

            Pointer<UByte> normalizedRef = Pointer.newUByte();
            if(inBuffer.decode(normalizedRef).isError(chain)) return chain.get();
            UByte normalized = normalizedRef.get();

            if(attType == GeometryAttribute.Type.INVALID) {
                return Status.dracoError("Invalid attribute type: " + attTypeRef.get());
            }
            if(dataType == DracoDataType.INVALID) {
                return Status.dracoError("Invalid data type: " + dataTypeRef.get());
            }

            // Check decoded attribute descriptor data
            if(numComponents.equals(0)) {
                return Status.dracoError("Number of components is zero");
            }

            // Add the attribute to the point cloud
            GeometryAttribute ga = new GeometryAttribute();
            ga.init(attType, null, numComponents, dataType, normalized.gt(UByte.ZERO),
                    dataType.getDataTypeLength() * numComponents.intValue(), 0);
            Pointer<UInt> uniqueIdRef = Pointer.newUInt();
            if(pointCloudDecoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(1, 3)) {
                Pointer<UShort> customIdRef = Pointer.newUShort();
                if(inBuffer.decode(customIdRef).isError(chain)) return chain.get();
                uniqueIdRef.set(customIdRef.get().uIntValue());
            }
            else {
                if(inBuffer.decodeVarint(uniqueIdRef).isError(chain)) return chain.get();
            }
            UInt uniqueId = uniqueIdRef.get();
            ga.setUniqueId(uniqueId);
            int attId = pc.addAttribute(new PointAttribute(ga));
            pc.getAttribute(attId).setUniqueId(uniqueId);
            pointAttributeIds.set(i.intValue(), attId);

            // Update the inverse map
            if(attId >= pointAttributeToLocalIdMap.size()) {
                pointAttributeToLocalIdMap.resize(attId + 1, -1);
            }
            pointAttributeToLocalIdMap.set(attId, i.intValue());
        }
        return Status.ok();
    }

    @Override
    public Status decodeAttributes(DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();
        if(this.decodePortableAttributes(inBuffer).isError(chain)) return chain.get();
        if(this.decodeDataNeededByPortableTransforms(inBuffer).isError(chain)) return chain.get();
        if(this.transformAttributesToOriginalFormat().isError(chain)) return chain.get();
        return Status.ok();
    }

    @Override
    public int getAttributeId(int i) {
        return this.pointAttributeIds.get(i);
    }

    @Override
    public int getNumAttributes() {
        return (int) this.pointAttributeIds.size();
    }

    @Override
    public PointCloudDecoder getDecoder() {
        return this.pointCloudDecoder;
    }

    protected int getLocalIdForPointAttribute(int pointAttributeId) {
        int idMapSize = (int) this.pointAttributeToLocalIdMap.size();
        if(pointAttributeId >= idMapSize) {
            return -1;
        }
        return this.pointAttributeToLocalIdMap.get(pointAttributeId);
    }

    protected abstract Status decodePortableAttributes(DecoderBuffer inBuffer);
    protected Status decodeDataNeededByPortableTransforms(DecoderBuffer inBuffer) {
        return Status.ok();
    }
    protected Status transformAttributesToOriginalFormat() {
        return Status.ok();
    }
}
