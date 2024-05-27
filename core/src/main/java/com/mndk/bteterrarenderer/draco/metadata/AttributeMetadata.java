package com.mndk.bteterrarenderer.draco.metadata;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Class for representing specifically metadata of attributes. It must have an
 * attribute id which should be identical to its counterpart attribute in
 * the point cloud it belongs to.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AttributeMetadata extends Metadata {

    private int attUniqueId;

    public AttributeMetadata() {
        super();
        this.attUniqueId = 0;
    }

    public AttributeMetadata(Metadata metadata) {
        super(metadata);
        this.attUniqueId = 0;
    }

    public AttributeMetadata(AttributeMetadata metadata) {
        super(metadata);
        this.attUniqueId = metadata.attUniqueId;
    }

}
