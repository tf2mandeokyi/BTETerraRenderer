package com.mndk.bteterrarenderer.draco.compression.attributes;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.attributes.AttributeValueIndex;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.attributes.scheme.PSchemeInterface;
import com.mndk.bteterrarenderer.draco.compression.config.SequentialAttributeEncoderType;
import com.mndk.bteterrarenderer.draco.compression.pointcloud.PointCloudEncoder;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class SequentialAttributeEncoder {

    @Getter
    private PointCloudEncoder encoder = null;
    @Getter
    private PointAttribute attribute = null;
    @Getter
    private int attributeId = -1;
    private final CppVector<Integer> parentAttributes = new CppVector<>(DataType.int32());
    @Getter(AccessLevel.PROTECTED)
    private boolean isParentEncoder = false;
    @Setter
    private PointAttribute portableAttribute;

    public Status init(PointCloudEncoder encoder, int attributeId) {
        this.encoder = encoder;
        this.attribute = encoder.getPointCloud().getAttribute(attributeId);
        this.attributeId = attributeId;
        return Status.ok();
    }

    public Status initializeStandalone(PointAttribute attribute) {
        this.attribute = attribute;
        this.attributeId = -1;
        return Status.ok();
    }

    public Status transformAttributeToPortableFormat(CppVector<PointIndex> pointIds) {
        // Default implementation doesn't transform the input data.
        return Status.ok();
    }

    public Status encodePortableAttribute(CppVector<PointIndex> pointIds, EncoderBuffer outBuffer) {
        // Lossless encoding of the input values.
        return this.encodeValues(pointIds, outBuffer);
    }

    public Status encodeDataNeededByPortableTransform(EncoderBuffer outBuffer) {
        // Default implementation doesn't transform the input data.
        return Status.ok();
    }

    public boolean isLossyEncoder() {
        return false;
    }

    public int getNumParentAttributes() {
        return (int) parentAttributes.size();
    }

    public int getParentAttributeId(int i) {
        return parentAttributes.get(i);
    }

    public PointAttribute getPortableAttribute() {
        return portableAttribute != null ? portableAttribute : this.getAttribute();
    }

    /**
     * Returns a mutable attribute that should be filled by derived encoders with
     * the transformed version of the attribute data. To get a public const
     * version, use the {@link #getPortableAttribute()} method.
     */
    protected PointAttribute getPortableAttributeInternal() {
        return portableAttribute;
    }

    public void markParentAttribute() {
        isParentEncoder = true;
    }

    public UByte getUniqueId() {
        return UByte.of(SequentialAttributeEncoderType.GENERIC.getValue());
    }

    protected Status initPredictionScheme(PSchemeInterface ps) {
        for(int i = 0; i < ps.getNumParentAttributes(); i++) {
            int attId = encoder.getPointCloud().getNamedAttributeId(ps.getParentAttributeType(i));
            if(attId == -1) {
                return Status.dracoError("Requested attribute does not exist.");
            }
            parentAttributes.pushBack(attId);
            encoder.markParentAttribute(attId);
        }
        return Status.ok();
    }

    protected Status setPredictionSchemeParentAttributes(PSchemeInterface ps) {
        StatusChain chain = new StatusChain();

        for(int i = 0; i < ps.getNumParentAttributes(); i++) {
            int attId = encoder.getPointCloud().getNamedAttributeId(ps.getParentAttributeType(i));
            if(attId == -1) return Status.dracoError("Requested attribute does not exist.");
            if(ps.setParentAttribute(encoder.getPortableAttribute(attId)).isError(chain)) return chain.get();
        }
        return Status.ok();
    }

    protected Status encodeValues(CppVector<PointIndex> pointIds, EncoderBuffer outBuffer) {
        int entrySize = (int) attribute.getByteStride();
        Pointer<UByte> valueData = Pointer.newUByteArray(entrySize);
        for(int i = 0; i < pointIds.size(); i++) {
            AttributeValueIndex entryId = attribute.getMappedIndex(pointIds.get(i));
            attribute.getValue(entryId, valueData, entrySize);
            outBuffer.encode(valueData, entrySize);
        }
        return Status.ok();
    }

}
