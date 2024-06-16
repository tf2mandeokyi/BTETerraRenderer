package com.mndk.bteterrarenderer.draco.pointcloud;

import com.mndk.bteterrarenderer.draco.attributes.AttributeValueIndex;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.core.BoundingBox;
import com.mndk.bteterrarenderer.draco.core.DataType;
import com.mndk.bteterrarenderer.draco.core.VectorD;
import com.mndk.bteterrarenderer.draco.metadata.AttributeMetadata;
import com.mndk.bteterrarenderer.draco.metadata.GeometryMetadata;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * PointCloud is a collection of n-dimensional points that are described by a
 * set of PointAttributes that can represent data such as positions or colors
 * of individual points (see {@link PointAttribute})
 */
public class PointCloud {

    @Getter
    private GeometryMetadata metadata = null;
    private final List<PointAttribute> attributes = new ArrayList<>();
    private final List<List<Integer>> namedAttributeIndex = new ArrayList<List<Integer>>() {{
        for(int i = 0; i < GeometryAttribute.NAMED_ATTRIBUTES_COUNT; ++i) {
            add(new ArrayList<>());
        }
    }};
    @Getter @Setter
    private int numPoints = 0;

    public PointCloud() {}

    /** Returns the number of named attributes of a given type */
    public int numNamedAttributes(GeometryAttribute.Type type) {
        if(type == GeometryAttribute.Type.INVALID || type.ordinal() >= GeometryAttribute.NAMED_ATTRIBUTES_COUNT) {
            return 0;
        }
        return namedAttributeIndex.get(type.ordinal()).size();
    }

    /**
     * Returns attribute id of the first named attribute with a given type or -1
     * when the attribute is not used by the point cloud.
     */
    public int getNamedAttributeId(GeometryAttribute.Type type) {
        return getNamedAttributeId(type, 0);
    }

    /** Returns the id of the i-th named attribute of a given type. */
    public int getNamedAttributeId(GeometryAttribute.Type type, int i) {
        if(numNamedAttributes(type) <= i) {
            return -1;
        }
        return namedAttributeIndex.get(type.getIndex()).get(i);
    }

    /**
     * Returns the first named attribute of a given type or null if the
     * attribute is not used by the point cloud.
     */
    public PointAttribute getNamedAttribute(GeometryAttribute.Type type) {
        return getNamedAttribute(type, 0);
    }

    /** Returns the i-th named attribute of a given type. */
    public PointAttribute getNamedAttribute(GeometryAttribute.Type type, int i) {
        int attId = getNamedAttributeId(type, i);
        if(attId == -1) {
            return null;
        }
        return attributes.get(attId);
    }

    /** Returns the named attribute of a given unique id. */
    public PointAttribute getNamedAttributeByUniqueId(GeometryAttribute.Type type, int uniqueId) {
        for(int attId : namedAttributeIndex.get(type.getIndex())) {
            if(attributes.get(attId).getUniqueId() == uniqueId) {
                return attributes.get(attId);
            }
        }
        return null;
    }

    /** Returns the attribute of a given unique id. */
    public PointAttribute getAttributeByUniqueId(int uniqueId) {
        int attId = getAttributeIdByUniqueId(uniqueId);
        if(attId == -1) {
            return null;
        }
        return attributes.get(attId);
    }
    public int getAttributeIdByUniqueId(int uniqueId) {
        for(int attId = 0; attId < attributes.size(); ++attId) {
            if(attributes.get(attId).getUniqueId() == uniqueId) {
                return attId;
            }
        }
        return -1;
    }

    public int getNumAttributes() {
        return attributes.size();
    }

    /**
     * Returned attribute can be modified, but it's caller's responsibility to
     * maintain the attribute's consistency with draco::PointCloud.
     */
    public PointAttribute getAttribute(int attId) {
        return attributes.get(attId);
    }

    /**
     * Adds a new attribute to the point cloud.
     * Returns the attribute id.
     */
    public int addAttribute(PointAttribute pa) {
        setAttribute(attributes.size(), pa);
        return attributes.size() - 1;
    }

    /**
     * Creates and adds a new attribute to the point cloud. The attribute has
     * properties derived from the provided GeometryAttribute {@code att}.
     * If {@code identityMapping} is set to true, the attribute will use identity
     * mapping between point indices and attribute value indices (i.e., each
     * point has a unique attribute value). If {@code identityMapping} is false, the
     * mapping between point indices and attribute value indices is set to
     * explicit, and it needs to be initialized manually using the
     * {@link PointAttribute#setPointMapEntry} method. {@code numAttributeValues} can be
     * used to specify the number of attribute values that are going to be
     * stored in the newly created attribute. Returns attribute id of the newly
     * created attribute or -1 in case of failure.
     */
    public int addAttribute(GeometryAttribute att, boolean identityMapping, int numAttributeValues) {
        PointAttribute pa = createAttribute(att, identityMapping, numAttributeValues);
        if (pa == null) {
            return -1;
        }
        return addAttribute(pa);
    }

