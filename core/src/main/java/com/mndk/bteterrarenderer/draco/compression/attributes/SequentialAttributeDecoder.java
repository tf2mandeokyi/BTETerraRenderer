package com.mndk.bteterrarenderer.draco.compression.attributes;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.attributes.scheme.PSchemeInterface;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.pointcloud.PointCloudDecoder;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import lombok.Getter;

@Getter
public class SequentialAttributeDecoder {

    private PointCloudDecoder decoder = null;
    private PointAttribute attribute = null;
    private int attributeId = -1;

    private PointAttribute portableAttribute = null;

    public Status init(PointCloudDecoder decoder, int attributeId) {
        this.decoder = decoder;
        this.attribute = decoder.getPointCloud().getAttribute(attributeId);
        this.attributeId = attributeId;
        return Status.ok();
    }

    public Status initializeStandalone(PointAttribute attribute) {
        this.attribute = attribute;
        this.attributeId = -1;
        return Status.ok();
    }

    public Status decodePortableAttribute(CppVector<PointIndex> pointIds, DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();

        if(attribute.getNumComponents().le(0)) {
            return Status.dracoError("Attribute has no components");
        }
        if(attribute.reset(pointIds.size()).isError(chain)) return chain.get();
        if(this.decodeValues(pointIds, inBuffer).isError(chain)) return chain.get();
        return Status.ok();
    }

    public Status decodeDataNeededByPortableTransform(CppVector<PointIndex> pointIds, DecoderBuffer inBuffer) {
        // Default implementation does not apply any transform.
        return Status.ok();
    }

    public Status transformAttributeToOriginalFormat(CppVector<PointIndex> pointIds) {
        // Default implementation does not apply any transform.
        return Status.ok();
    }

    protected PointAttribute getPortableAttributeInternal() {
        return portableAttribute;
    }

    public PointAttribute getPortableAttribute() {
        // If needed, copy point to attribute value index mapping from the final
        // attribute to the portable attribute.
        if(!attribute.isMappingIdentity() && portableAttribute != null && portableAttribute.isMappingIdentity()) {
            portableAttribute.setExplicitMapping(attribute.indicesMapSize());
            for(PointIndex i : PointIndex.range(0, (int) attribute.indicesMapSize())) {
                portableAttribute.setPointMapEntry(i, attribute.getMappedIndex(i));
            }
        }
        return portableAttribute;
    }

    protected Status initPredictionScheme(PSchemeInterface ps) {
        StatusChain chain = new StatusChain();

        for(int i = 0; i < ps.getNumParentAttributes(); i++) {
            int attId = decoder.getPointCloud().getNamedAttributeId(ps.getParentAttributeType(i));
            if(attId == -1) {
                return Status.dracoError("Requested attribute does not exist");
            }
            if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
                if(ps.setParentAttribute(decoder.getPointCloud().getAttribute(attId)).isError(chain)) return chain.get();
            }
            else {
                PointAttribute pa = decoder.getPortableAttribute(attId);
                if (pa == null) {
                    return Status.dracoError("Requested attribute does not exist");
                }
                if (ps.setParentAttribute(pa).isError(chain)) return chain.get();
            }
        }
        return Status.ok();
    }

    protected Status decodeValues(CppVector<PointIndex> pointIds, DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();

        int numValues = (int) pointIds.size();
        int entrySize = (int) attribute.getByteStride();
        Pointer<UByte> valueData = Pointer.newUByteArray(entrySize);
        int outBytePos = 0;
        for(int i = 0; i < numValues; i++) {
            if(inBuffer.decode(valueData, entrySize).isError(chain)) return chain.get();
            attribute.getBuffer().write(outBytePos, valueData, entrySize);
            outBytePos += entrySize;
        }
        return Status.ok();
    }

    protected void setPortableAttribute(PointAttribute att) {
        this.portableAttribute = att;
    }

}
