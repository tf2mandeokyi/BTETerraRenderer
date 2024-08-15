package com.mndk.bteterrarenderer.draco.compression.attributes;

import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.compression.pointcloud.PointCloudDecoder;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.pointcloud.PointCloud;

/**
 * Interface class for decoding one or more attributes that were encoded with a
 * matching AttributesEncoder. It provides only the basic interface
 * that is used by the PointCloudDecoder. The actual decoding must be
 * implemented in derived classes using the DecodeAttributes() method.
 */
public interface AttributesDecoderInterface {

    /**
     * Called after all attribute decoders are created. It can be used to perform
     * any custom initialization.
     */
    Status init(PointCloudDecoder decoder, PointCloud pc);

    /**
     * Decodes any attribute decoder specific data from the |in_buffer|.
     */
    Status decodeAttributesDecoderData(DecoderBuffer inBuffer);

    /**
     * Decode attribute data from the source buffer. Needs to be implemented by
     * the derived classes.
     */
    Status decodeAttributes(DecoderBuffer inBuffer);

    int getAttributeId(int i);
    int getNumAttributes();
    PointCloudDecoder getDecoder();

    /**
     * Returns an attribute containing data processed by the attribute transform.
     * (see TransformToPortableFormat() method). This data is guaranteed to be
     * same for encoder and decoder, and it can be used by predictors.
     */
    default PointAttribute getPortableAttribute(int pointAttributeId) {
        return null;
    }
}