    /**
     * Creates and returns a new attribute or null in case of failure. This
     * method is similar to {@link #addAttribute}, except that it returns the new
     * attribute instead of adding it to the point cloud.
     */
    public PointAttribute createAttribute(GeometryAttribute att, boolean identityMapping, int numAttributeValues) {
        if (att.getAttributeType() == GeometryAttribute.Type.INVALID) {
            return null;
        }
        PointAttribute pa = new PointAttribute(att);
        // Initialize point cloud specific attribute data.
        if (!identityMapping) {
            // First create mapping between indices.
            pa.setExplicitMapping(numPoints);
        } else {
            pa.setIdentityMapping();
            numAttributeValues = Math.max(numPoints, numAttributeValues);
        }
        if (numAttributeValues > 0) {
            pa.reset(numAttributeValues);
        }
        return pa;
    }

    /**
     * Assigns an attribute id to a given PointAttribute. If an attribute with
     * the same attribute id already exists, it is deleted.
     */
    public void setAttribute(int attId, PointAttribute pa) {
        if (attId >= attributes.size()) {
            attributes.add(pa);
        } else {
            attributes.set(attId, pa);
        }
        if (pa.getAttributeType().getIndex() < GeometryAttribute.NAMED_ATTRIBUTES_COUNT) {
            namedAttributeIndex.get(pa.getAttributeType().getIndex()).add(attId);
        }
        pa.setUniqueId(attId);
    }

    /**
     * Deletes an attribute with specified attribute id. Note that this changes
     * attribute ids of all subsequent attributes.
     */
    public void deleteAttribute(int attId) {
        if (attId < 0 || attId >= attributes.size()) {
            return;  // Attribute does not exist.
        }
        GeometryAttribute.Type attType = attributes.get(attId).getAttributeType();
        int uniqueId = attributes.get(attId).getUniqueId();
        attributes.remove(attId);
        // Remove metadata if applicable.
        if (metadata != null) {
            metadata.deleteAttributeMetadataByUniqueId(uniqueId);
        }

        // Remove the attribute from the named attribute list if applicable.
        if (attType.getIndex() < GeometryAttribute.NAMED_ATTRIBUTES_COUNT) {
            namedAttributeIndex.get(attType.getIndex()).remove((Integer) attId);
        }

        // Update ids of all subsequent named attributes (decrease them by one).
        for (int i = 0; i < GeometryAttribute.NAMED_ATTRIBUTES_COUNT; ++i) {
            for (int j = 0; j < namedAttributeIndex.get(i).size(); ++j) {
                if (namedAttributeIndex.get(i).get(j) > attId) {
                    namedAttributeIndex.get(i).set(j, namedAttributeIndex.get(i).get(j) - 1);
                }
            }
        }
    }

    /** Get bounding box. */
    public BoundingBox computeBoundingBox() {
        BoundingBox boundingBox = new BoundingBox();
        PointAttribute pcAtt = getNamedAttribute(GeometryAttribute.Type.POSITION);
        if(pcAtt == null) {
            return boundingBox;
        }
        for(int i = 0; i < pcAtt.size(); ++i) {
            AttributeValueIndex ai = AttributeValueIndex.of(i);
            float[] p = new float[3];
            pcAtt.getValue(ai, DataType.FLOAT32, p);
            VectorD.F3 point = new VectorD.F3(p[0], p[1], p[2]);
            boundingBox.update(point);
        }
        return boundingBox;
    }

    /** Add metadata. */
    public void addMetadata(GeometryMetadata metadata) {
        this.metadata = metadata;
    }

    /** Add metadata for an attribute. */
    public void addAttributeMetadata(int attId, AttributeMetadata metadata) {
        if(metadata == null) return;
        if(this.metadata == null) {
            this.metadata = new GeometryMetadata();
        }
        int attUniqueId = getAttribute(attId).getUniqueId();
        metadata.setAttUniqueId(attUniqueId);
        this.metadata.addAttributeMetadata(metadata);
    }

    public AttributeMetadata getAttributeMetadataByAttributeId(int attId) {
        if(metadata == null) return null;
        int uniqueId = getAttribute(attId).getUniqueId();
        return metadata.getAttributeMetadataByUniqueId(uniqueId);
    }

    /** Returns the attribute metadata that has the requested metadata entry. */
    public AttributeMetadata getAttributeMetadataByStringEntry(String name, String value) {
        if(metadata == null) return null;
        return metadata.getAttributeMetadataByStringEntry(name, value);
    }

    /** Returns the first attribute that has the requested metadata entry. */
    public int getAttributeIdByMetadataEntry(String name, String value) {
        if(metadata == null) return -1;
        AttributeMetadata attMetadata = metadata.getAttributeMetadataByStringEntry(name, value);
        if(attMetadata == null) return -1;
        return getAttributeIdByUniqueId((int) attMetadata.getAttUniqueId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(numPoints, attributes, namedAttributeIndex, metadata);
    }
}
