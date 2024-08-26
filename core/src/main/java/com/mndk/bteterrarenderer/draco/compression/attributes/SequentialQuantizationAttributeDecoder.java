package com.mndk.bteterrarenderer.draco.compression.attributes;

import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.draco.attributes.AttributeQuantizationTransform;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.pointcloud.PointCloudDecoder;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.DracoDataType;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;

public class SequentialQuantizationAttributeDecoder extends SequentialIntegerAttributeDecoder {

    private final AttributeQuantizationTransform quantizationTransform = new AttributeQuantizationTransform();

    @Override
    public Status init(PointCloudDecoder decoder, int attributeId) {
        StatusChain chain = new StatusChain();
        if (super.init(decoder, attributeId).isError(chain)) return chain.get();
        PointAttribute attribute = decoder.getPointCloud().getAttribute(attributeId);
        if (attribute.getDataType() != DracoDataType.FLOAT32) {
            return Status.dracoError("Currently we can quantize only floating point arguments.");
        }
        return Status.ok();
    }

    @Override
    protected Status decodeIntegerValues(CppVector<PointIndex> pointIds, DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();

        if(this.getDecoder().getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            if(this.decodeQuantizedDataInfo().isError(chain)) return chain.get();
        }
        return super.decodeIntegerValues(pointIds, inBuffer);
    }

    @Override
    public Status decodeDataNeededByPortableTransform(CppVector<PointIndex> pointIds, DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();

        if(this.getDecoder().getBitstreamVersion() >= DracoVersions.getBitstreamVersion(2, 0)) {
            if(this.decodeQuantizedDataInfo().isError(chain)) return chain.get();
        }
        return this.quantizationTransform.transferToAttribute(this.getPortableAttributeInternal());
    }

    @Override
    public Status storeValues(UInt numValues) {
        return this.dequantizeValues(numValues);
    }

    public Status decodeQuantizedDataInfo() {
        PointAttribute attribute = this.getPortableAttribute();
        if (attribute == null) attribute = this.getAttribute();
        return this.quantizationTransform.decodeParameters(attribute, this.getDecoder().getBuffer());
    }

    public Status dequantizeValues(UInt numValues) {
        return this.quantizationTransform.inverseTransformAttribute(
                this.getPortableAttribute(), this.getAttribute());
    }
}