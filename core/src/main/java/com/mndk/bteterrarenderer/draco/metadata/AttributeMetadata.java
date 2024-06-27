package com.mndk.bteterrarenderer.draco.metadata;

import com.mndk.bteterrarenderer.datatype.number.UInt;
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

    private UInt attUniqueId;

    public AttributeMetadata() {
        super();
        this.attUniqueId = UInt.ZERO;
    }

    public AttributeMetadata(Metadata metadata) {
        super(metadata);
        this.attUniqueId = UInt.ZERO;
    }

    public AttributeMetadata(AttributeMetadata metadata) {
        super(metadata);
        this.attUniqueId = metadata.attUniqueId;
    }

}
