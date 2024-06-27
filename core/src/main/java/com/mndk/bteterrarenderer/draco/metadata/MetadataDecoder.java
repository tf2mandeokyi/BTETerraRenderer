package com.mndk.bteterrarenderer.draco.metadata;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.array.UByteArray;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.draco.core.*;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;
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
        AtomicReference<UInt> numAttMetadataRef = new AtomicReference<>();
        if(BitUtils.decodeVarint(DataType.uint32(), numAttMetadataRef, buffer).isError(chain)) return chain.get();
        int numAttMetadata = numAttMetadataRef.get().intValue();

        // Decode attribute metadata.
        for(int i = 0; i < numAttMetadata; ++i) {
            AtomicReference<UInt> attUniqueId = new AtomicReference<>();
            if(BitUtils.decodeVarint(DataType.uint32(), attUniqueId, buffer).isError(chain)) return chain.get();
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
        CppVector<MetadataTuple> metadataStack = CppVector.create();
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

            AtomicReference<UInt> numEntriesRef = new AtomicReference<>();
            if(BitUtils.decodeVarint(DataType.uint32(), numEntriesRef, buffer).isError(chain)) return chain.get();
            int numEntries = numEntriesRef.get().intValue();
            for(int i = 0; i < numEntries; ++i) {
                if(decodeEntry(metadata).isError(chain)) return chain.get();
            }
            AtomicReference<UInt> numSubMetadataRef = new AtomicReference<>();
            if(BitUtils.decodeVarint(DataType.uint32(), numSubMetadataRef, buffer).isError(chain)) return chain.get();
            int numSubMetadata = numSubMetadataRef.get().intValue();
            if(numSubMetadata > buffer.getRemainingSize()) {
                return new Status(Status.Code.IO_ERROR, "The decoded number of metadata items is unreasonably high.");
            }
            for(int i = 0; i < numSubMetadata; ++i) {
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
        AtomicReference<UInt> dataSizeRef = new AtomicReference<>();
        if(BitUtils.decodeVarint(DataType.uint32(), dataSizeRef, buffer).isError(chain)) return chain.get();
        int dataSize = dataSizeRef.get().intValue();
        if(dataSize == 0) {
            return new Status(Status.Code.IO_ERROR, "Data size is zero.");
        }
        if(dataSize > buffer.getRemainingSize()) {
            return new Status(Status.Code.IO_ERROR, "Data size exceeds buffer size.");
        }
        AtomicReference<UByteArray> entryValue = new AtomicReference<>();
        if(buffer.decode(DataType.bytes(dataSize), entryValue::set).isError(chain)) return chain.get();
        metadata.addEntryBinary(entryName.get(), entryValue.get());
        return Status.OK;
    }

    private Status decodeName(AtomicReference<String> name) {
        StatusChain chain = Status.newChain();

        AtomicReference<UByte> nameLenRef = new AtomicReference<>();
        if(buffer.decode(DataType.uint8(), nameLenRef::set).isError(chain)) return chain.get();
        int nameLen = nameLenRef.get().intValue();
        if(nameLen == 0) return Status.OK;

        if(buffer.decode(DataType.bytes(nameLen), val -> name.set(val.decode())).isError(chain)) return chain.get();
        return Status.OK;
    }

}
