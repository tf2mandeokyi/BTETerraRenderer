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
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class MetadataEncoder {

    public Status encodeMetadata(EncoderBuffer outBuffer, Metadata metadata) {
        StatusChain chain = new StatusChain();

        Map<String, Metadata.EntryValue> entries = metadata.getEntries();
        // Encode number of entries.
        outBuffer.encodeVarint(UInt.of(metadata.getNumEntries()));
        // Encode all entries.
        for(Map.Entry<String, Metadata.EntryValue> entry : entries.entrySet()) {
            if(encodeString(outBuffer, entry.getKey()).isError(chain)) return chain.get();
            CppVector<UByte> entryValue = entry.getValue().getBuffer();
            outBuffer.encodeVarint(UInt.of(entryValue.size()));
            outBuffer.encode(entryValue.getRawPointer(), entryValue.size());
        }

        Map<String, Metadata> subMetadatas = metadata.getSubMetadatas();
        // Encode number of sub-metadata
        outBuffer.encodeVarint(UInt.of(subMetadatas.size()));
        // Encode each sub-metadata
        for(Map.Entry<String, Metadata> subMetadataEntry : subMetadatas.entrySet()) {
            if(encodeString(outBuffer, subMetadataEntry.getKey()).isError(chain)) return chain.get();
            if(encodeMetadata(outBuffer, subMetadataEntry.getValue()).isError(chain)) return chain.get();
        }

        return Status.ok();
    }

    public Status encodeAttributeMetadata(EncoderBuffer outBuffer, AttributeMetadata metadata) {
        StatusChain chain = new StatusChain();

        if(metadata == null) {
            return Status.invalidParameter("metadata is null");
        }
        // Encode attribute id.
        if(outBuffer.encodeVarint(metadata.getAttUniqueId()).isError(chain)) return chain.get();
        if(encodeMetadata(outBuffer, metadata).isError(chain)) return chain.get();
        return Status.ok();
    }

    public Status encodeGeometryMetadata(EncoderBuffer outBuffer, GeometryMetadata metadata) {
        StatusChain chain = new StatusChain();

        if(metadata == null) {
            return Status.invalidParameter("metadata is null");
        }
        // Encode number of attribute metadata.
        outBuffer.encodeVarint(UInt.of(metadata.getAttributeMetadatas().size()));
        // Encode each attribute metadata
        for(AttributeMetadata attMetadata : metadata.getAttributeMetadatas()) {
            if(encodeAttributeMetadata(outBuffer, attMetadata).isError(chain)) return chain.get();
        }
        // Encode normal metadata part.
        if(encodeMetadata(outBuffer, metadata).isError(chain)) return chain.get();
        return Status.ok();
    }

    public Status encodeString(EncoderBuffer outBuffer, String str) {
        // We only support string of maximum length 255 which is using one byte to
        // encode the length.
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        if(bytes.length > 255) {
            return Status.invalidParameter("string is longer than 255 bytes");
        }
        if(bytes.length == 0) {
            outBuffer.encode(UByte.ZERO);
        } else {
            outBuffer.encode(UByte.of(str.length()));
            outBuffer.encode(Pointer.wrap(bytes).asRaw(), bytes.length);
        }
        return Status.ok();
    }

}
