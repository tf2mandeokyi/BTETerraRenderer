package com.mndk.bteterrarenderer.draco.compression.attributes;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.compression.pointcloud.PointCloudEncoder;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.pointcloud.PointCloud;
import lombok.Getter;

public abstract class AttributesEncoder {

    private final CppVector<Integer> pointAttributeIds = new CppVector<>(DataType.int32());
    private final CppVector<Integer> pointAttributeToLocalIdMap = new CppVector<>(DataType.int32());
    @Getter
    private PointCloudEncoder pointCloudEncoder = null;
    private PointCloud pointCloud = null;

    public AttributesEncoder() {}

    public AttributesEncoder(int pointAttribId) {
        this.addAttributeId(pointAttribId);
    }

    public Status init(PointCloudEncoder encoder, PointCloud pc) {
        this.pointCloudEncoder = encoder;
        this.pointCloud = pc;
        return Status.ok();
    }

    public Status encodeAttributesEncoderData(EncoderBuffer outBuffer) {
        outBuffer.encodeVarint(UInt.of(this.getNumAttributes()));
        for(int i = 0; i < this.getNumAttributes(); i++) {
            int attId = this.getAttributeId(i);
            PointAttribute pa = this.pointCloud.getAttribute(attId);
            GeometryAttribute.Type type = pa.getAttributeType();
            outBuffer.encode(UByte.of(type.getIndex()));
            outBuffer.encode(pa.getDataType().getId());
            outBuffer.encode(pa.getNumComponents());
            outBuffer.encode(UByte.of(pa.isNormalized() ? 1 : 0));
            outBuffer.encodeVarint(pa.getUniqueId());
        }
        return Status.ok();
    }

    public abstract UByte getUniqueId();

    public Status encodeAttributes(EncoderBuffer outBuffer) {
        StatusChain chain = new StatusChain();

        if(this.transformAttributesToPortableFormat().isError(chain)) return chain.get();
        if(this.encodePortableAttributes(outBuffer).isError(chain)) return chain.get();
        // Encode data needed by portable transforms after the attribute is encoded.
        return this.encodeDataNeededByPortableTransforms(outBuffer);
    }

    public int getNumParentAttributes(int pointAttributeId) {
        return 0;
    }

    public int getParentAttributeId(int pointAttributeId, int parentIndex) {
        return -1;
    }

    public Status markParentAttribute(int pointAttributeId) {
        return Status.unsupportedFeature("Not implemented");
    }

    public PointAttribute getPortableAttribute(int pointAttributeId) {
        return null;
    }

    public void addAttributeId(int id) {
        pointAttributeIds.pushBack(id);
        if(id >= pointAttributeToLocalIdMap.size()) {
            pointAttributeToLocalIdMap.resize(id + 1, -1);
        }
        pointAttributeToLocalIdMap.set(id, pointAttributeIds.size() - 1);
    }

    public void setAttributeIds(CppVector<Integer> pointAttributeIds) {
        this.pointAttributeIds.clear();
        this.pointAttributeToLocalIdMap.clear();
        for(int attId : pointAttributeIds) {
            addAttributeId(attId);
        }
    }

    public int getAttributeId(int i) {
        return pointAttributeIds.get(i);
    }

    public int getNumAttributes() {
        return pointAttributeIds.size();
    }

    protected Status transformAttributesToPortableFormat() {
        return Status.ok();
    }

    protected abstract Status encodePortableAttributes(EncoderBuffer outBuffer);

    protected Status encodeDataNeededByPortableTransforms(EncoderBuffer outBuffer) {
        return Status.ok();
    }

    public int getLocalIdForPointAttribute(int pointAttributeId) {
        int idMapSize = pointAttributeToLocalIdMap.size();
        if(pointAttributeId >= idMapSize) {
            return -1;
        }
        return pointAttributeToLocalIdMap.get(pointAttributeId);
    }

}
