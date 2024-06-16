package com.mndk.bteterrarenderer.draco.metadata;

import com.mndk.bteterrarenderer.draco.core.*;
import lombok.AllArgsConstructor;

import java.util.concurrent.atomic.AtomicReference;

public class MetadataDecoder {

    private DecoderBuffer buffer = null;

    public Status decodeMetadata(DecoderBuffer inBuffer, Metadata metadata) {
        if(metadata == null) {
            return new Status(Status.Code.INVALID_PARAMETER, "Metadata is null.");
        }
        buffer = inBuffer;
        return decodeMetadata(metadata);
    }

    public Status decodeGeometryMetadata(DecoderBuffer inBuffer, GeometryMetadata metadata) {
        StatusChain chain = Status.newChain();
        
        if(metadata == null) {
            return new Status(Status.Code.INVALID_PARAMETER, "Metadata is null.");
        }
        buffer = inBuffer;
        AtomicReference<Long> numAttMetadata = new AtomicReference<>(0L);
        if(BitUtils.decodeVarint(DataType.UINT32, numAttMetadata, buffer).isError(chain)) return chain.get();

        // Decode attribute metadata.
        for(int i = 0; i < numAttMetadata.get(); ++i) {
            AtomicReference<Long> attUniqueId = new AtomicReference<>(0L);
            if(BitUtils.decodeVarint(DataType.UINT32, attUniqueId, buffer).isError(chain)) return chain.get();
            AttributeMetadata attMetadata = new AttributeMetadata();
            attMetadata.setAttUniqueId(attUniqueId.get());
            if(decodeMetadata(attMetadata).isError(chain)) return chain.get();
            metadata.addAttributeMetadata(attMetadata);
        }
        return decodeMetadata(metadata);
    }

    private Status decodeMetadata(Metadata metadata) {
        StatusChain chain = Status.newChain();

        // Limit metadata nesting depth to avoid stack overflow in destructor.
        final int kMaxSubmetadataLevel = 1000;

        @AllArgsConstructor
        class MetadataTuple {
            Metadata parentMetadata, decodedMetadata;
            int level;
        }
        CppVector<MetadataTuple> metadataStack = new CppVector<>();
        metadataStack.pushBack(new MetadataTuple(null, metadata, 0));
        while(!metadataStack.isEmpty()) {
            MetadataTuple mp = metadataStack.popBack();
            metadata = mp.decodedMetadata;

            if(mp.parentMetadata != null) {
                if(mp.level > kMaxSubmetadataLevel) {
                    return new Status(Status.Code.DRACO_ERROR, "Metadata nesting depth exceeded.");
                }
                AtomicReference<String> subMetadataName = new AtomicReference<>("");
                if(decodeName(subMetadataName).isError(chain)) return chain.get();
                Metadata subMetadata = new Metadata();
                metadata = subMetadata;
                if(mp.parentMetadata.addSubMetadata(subMetadataName.get(), subMetadata).isError(chain)) return chain.get();
            }
            if(metadata == null) {
                return new Status(Status.Code.DRACO_ERROR, "Metadata is null.");
            }

            AtomicReference<Long> numEntries = new AtomicReference<>(0L);
            if(BitUtils.decodeVarint(DataType.UINT32, numEntries, buffer).isError(chain)) return chain.get();
            for(int i = 0; i < numEntries.get(); ++i) {
                if(decodeEntry(metadata).isError(chain)) return chain.get();
            }
            AtomicReference<Long> numSubMetadata = new AtomicReference<>(0L);
            if(BitUtils.decodeVarint(DataType.UINT32, numSubMetadata, buffer).isError(chain)) return chain.get();
            if(numSubMetadata.get() > buffer.getRemainingSize()) {
                return new Status(Status.Code.IO_ERROR, "The decoded number of metadata items is unreasonably high.");
            }
            for(int i = 0; i < numSubMetadata.get(); ++i) {
                metadataStack.pushBack(new MetadataTuple(
                        metadata, null, mp.parentMetadata != null ? mp.level + 1 : mp.level));
            }
        }
        return Status.OK;
    }

    private Status decodeEntry(Metadata metadata) {
        StatusChain chain = Status.newChain();

        AtomicReference<String> entryName = new AtomicReference<>("");
        if(decodeName(entryName).isError(chain)) return chain.get();
        AtomicReference<Long> dataSize = new AtomicReference<>(0L);
        if(BitUtils.decodeVarint(DataType.UINT32, dataSize, buffer).isError(chain)) return chain.get();
        if(dataSize.get() == 0) {
            return new Status(Status.Code.IO_ERROR, "Data size is zero.");
        }
        if(dataSize.get() > buffer.getRemainingSize()) {
            return new Status(Status.Code.IO_ERROR, "Data size exceeds buffer size.");
        }
        AtomicReference<byte[]> entryValue = new AtomicReference<>(new byte[0]);
        if(buffer.decode(DataType.bytes((int) (long) dataSize.get()), entryValue).isError(chain)) return chain.get();
        metadata.addEntryBinary(entryName.get(), entryValue.get());
        return Status.OK;
    }

    private Status decodeName(AtomicReference<String> name) {
        StatusChain chain = Status.newChain();

        AtomicReference<Short> nameLen = new AtomicReference<>((short) 0);
        if(buffer.decode(DataType.UINT8, nameLen).isError(chain)) return chain.get();
        if(nameLen.get() == 0) {
            return Status.OK;
        }
        if(buffer.decode(DataType.string(nameLen.get()), name).isError(chain)) return chain.get();
        return Status.OK;
    }

}
