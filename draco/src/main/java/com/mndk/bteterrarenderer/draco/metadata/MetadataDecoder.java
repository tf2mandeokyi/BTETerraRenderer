/*
 * Copyright (C) 2024 The Draco Authors (for providing the original C++ code)
 * Copyright (C) 2024 m4ndeokyi (for translating the code into Java)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mndk.bteterrarenderer.draco.metadata;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.PointerHelper;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MetadataDecoder {

    private DecoderBuffer buffer = null;

    public Status decodeMetadata(DecoderBuffer inBuffer, Metadata metadata) {
        if (metadata == null) {
            return Status.invalidParameter("Metadata is null.");
        }
        buffer = inBuffer;
        return decodeMetadata(metadata);
    }

    public Status decodeGeometryMetadata(DecoderBuffer inBuffer, GeometryMetadata metadata) {
        StatusChain chain = new StatusChain();
        
        if (metadata == null) {
            return Status.invalidParameter("Metadata is null.");
        }
        buffer = inBuffer;
        Pointer<UInt> numAttMetadataRef = Pointer.newUInt();
        if (buffer.decodeVarint(numAttMetadataRef).isError(chain)) return chain.get();
        int numAttMetadata = numAttMetadataRef.get().intValue();

        // Decode attribute metadata.
        for (int i = 0; i < numAttMetadata; ++i) {
            Pointer<UInt> attUniqueId = Pointer.newUInt();
            if (buffer.decodeVarint(attUniqueId).isError(chain)) return chain.get();
            AttributeMetadata attMetadata = new AttributeMetadata();
            attMetadata.setAttUniqueId(attUniqueId.get());
            if (decodeMetadata(attMetadata).isError(chain)) return chain.get();
            metadata.addAttributeMetadata(attMetadata);
        }
        return decodeMetadata(metadata);
    }

    private Status decodeMetadata(Metadata metadata) {
        StatusChain chain = new StatusChain();

        // Limit metadata nesting depth to avoid stack overflow in destructor.
        final int kMaxSubmetadataLevel = 1000;

        @AllArgsConstructor
        class MetadataTuple {
            Metadata parentMetadata, decodedMetadata;
            int level;
        }
        List<MetadataTuple> metadataStack = new ArrayList<>();
        metadataStack.add(new MetadataTuple(null, metadata, 0));
        while (!metadataStack.isEmpty()) {
            MetadataTuple mp = metadataStack.remove(metadataStack.size() - 1);
            metadata = mp.decodedMetadata;

            if (mp.parentMetadata != null) {
                if (mp.level > kMaxSubmetadataLevel) {
                    return Status.dracoError("Metadata nesting depth exceeded.");
                }
                AtomicReference<String> subMetadataName = new AtomicReference<>("");
                if (decodeName(subMetadataName).isError(chain)) return chain.get();
                Metadata subMetadata = new Metadata();
                metadata = subMetadata;
                if (mp.parentMetadata.addSubMetadata(subMetadataName.get(), subMetadata).isError(chain)) return chain.get();
            }
            if (metadata == null) {
                return Status.dracoError("Metadata is null.");
            }

            Pointer<UInt> numEntriesRef = Pointer.newUInt();
            if (buffer.decodeVarint(numEntriesRef).isError(chain)) return chain.get();
            int numEntries = numEntriesRef.get().intValue();
            for (int i = 0; i < numEntries; ++i) {
                if (decodeEntry(metadata).isError(chain)) return chain.get();
            }
            Pointer<UInt> numSubMetadataRef = Pointer.newUInt();
            if (buffer.decodeVarint(numSubMetadataRef).isError(chain)) return chain.get();
            int numSubMetadata = numSubMetadataRef.get().intValue();
            if (numSubMetadata > buffer.getRemainingSize()) {
                return Status.ioError("The decoded number of metadata items is unreasonably high.");
            }
            for (int i = 0; i < numSubMetadata; ++i) {
                metadataStack.add(new MetadataTuple(
                        metadata, null, mp.parentMetadata != null ? mp.level + 1 : mp.level));
            }
        }
        return Status.ok();
    }

    private Status decodeEntry(Metadata metadata) {
        StatusChain chain = new StatusChain();

        AtomicReference<String> entryName = new AtomicReference<>("");
        if (decodeName(entryName).isError(chain)) return chain.get();
        Pointer<UInt> dataSizeRef = Pointer.newUInt();
        if (buffer.decodeVarint(dataSizeRef).isError(chain)) return chain.get();
        int dataSize = dataSizeRef.get().intValue();
        if (dataSize == 0) {
            return Status.ioError("Data size is zero.");
        }
        if (dataSize > buffer.getRemainingSize()) {
            return Status.ioError("Data size exceeds buffer size.");
        }
        RawPointer entryValue = RawPointer.newArray(dataSize);
        if (buffer.decode(entryValue, dataSize).isError(chain)) return chain.get();
        metadata.addEntryBinary(entryName.get(), entryValue, dataSize);
        return Status.ok();
    }

    private Status decodeName(AtomicReference<String> name) {
        StatusChain chain = new StatusChain();

        Pointer<UByte> nameLenRef = Pointer.newUByte();
        if (buffer.decode(nameLenRef).isError(chain)) return chain.get();
        int nameLen = nameLenRef.get().intValue();
        if (nameLen == 0) return Status.ok();

        RawPointer nameBuffer = RawPointer.newArray(nameLen);
        if (buffer.decode(nameBuffer, nameLen).isError(chain)) return chain.get();
        name.set(PointerHelper.rawToString(nameBuffer, nameLen));
        return Status.ok();
    }

}
