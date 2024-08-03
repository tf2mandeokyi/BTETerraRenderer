package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.mesh.ICornerTable;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import lombok.Getter;

@Getter
public class MPSchemeData<T extends ICornerTable> {

    private Mesh mesh;
    private T cornerTable;

    /**
     * Mapping between vertices and their encoding order. I.e. when an attribute
     * entry on a given vertex was encoded.
     */
    private CppVector<Integer> vertexToDataMap;

    /**
     * Array that stores which corner was processed when a given attribute entry
     * was encoded or decoded.
     */
    private CppVector<CornerIndex> dataToCornerMap;

    public void set(Mesh mesh, T table, CppVector<CornerIndex> dataToCornerMap, CppVector<Integer> vertexToDataMap) {
        this.mesh = mesh;
        this.cornerTable = table;
        this.dataToCornerMap = dataToCornerMap;
        this.vertexToDataMap = vertexToDataMap;
    }

    public boolean isInitialized() {
        return mesh != null && cornerTable != null && vertexToDataMap != null && dataToCornerMap != null;
    }

}
