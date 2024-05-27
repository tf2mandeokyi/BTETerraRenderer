package com.mndk.bteterrarenderer.draco.metadata;

import com.mndk.bteterrarenderer.draco.core.DracoVector;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Class for representing the metadata for a point cloud. It could have a list
 * of attribute metadeata.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GeometryMetadata extends Metadata {

    private final DracoVector<AttributeMetadata> attMetadatas = new DracoVector<>();

    public GeometryMetadata() {
        super();
    }

    public GeometryMetadata(GeometryMetadata geometryMetadata) {
        super();
        for(AttributeMetadata attMetadata : geometryMetadata.attMetadatas) {
            this.attMetadatas.add(new AttributeMetadata(attMetadata));
        }
    }

    public GeometryMetadata(Metadata metadata) {
        super(metadata);
    }

    public AttributeMetadata getAttributeMetadataByStringEntry(String entryName, String entryValue) {
        for(AttributeMetadata attMetadata : attMetadatas) {
            String value = attMetadata.getEntryString(entryName);
            if(value == null) continue;
            if(value.equals(entryValue)) return attMetadata;
        }
        // No attribute has the requested entry.
        return null;
    }

    public boolean addAttributeMetadata(AttributeMetadata attMetadata) {
        if(attMetadata == null) return false;
        attMetadatas.add(attMetadata);
        return true;
    }

    public void deleteAttributeMetadataByUniqueId(int attUniqueId) {
        if(attUniqueId < 0) return;
        for(int i = 0; i < attMetadatas.size(); i++) {
            if(attMetadatas.get(i).getAttUniqueId() == attUniqueId) {
                attMetadatas.remove(i);
                return;
            }
        }
    }

    public AttributeMetadata getAttributeMetadataByUniqueId(int attUniqueId) {
        if(attUniqueId < 0) return null;
        for(AttributeMetadata attMetadata : attMetadatas) {
            if (attMetadata.getAttUniqueId() == attUniqueId) {
                return attMetadata;
            }
        }
        return null;
    }
}
