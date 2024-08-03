package com.mndk.bteterrarenderer.draco.compression.attributes;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.config.SequentialAttributeEncoderType;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;

public class SequentialAttributeDecodersController extends AttributesDecoder {

    private final CppVector<SequentialAttributeDecoder> sequentialDecoders = new CppVector<>(SequentialAttributeDecoder::new);
    private final CppVector<PointIndex> pointIds = new CppVector<>(PointIndex.type());
    private final PointsSequencer sequencer;

    public SequentialAttributeDecodersController(PointsSequencer sequencer) {
        this.sequencer = sequencer;
    }

    @Override
    public Status decodeAttributesDecoderData(DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();

        if(super.decodeAttributesDecoderData(inBuffer).isError(chain)) return chain.get();

        // Decode unique ids of all sequential encoders and create them.
        final int numAttributes = getNumAttributes();
        sequentialDecoders.resize(numAttributes);
        for(int i = 0; i < numAttributes; ++i) {
            Pointer<UByte> decoderTypeRef = Pointer.newUByte();
            if(inBuffer.decode(decoderTypeRef).isError(chain)) return chain.get();
            SequentialAttributeEncoderType decoderType = SequentialAttributeEncoderType.valueOf(decoderTypeRef.get());
            if(decoderType == null) {
                return Status.ioError("Failed to decode sequential attribute encoder type");
            }

            // Create the decoder from the id.
            SequentialAttributeDecoder decoder = this.createSequentialDecoder(decoderType);
            if(decoder == null) return Status.ioError("Failed to create sequential decoder");
            if(decoder.init(this.getDecoder(), getAttributeId(i)).isError(chain)) return chain.get();

            sequentialDecoders.set(i, decoder);
        }
        return Status.ok();
    }

    @Override
    public Status decodeAttributes(DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();
        if(sequencer == null) return Status.ioError("Sequencer is null");
        if(sequencer.generateSequence(pointIds).isError(chain)) return chain.get();
        // Initialize point to attribute value mapping for all decoded attributes.
        final int numAttributes = getNumAttributes();
        for(int i = 0; i < numAttributes; ++i) {
            PointAttribute pa = this.getDecoder().getPointCloud().getAttribute(getAttributeId(i));
            if(sequencer.updatePointToAttributeIndexMapping(pa).isError(chain)) return chain.get();
        }
        return super.decodeAttributes(inBuffer);
    }

    @Override
    public PointAttribute getPortableAttribute(int pointAttributeId) {
        int locId = getLocalIdForPointAttribute(pointAttributeId);
        if(locId < 0) return null;
        return sequentialDecoders.get(locId).getPortableAttribute();
    }

    @Override
    protected Status decodePortableAttributes(DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();
        final int numAttributes = getNumAttributes();
        for(int i = 0; i < numAttributes; ++i) {
            SequentialAttributeDecoder decoder = sequentialDecoders.get(i);
            if(decoder.decodePortableAttribute(pointIds, inBuffer).isError(chain)) return chain.get();
        }
        return Status.ok();
    }

    @Override
    protected Status decodeDataNeededByPortableTransforms(DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();
        final int numAttributes = getNumAttributes();
        for(int i = 0; i < numAttributes; ++i) {
            SequentialAttributeDecoder decoder = sequentialDecoders.get(i);
            if(decoder.decodeDataNeededByPortableTransform(pointIds, inBuffer).isError(chain)) return chain.get();
        }
        return Status.ok();
    }

    @Override
    protected Status transformAttributesToOriginalFormat() {
        StatusChain chain = new StatusChain();
        final int numAttributes = getNumAttributes();
        for(int i = 0; i < numAttributes; ++i) {
            SequentialAttributeDecoder decoder = sequentialDecoders.get(i);
            // Check whether the attribute transform should be skipped.
            if (this.getDecoder().getOptions() != null) {
                PointAttribute attribute = decoder.getAttribute();
                PointAttribute portableAttribute = decoder.getPortableAttribute();
                if (portableAttribute != null && this.getDecoder().getOptions().getAttributeBool(
                        attribute.getAttributeType(), "skip_attribute_transform", false)) {
                    // Attribute transform should not be performed. In this case, we replace
                    // the output geometry attribute with the portable attribute.
                    sequentialDecoders.get(i).getAttribute().copyFrom(portableAttribute);
                    continue;
                }
            }
            if (decoder.transformAttributeToOriginalFormat(pointIds).isError(chain)) return chain.get();
        }
        return Status.ok();
    }

    protected SequentialAttributeDecoder createSequentialDecoder(SequentialAttributeEncoderType decoderType) {
        switch (decoderType) {
            case GENERIC: return new SequentialAttributeDecoder();
            case INTEGER: return new SequentialIntegerAttributeDecoder();
            case QUANTIZATION: return new SequentialQuantizationAttributeDecoder();
            case NORMALS: return new SequentialNormalAttributeDecoder();
            default: return null; // Unknown or unsupported decoder type.
        }
    }

}
