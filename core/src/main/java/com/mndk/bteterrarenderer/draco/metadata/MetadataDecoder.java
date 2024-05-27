package com.mndk.bteterrarenderer.draco.metadata;

import com.mndk.bteterrarenderer.draco.util.DracoCompressionException;
import com.mndk.bteterrarenderer.draco.util.BitUtils;
import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Stack;

/**
 * Class for decoding the metadata.
 */
public class MetadataDecoder {

    private static final int kMaxSubmetadataLevel = 1000;

    private ByteBuf buf = null;

    public void decodeMetadata(ByteBuf buf, Metadata metadata) throws DracoCompressionException {
        if(metadata == null) throw new DracoCompressionException("Metadata is null");
        this.buf = buf;
        this.decodeMetadata(metadata);
    }

    public void decodeGeometryMetadata(ByteBuf buf, GeometryMetadata metadata) throws DracoCompressionException {
        if(metadata == null) throw new DracoCompressionException("Metadata is null");
        this.buf = buf;
        int numAttMetadata = BitUtils.decodeVariableIntegerLE(buf);
        for(int i = 0; i < numAttMetadata; i++) {
            int attUniqueId = BitUtils.decodeVariableIntegerLE(buf);
            AttributeMetadata attMetadata = new AttributeMetadata();
            attMetadata.setAttUniqueId(attUniqueId);
            this.decodeMetadata(attMetadata);
            metadata.addAttributeMetadata(attMetadata);
        }
        this.decodeMetadata(metadata);
    }

    private void decodeMetadata(Metadata metadata) throws DracoCompressionException {

        @RequiredArgsConstructor
        class MetadataTuple {
            final Metadata parentMetadata;
            final Metadata decodedMetadata;
            final int level;
        }
        Stack<MetadataTuple> metadataStack = new Stack<>();
        metadataStack.push(new MetadataTuple(null, metadata, 0));
        while(!metadataStack.isEmpty()) {
            MetadataTuple mp = metadataStack.pop();
            metadata = mp.decodedMetadata;

            if(mp.parentMetadata != null) {
                if(mp.level > kMaxSubmetadataLevel) {
                    throw new DracoCompressionException("stack overflow on metadata decode");
                }
                String subMetadataName = this.decodeName();
                Metadata subMetadata = new Metadata();
                metadata = subMetadata;
                mp.parentMetadata.addSubMetadata(subMetadataName, subMetadata);
            }
            if(metadata == null) {
                throw new DracoCompressionException("metadata is null");
            }

            int numEntries = BitUtils.decodeVariableIntegerLE(buf);
            for(int i = 0; i < numEntries; i++) {
                this.decodeEntry(metadata);
            }
            int numSubMetadata = BitUtils.decodeVariableIntegerLE(buf);
            if(numSubMetadata > buf.readableBytes()) {
                throw new DracoCompressionException("submetadata length exceeded");
            }
            for(int i = 0; i < numSubMetadata; i++) {
                metadataStack.push(new MetadataTuple(metadata, null,
                        mp.parentMetadata != null ? mp.level + 1 : mp.level));
            }
        }
    }

    private void decodeEntry(Metadata metadata) throws DracoCompressionException {
        String entryName = this.decodeName();
        int dataSize = BitUtils.decodeVariableIntegerLE(buf);
        if(dataSize == 0) {
            throw new DracoCompressionException("data size is 0");
        }
        if(dataSize > buf.readableBytes()) {
            throw new DracoCompressionException("data size exceeded");
        }
        ByteBuf entryValue = buf.readBytes(dataSize);
        metadata.addEntryBinary(entryName, entryValue);
    }

    private String decodeName() throws DracoCompressionException {
        short nameLen = buf.readUnsignedByte();
        if(nameLen == 0) {
            throw new DracoCompressionException("name length is 0");
        }
        return buf.readBytes(nameLen).toString(StandardCharsets.UTF_8);
    }

}
