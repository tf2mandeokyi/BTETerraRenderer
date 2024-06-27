package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.draco.core.DataBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.core.vector.IndexTypeVector;
import com.mndk.bteterrarenderer.datatype.DataIOManager;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Class for storing point specific data about each attribute. In general,
 * multiple points stored in a point cloud can share the same attribute value
 * and this class provides the necessary mapping between point ids and attribute
 * value ids.
 */
public class PointAttribute extends GeometryAttribute {

    /** Mapping between point ids and attribute value ids. */
    private IndexTypeVector<PointIndex, AttributeValueIndex> indicesMap =
            IndexTypeVector.create(PointIndex::of, AttributeValueIndex.arrayManager());
    private int numUniqueEntries;
    /** Flag when the mapping between point ids and attribute values is identity. */
    @Getter
    private boolean identityMapping;

    /**
     * If an attribute contains transformed data (e.g. quantized), we can specify
     * the attribute transform here and use it to transform the attribute back to
     * its original format.
     */
    @Getter @Setter
    private AttributeTransformData attributeTransformData;

    public PointAttribute() {
        this.numUniqueEntries = 0;
        this.identityMapping = false;
    }

    public PointAttribute(GeometryAttribute att) {
        super(att);
        this.numUniqueEntries = 0;
        this.identityMapping = false;
    }

    /**
     * Initializes a point attribute. By default, the attribute will be set to
     * identity mapping between point indices and attribute values. To set custom
     * mapping use {@link PointAttribute#setExplicitMapping} function.
     */
    public final void init(Type attributeType, byte numComponents, DataNumberType<?, ?> dataType,
                           boolean normalized, int numAttributeValues)
    {
        this.buffer = new DataBuffer();
        super.init(attributeType, this.buffer,
                numComponents, dataType, normalized,
                dataType.size() * numComponents, 0);
        this.reset(numAttributeValues);
        this.setIdentityMapping();
    }

    /** Copies attribute data from the provided {@code srcAtt} attribute. */
    public final Status copyFrom(PointAttribute srcAtt) {
        StatusChain chain = Status.newChain();

        if(this.getBuffer() == null) {
            this.resetBuffer(new DataBuffer(), 0, 0);
        }
        if(super.copyFrom(srcAtt).isError(chain)) return chain.get();
        identityMapping = srcAtt.identityMapping;
        numUniqueEntries = srcAtt.numUniqueEntries;
        indicesMap = srcAtt.indicesMap;
        if(srcAtt.attributeTransformData != null) {
            attributeTransformData = new AttributeTransformData(srcAtt.attributeTransformData);
        }
        else {
            attributeTransformData = null;
        }

        return Status.OK;
    }

    /**
     * Prepares the attribute storage for the specified number of entries.
     */
    public final Status reset(int numAttributeValues) {
        StatusChain chain = Status.newChain();

        if(buffer == null) {
            buffer = new DataBuffer();
        }
        long entrySize = this.getDataType().size() * this.getNumComponents();
        if(buffer.update(null, numAttributeValues * entrySize).isError(chain)) return chain.get();
        // Assign the new buffer to the parent attribute.
        this.resetBuffer(buffer, entrySize, 0);
        numUniqueEntries = numAttributeValues;
        return Status.OK;
    }

    public final int size() {
        return numUniqueEntries;
    }
    public final AttributeValueIndex getMappedIndex(PointIndex pointIndex) {
        if(identityMapping) {
            return AttributeValueIndex.of(pointIndex.getValue());
        }
        return indicesMap.get(pointIndex);
    }
    public final int indicesMapSize() {
        return indicesMap.size();
    }

    public final long getBytePosOfMappedIndex(PointIndex pointIndex) {
        return getBytePos(indicesMap.get(pointIndex));
    }

    /**
     * Sets the new number of unique attribute entries for the attribute. The
     * function resizes the attribute storage to hold {@code numAttributeValues}
     * entries.<br>
     * All previous entries with {@link AttributeValueIndex} less than {@code numAttributeValues}
     * are preserved. Caller needs to ensure that the {@link PointAttribute} is still
     * valid after the resizing operation (that is, each point is mapped to a
     * valid attribute value).
     */
    public final void resize(int newNumUniqueEntries) {
        numUniqueEntries = newNumUniqueEntries;
        buffer.resize(newNumUniqueEntries * this.getByteStride());
    }

    /**
     * Functions for setting the type of mapping between point indices and
     * attribute entry ids.<br>
     * This function sets the mapping to implicit, where point indices are equal
     * to attribute entry indices.
     */
    public final void setIdentityMapping() {
        identityMapping = true;
        indicesMap.clear();
    }

    /**
     * This function sets the mapping to be explicitly using the {@code indicesMap}
     * array that needs to be initialized by the caller.
     */
    public final void setExplicitMapping(int numPoints) {
        identityMapping = false;
        indicesMap.resize(numPoints, AttributeValueIndex.INVALID);
    }

    /** Set an explicit map entry for a specific point index */
    public final void setPointMapEntry(PointIndex pointIndex, AttributeValueIndex entryIndex) {
        if(identityMapping) throw new RuntimeException("Not explicit mapping");
        indicesMap.set(pointIndex, entryIndex);
    }

    /**
     * Same as {@link GeometryAttribute#getValue}, but using point id as the input.
     * Mapping to attribute value index is performed automatically.
     */
    public final <T> T getMappedValue(PointIndex pointIndex, DataIOManager<T> outType) {
        return this.getValue(this.getMappedIndex(pointIndex), outType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), identityMapping, numUniqueEntries, indicesMap, buffer);
    }
}
