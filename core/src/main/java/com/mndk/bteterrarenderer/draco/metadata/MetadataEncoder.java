package com.mndk.bteterrarenderer.draco.metadata;

import com.mndk.bteterrarenderer.draco.core.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class MetadataEncoder {

    public Status encodeMetadata(EncoderBuffer outBuffer, Metadata metadata) {
        StatusChain chain = Status.newChain();

        Map<String, Metadata.EntryValue> entries = metadata.getEntries();
        // Encode number of entries.
        BitUtils.encodeVarint(DataType.UINT32, (long) metadata.getNumEntries(), outBuffer);
        // Encode all entries.
        for(Map.Entry<String, Metadata.EntryValue> entry : entries.entrySet()) {
            if(encodeString(outBuffer, entry.getKey()).isError(chain)) return chain.get();
            DataBuffer entryValue = entry.getValue().getBuffer();
            BitUtils.encodeVarint(DataType.UINT32, (long) entryValue.size(), outBuffer);
            outBuffer.encode(DataType.bytes(entryValue.size()), entryValue.getData());
        }

        Map<String, Metadata> subMetadatas = metadata.getSubMetadatas();
        // Encode number of sub-metadata
        BitUtils.encodeVarint(DataType.UINT32, (long) subMetadatas.size(), outBuffer);
        // Encode each sub-metadata
        for(Map.Entry<String, Metadata> subMetadataEntry : subMetadatas.entrySet()) {
            if(encodeString(outBuffer, subMetadataEntry.getKey()).isError(chain)) return chain.get();
            if(encodeMetadata(outBuffer, subMetadataEntry.getValue()).isError(chain)) return chain.get();
        }

        return Status.OK;
    }

    public Status encodeAttributeMetadata(EncoderBuffer outBuffer, AttributeMetadata metadata) {
        StatusChain chain = Status.newChain();

        if(metadata == null) {
            return new Status(Status.Code.INVALID_PARAMETER, "metadata is null");
        }
        // Encode attribute id.
        if(BitUtils.encodeVarint(DataType.UINT32, metadata.getAttUniqueId(), outBuffer).isError(chain)) return chain.get();
        if(encodeMetadata(outBuffer, metadata).isError(chain)) return chain.get();
        return Status.OK;
    }

    public Status encodeGeometryMetadata(EncoderBuffer outBuffer, GeometryMetadata metadata) {
        StatusChain chain = Status.newChain();

        if(metadata == null) {
            return new Status(Status.Code.INVALID_PARAMETER, "metadata is null");
        }
        // Encode number of attribute metadata.
        BitUtils.encodeVarint(DataType.UINT32, (long) metadata.getAttributeMetadatas().size(), outBuffer);
        // Encode each attribute metadata
        for(AttributeMetadata attMetadata : metadata.getAttributeMetadatas()) {
            if(encodeAttributeMetadata(outBuffer, attMetadata).isError(chain)) return chain.get();
        }
        // Encode normal metadata part.
        if(encodeMetadata(outBuffer, metadata).isError(chain)) return chain.get();
        return Status.OK;
    }

    public Status encodeString(EncoderBuffer outBuffer, String str) {
        // We only support string of maximum length 255 which is using one byte to
        // encode the length.
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        if(bytes.length > 255) {
            return new Status(Status.Code.INVALID_PARAMETER, "string is longer than 255 bytes");
        }
        if(bytes.length == 0) {
            outBuffer.encode(DataType.UINT8, (short) 0);
        } else {
            outBuffer.encode(DataType.UINT8, (short) str.length());
            outBuffer.encode(DataType.bytes(str.length()), bytes);
        }
        return Status.OK;
    }

}
