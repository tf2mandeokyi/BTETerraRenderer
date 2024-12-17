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

import com.mndk.bteterrarenderer.datatype.number.UInt;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for representing the metadata for a point cloud. It could have a list
 * of attribute metadeata.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GeometryMetadata extends Metadata {

    private final List<AttributeMetadata> attributeMetadatas = new ArrayList<>();

    public GeometryMetadata() {
        super();
    }

    public GeometryMetadata(GeometryMetadata geometryMetadata) {
        super();
        for (AttributeMetadata attMetadata : geometryMetadata.attributeMetadatas) {
            this.attributeMetadatas.add(new AttributeMetadata(attMetadata));
        }
    }

    public GeometryMetadata(Metadata metadata) {
        super(metadata);
    }

    public AttributeMetadata getAttributeMetadataByStringEntry(String entryName, String entryValue) {
        for (AttributeMetadata attMetadata : attributeMetadatas) {
            StringBuilder value = new StringBuilder();
            if (attMetadata.getEntryString(entryName, value).isError()) {
                continue;
            }
            if (value.toString().equals(entryValue)) return attMetadata;
        }
        // No attribute has the requested entry.
        return null;
    }

    public boolean addAttributeMetadata(AttributeMetadata attMetadata) {
        if (attMetadata == null) return false;
        attributeMetadatas.add(attMetadata);
        return true;
    }

    public void deleteAttributeMetadataByUniqueId(UInt attUniqueId) {
        if (attUniqueId.lt(0)) return;
        for (int i = 0; i < attributeMetadatas.size(); i++) {
            if (attributeMetadatas.get(i).getAttUniqueId().equals(attUniqueId)) {
                attributeMetadatas.remove(i);
                return;
            }
        }
    }

    public AttributeMetadata getAttributeMetadataByUniqueId(UInt attUniqueId) {
        if (attUniqueId.lt(0)) return null;
        for (AttributeMetadata attMetadata : attributeMetadatas) {
            if (attMetadata.getAttUniqueId().equals(attUniqueId)) {
                return attMetadata;
            }
        }
        return null;
    }
}
