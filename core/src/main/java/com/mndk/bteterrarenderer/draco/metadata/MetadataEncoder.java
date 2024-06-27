package com.mndk.bteterrarenderer.draco.metadata;

import com.mndk.bteterrarenderer.draco.core.BitUtils;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.array.UByteArray;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class MetadataEncoder {

    public Status encodeMetadata(EncoderBuffer outBuffer, Metadata metadata) {
        StatusChain chain = Status.newChain();

        Map<String, Metadata.EntryValue> entries = metadata.getEntries();
        // Encode number of entries.
        BitUtils.encodeVarint(DataType.uint32(), UInt.of(metadata.getNumEntries()), outBuffer);
        // Encode all entries.
        for(Map.Entry<String, Metadata.EntryValue> entry : entries.entrySet()) {
            if(encodeString(outBuffer, entry.getKey()).isError(chain)) return chain.get();
            UByteArray entryValue = entry.getValue().getBuffer();
            BitUtils.encodeVarint(DataType.uint32(), UInt.of(entryValue.size()), outBuffer);
            outBuffer.encode(DataType.bytes(entryValue.size()), entryValue);
        }

        Map<String, Metadata> subMetadatas = metadata.getSubMetadatas();
        // Encode number of sub-metadata
        BitUtils.encodeVarint(DataType.uint32(), UInt.of(subMetadatas.size()), outBuffer);
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
        if(BitUtils.encodeVarint(DataType.uint32(), metadata.getAttUniqueId(), outBuffer).isError(chain)) return chain.get();
        if(encodeMetadata(outBuffer, metadata).isError(chain)) return chain.get();
        return Status.OK;
    }

    public Status encodeGeometryMetadata(EncoderBuffer outBuffer, GeometryMetadata metadata) {
        StatusChain chain = Status.newChain();

        if(metadata == null) {
            return new Status(Status.Code.INVALID_PARAMETER, "metadata is null");
        }
        // Encode number of attribute metadata.
        BitUtils.encodeVarint(DataType.uint32(), UInt.of(metadata.getAttributeMetadatas().size()), outBuffer);
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
        UByteArray bytes = UByteArray.create(str, StandardCharsets.UTF_8);
        if(bytes.size() > 255) {
            return new Status(Status.Code.INVALID_PARAMETER, "string is longer than 255 bytes");
        }
        if(bytes.size() == 0) {
            outBuffer.encode(DataType.uint8(), UByte.ZERO);
        } else {
            outBuffer.encode(DataType.uint8(), UByte.of(str.length()));
            outBuffer.encode(DataType.bytes(bytes.size()), bytes);
        }
        return Status.OK;
    }

}
