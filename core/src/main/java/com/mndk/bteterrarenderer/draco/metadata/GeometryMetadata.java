package com.mndk.bteterrarenderer.draco.metadata;

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
        for(AttributeMetadata attMetadata : geometryMetadata.attributeMetadatas) {
            this.attributeMetadatas.add(new AttributeMetadata(attMetadata));
        }
    }

    public GeometryMetadata(Metadata metadata) {
        super(metadata);
    }

    public AttributeMetadata getAttributeMetadataByStringEntry(String entryName, String entryValue) {
        for(AttributeMetadata attMetadata : attributeMetadatas) {
            StringBuilder value = new StringBuilder();
            if(attMetadata.getEntryString(entryName, value).isError(null)) {
                continue;
            }
            if(value.toString().equals(entryValue)) return attMetadata;
        }
        // No attribute has the requested entry.
        return null;
    }

    public boolean addAttributeMetadata(AttributeMetadata attMetadata) {
        if(attMetadata == null) return false;
        attributeMetadatas.add(attMetadata);
        return true;
    }

    public void deleteAttributeMetadataByUniqueId(int attUniqueId) {
        if(attUniqueId < 0) return;
        for(int i = 0; i < attributeMetadatas.size(); i++) {
            if(attributeMetadatas.get(i).getAttUniqueId() == attUniqueId) {
                attributeMetadatas.remove(i);
                return;
            }
        }
    }

    public AttributeMetadata getAttributeMetadataByUniqueId(int attUniqueId) {
        if(attUniqueId < 0) return null;
        for(AttributeMetadata attMetadata : attributeMetadatas) {
            if (attMetadata.getAttUniqueId() == attUniqueId) {
                return attMetadata;
            }
        }
        return null;
    }
}
