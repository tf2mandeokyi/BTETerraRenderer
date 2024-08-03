package com.mndk.bteterrarenderer.draco.pointcloud;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.AttributeValueIndex;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.core.BoundingBox;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.VectorD;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.core.IndexTypeVector;
import com.mndk.bteterrarenderer.draco.metadata.AttributeMetadata;
import com.mndk.bteterrarenderer.draco.metadata.GeometryMetadata;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.*;

/**
 * PointCloud is a collection of n-dimensional points that are described by a
 * set of PointAttributes that can represent data such as positions or colors
 * of individual points (see {@link PointAttribute})
 */
public class PointCloud {

    @Getter
    private GeometryMetadata metadata = null;
    private final List<PointAttribute> attributes = new ArrayList<>();
    private final List<CppVector<Integer>> namedAttributeIndex = new ArrayList<CppVector<Integer>>() {{
        for(int i = 0; i < GeometryAttribute.NAMED_ATTRIBUTES_COUNT; ++i) {
            add(new CppVector<>(DataType.int32()));
        }
    }};
    @Getter @Setter
    private int numPoints = 0;

    public PointCloud() {}

    /** Returns the number of named attributes of a given type */
    public int numNamedAttributes(GeometryAttribute.Type type) {
        if(type == GeometryAttribute.Type.INVALID || type == null) {
            return 0;
        }
        return namedAttributeIndex.get(type.getIndex()).size();
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
    public PointAttribute getNamedAttributeByUniqueId(GeometryAttribute.Type type, UInt uniqueId) {
        for(int attId : namedAttributeIndex.get(type.getIndex())) {
            if(attributes.get(attId).getUniqueId().equals(uniqueId)) {
                return attributes.get(attId);
            }
        }
        return null;
    }

    /** Returns the attribute of a given unique id. */
    public PointAttribute getAttributeByUniqueId(UInt uniqueId) {
        int attId = getAttributeIdByUniqueId(uniqueId);
        if(attId == -1) {
            return null;
        }
        return attributes.get(attId);
    }
    public int getAttributeIdByUniqueId(UInt uniqueId) {
        for(int attId = 0; attId < attributes.size(); ++attId) {
            if(attributes.get(attId).getUniqueId().equals(uniqueId)) {
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
            namedAttributeIndex.get(pa.getAttributeType().getIndex()).pushBack(attId);
        }
        pa.setUniqueId(UInt.of(attId));
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
        UInt uniqueId = attributes.get(attId).getUniqueId();
        attributes.remove(attId);
        // Remove metadata if applicable.
        if (metadata != null) {
            metadata.deleteAttributeMetadataByUniqueId(uniqueId);
        }

        // Remove the attribute from the named attribute list if applicable.
        if (attType.getIndex() < GeometryAttribute.NAMED_ATTRIBUTES_COUNT) {
            namedAttributeIndex.get(attType.getIndex()).erase(attId);
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

    public void deduplicatePointIds() {
        @RequiredArgsConstructor
        class PointIndexWrapper {
            final PointIndex pointIndex;

            @Override
            public String toString() {
                return pointIndex.toString();
            }

            @Override public int hashCode() {
                int hash = 0;
                for(int i = 0; i < getNumAttributes(); ++i) {
                    AttributeValueIndex attId = getAttribute(i).getMappedIndex(pointIndex);
                    hash = Objects.hash(attId.hashCode(), hash);
                }
                return hash;
            }

            @Override
            public boolean equals(Object o) {
                if(o == this) return true;
                if(!(o instanceof PointIndexWrapper)) return false;
                PointIndex index = ((PointIndexWrapper) o).pointIndex;
                for(int i = 0; i < getNumAttributes(); ++i) {
                    AttributeValueIndex attId0 = getAttribute(i).getMappedIndex(pointIndex);
                    AttributeValueIndex attId1 = getAttribute(i).getMappedIndex(index);
                    if(!attId0.equals(attId1)) return false;
                }
                return true;
            }
        }

        Map<PointIndexWrapper, PointIndex> uniquePointMap = new HashMap<>();
        int numUniquePoints = 0;
        IndexTypeVector<PointIndex, PointIndex> indexMap =
                new IndexTypeVector<>(PointIndex.type(), numPoints);
        CppVector<PointIndex> uniquePoints = new CppVector<>(PointIndex.type());

        // Go through all vertices and find their duplicates.
        for(PointIndex i : PointIndex.range(0, numPoints)) {
            PointIndexWrapper pointIndexWrapper = new PointIndexWrapper(i);
            if(uniquePointMap.containsKey(pointIndexWrapper)) {
                indexMap.set(i, uniquePointMap.get(pointIndexWrapper));
            } else {
                uniquePointMap.putIfAbsent(pointIndexWrapper, PointIndex.of(numUniquePoints));
                indexMap.set(i, PointIndex.of(numUniquePoints));
                uniquePoints.pushBack(i);
                ++numUniquePoints;
            }
        }
        if(numUniquePoints == numPoints) return;  // All vertices are already unique.

        this.applyPointIdDeduplication(indexMap, uniquePoints);
        this.numPoints = numUniquePoints;
    }

    protected void applyPointIdDeduplication(IndexTypeVector<PointIndex, PointIndex> idMap,
                                             CppVector<PointIndex> uniquePointIds) {
        int numUniquePoints = 0;
        for(PointIndex i : uniquePointIds) {
            PointIndex newPointId = idMap.get(i);
            if(newPointId.getValue() >= numUniquePoints) {
                // New unique vertex reached. Copy attribute indices to the proper position.
                for(int a = 0; a < getNumAttributes(); ++a) {
                    getAttribute(a).setPointMapEntry(newPointId, getAttribute(a).getMappedIndex(i));
                }
                numUniquePoints = newPointId.getValue() + 1;
            }
        }
        for(int a = 0; a < getNumAttributes(); ++a) {
            getAttribute(a).setExplicitMapping(numUniquePoints);
        }
    }

    public Status deduplicateAttributeValues() {
        // Go over all attributes and create mapping between duplicate entries.
        if(numPoints == 0) return Status.ok();  // Nothing to deduplicate.

        // Deduplicate all attributes.
        for(int attId = 0; attId < getNumAttributes(); ++attId) {
            if(this.getAttribute(attId).deduplicateValues(this.getAttribute(attId)) == 0) {
                return Status.dracoError("Failed to deduplicate attribute values");
            }
        }
        return Status.ok();
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
            Pointer<Float> p = Pointer.wrap(new float[3]);
            pcAtt.getValue(ai, p);
            VectorD.F3 point = new VectorD.F3(p.get(0), p.get(1), p.get(2));
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
        UInt attUniqueId = getAttribute(attId).getUniqueId();
        metadata.setAttUniqueId(attUniqueId);
        this.metadata.addAttributeMetadata(metadata);
    }

    public AttributeMetadata getAttributeMetadataByAttributeId(int attId) {
        if(metadata == null) return null;
        UInt uniqueId = getAttribute(attId).getUniqueId();
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
        return getAttributeIdByUniqueId(attMetadata.getAttUniqueId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(numPoints, attributes, namedAttributeIndex, metadata);
    }
}
