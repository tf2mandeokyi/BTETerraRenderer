package com.mndk.bteterrarenderer.draco.compression.attributes;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/** Data used for encoding and decoding of mesh attributes. */
@Getter
@Setter
@RequiredArgsConstructor
public class MeshAttributeIndicesEncodingData {

    /**
     * Array for storing the corner ids in the order their associated attribute
     * entries were encoded/decoded. For every encoded attribute value entry we
     * store exactly one corner. I.e., this is the mapping between an encoded
     * attribute entry ids and corner ids. This map is needed for example by
     * prediction schemes. Note that not all corners are included in this map,
     * e.g., if multiple corners share the same attribute value, only one of these
     * corners will usually be included.
     */
    private final CppVector<CornerIndex> encodedAttributeValueIndexToCornerMap =
            new CppVector<>(CornerIndex.type());

    /**
     * Map for storing encoding order of attribute entries for each vertex.
     * i.e. Mapping between vertices and their corresponding attribute entry ids
     * that are going to be used by the decoder.
     * -1 if an attribute entry hasn't been encoded/decoded yet.
     */
    private final CppVector<Integer> vertexToEncodedAttributeValueIndexMap = new CppVector<>(DataType.int32());

    /**
     * Total number of encoded/decoded attribute entries.
     */
    private int numValues = 0;

    public void init(int numVertices) {
        vertexToEncodedAttributeValueIndexMap.resize(numVertices);
        encodedAttributeValueIndexToCornerMap.reserve(numVertices);
    }

}
