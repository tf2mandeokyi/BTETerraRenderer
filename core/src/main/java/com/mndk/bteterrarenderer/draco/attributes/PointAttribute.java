package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.core.DataBuffer;
import com.mndk.bteterrarenderer.draco.core.DracoDataType;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.core.IndexTypeVector;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
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
            new IndexTypeVector<>(AttributeValueIndex.type());
    private int numUniqueEntries;
    /** Flag when the mapping between point ids and attribute values is identity. */
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
    public final void init(Type attributeType, byte numComponents, DracoDataType dataType,
                           boolean normalized, int numAttributeValues)
    {
        this.buffer = new DataBuffer();
        super.init(attributeType, this.buffer, UByte.of(numComponents), dataType, normalized,
                dataType.getDataTypeLength() * numComponents, 0);
        this.reset(numAttributeValues);
        this.setIdentityMapping();
    }

    /** Copies attribute data from the provided {@code srcAtt} attribute. */
    public final Status copyFrom(PointAttribute srcAtt) {
        StatusChain chain = new StatusChain();

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

        return Status.ok();
    }

    /**
     * Prepares the attribute storage for the specified number of entries.
     */
    public final Status reset(int numAttributeValues) {
        StatusChain chain = new StatusChain();

        if(buffer == null) {
            buffer = new DataBuffer();
        }
        long entrySize = this.getDataType().getDataTypeLength() * this.getNumComponents().intValue();
        if(buffer.update(null, numAttributeValues * entrySize).isError(chain)) return chain.get();
        // Assign the new buffer to the parent attribute.
        this.resetBuffer(buffer, entrySize, 0);
        numUniqueEntries = numAttributeValues;
        return Status.ok();
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

    public int deduplicateValues(GeometryAttribute inAtt) {
        return this.deduplicateValues(inAtt, AttributeValueIndex.of(0));
    }

    public int deduplicateValues(GeometryAttribute inAtt, AttributeValueIndex inAttOffset) {
        DataNumberType<?> dataType = inAtt.getDataType().getActualType();
        int numComponents = inAtt.getNumComponents().intValue();
        if(numComponents < 1 || numComponents > 4) return -1;

        int uniqueVals = this.deduplicateFormattedValues(dataType, numComponents, inAtt, inAttOffset);
        return uniqueVals == 0 ? -1 : uniqueVals;
    }

    private <T> int deduplicateFormattedValues(DataNumberType<T> dataType, int numComponents, GeometryAttribute inAtt,
                                               AttributeValueIndex inAttOffset) {
        @RequiredArgsConstructor
        class HashableValue {
            final Pointer<T> value;
            @Override public boolean equals(Object obj) {
                if(!(obj instanceof HashableValue)) return false;
                HashableValue other = (HashableValue) obj;
                return value.contentEquals(other.value, numComponents);
            }
            @Override public int hashCode() { return value.contentHashCode(numComponents); }
            @Override public String toString() { return "HashableValue{" + value + " -> hash=" + hashCode() + '}'; }
        }

        AttributeValueIndex uniqueVals = AttributeValueIndex.of(0);
        Map<HashableValue, AttributeValueIndex> valueToIndexMap = new HashMap<>();
        IndexTypeVector<AttributeValueIndex, AttributeValueIndex> valueMap =
                new IndexTypeVector<>(AttributeValueIndex.type(), numUniqueEntries);

        for(AttributeValueIndex i : AttributeValueIndex.range(0, numUniqueEntries)) {
            AttributeValueIndex attPos = i.add(inAttOffset);
            Pointer<T> attValue = inAtt.getValue(attPos, dataType, numComponents);
            HashableValue hashableValue = new HashableValue(attValue);

            boolean inserted = valueToIndexMap.putIfAbsent(hashableValue, uniqueVals) == null;

            // Try to update the hash map with a new entry pointing to the latest unique vertex index.
            if(!inserted) {
                // Duplicated value found. Update index mapping.
                valueMap.set(i, /*previousValue*/ /*uniqueVals*/ valueToIndexMap.get(hashableValue));
            } else {
                this.setAttributeValue(uniqueVals, attValue);
                valueMap.set(i, uniqueVals);
                uniqueVals = uniqueVals.add(1);
            }
        }
        if(uniqueVals.equals(numUniqueEntries)) {
            return uniqueVals.getValue();  // Nothing has changed.
        }
        if(this.isMappingIdentity()) {
            // Change identity mapping to the explicit one.
            // The number of points is equal to the number of old unique values.
            this.setExplicitMapping(numUniqueEntries);
            // Update the explicit map.
            for(PointIndex i : PointIndex.range(0, numUniqueEntries)) {
                this.setPointMapEntry(i, valueMap.get(AttributeValueIndex.of(i.getValue())));
            }
        } else {
            // Update point to value map using the mapping between old and new values.
            for(PointIndex i : PointIndex.range(0, indicesMapSize())) {
                this.setPointMapEntry(i, valueMap.get(indicesMap.get(i)));
            }
        }
        this.numUniqueEntries = uniqueVals.getValue();
        return this.numUniqueEntries;
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
    public final <T> void getMappedValue(PointIndex pointIndex, Pointer<T> outType) {
        this.getValue(this.getMappedIndex(pointIndex), outType);
    }

    public boolean isMappingIdentity() {
        return this.identityMapping;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), identityMapping, numUniqueEntries, indicesMap, buffer);
    }
}
