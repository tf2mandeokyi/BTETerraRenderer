package com.mndk.bteterrarenderer.draco.mesh;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.VertexIndex;
import com.mndk.bteterrarenderer.draco.core.IndexTypeVector;

/**
 * ValenceCache provides support for the caching of valences off of some kind of
 * CornerTable 'type' of class.
 * No valences should be queried before Caching is
 * performed and values should be removed/recached when changes to the
 * underlying mesh are taking place.
 */
public class ValenceCache<T extends ICornerTable> {

    private final T table;
    private final IndexTypeVector<VertexIndex, Byte> vertexValenceCache8Bit =
            new IndexTypeVector<>(DataType.int8());
    private final IndexTypeVector<VertexIndex, Integer> vertexValenceCache32Bit =
            new IndexTypeVector<>(DataType.int32());

    public ValenceCache(T table) {
        this.table = table;
    }

    public byte valenceFromCacheInaccurate(CornerIndex c) {
        if(c.equals(CornerIndex.INVALID)) {
            return -1;
        }
        return valenceFromCacheInaccurate(table.getVertex(c));
    }
    public int valenceFromCache(CornerIndex c) {
        if(c.equals(CornerIndex.INVALID)) {
            return -1;
        }
        return valenceFromCache(table.getVertex(c));
    }

    public int confidentValenceFromCache(VertexIndex v) {
        if(v.getValue() >= table.getNumVertices()) {
            throw new IllegalArgumentException("Vertex index out of bounds");
        }
        if(vertexValenceCache32Bit.size() != table.getNumVertices()) {
            throw new IllegalStateException("Cache not filled");
        }
        return vertexValenceCache32Bit.get(v);
    }

    public void cacheValencesInaccurate() {
        if (!vertexValenceCache8Bit.isEmpty()) return;
        vertexValenceCache8Bit.resize(table.getNumVertices());
        for(VertexIndex v : VertexIndex.range(0, table.getNumVertices())) {
            vertexValenceCache8Bit.set(v, (byte) Math.min(Byte.MAX_VALUE, table.getValence(v)));
        }
    }
    public void cacheValences() {
        if (!vertexValenceCache32Bit.isEmpty()) return;
        vertexValenceCache32Bit.resize(table.getNumVertices());
        for(VertexIndex v : VertexIndex.range(0, table.getNumVertices())) {
            vertexValenceCache32Bit.set(v, table.getValence(v));
        }
    }

    public byte confidentValenceFromCacheInaccurate(CornerIndex c) {
        if(c.getValue() < 0) {
            throw new IllegalArgumentException("Corner index out of bounds");
        }
        return confidentValenceFromCacheInaccurate(table.getConfidentVertex(c));
    }
    public int confidentValenceFromCache(CornerIndex c) {
        if(c.getValue() < 0) {
            throw new IllegalArgumentException("Corner index out of bounds");
        }
        return confidentValenceFromCache(table.getConfidentVertex(c));
    }
    public byte valenceFromCacheInaccurate(VertexIndex v) {
        if(v.equals(VertexIndex.INVALID) || v.getValue() >= table.getNumVertices()) {
            return -1;
        }
        return confidentValenceFromCacheInaccurate(v);
    }
    public byte confidentValenceFromCacheInaccurate(VertexIndex v) {
        if(v.getValue() >= table.getNumVertices()) {
            throw new IllegalArgumentException("Vertex index out of bounds");
        }
        if(vertexValenceCache8Bit.size() != table.getNumVertices()) {
            throw new IllegalStateException("Cache not filled");
        }
        return vertexValenceCache8Bit.get(v);
    }

    public int valenceFromCache(VertexIndex v) {
        if(v.equals(VertexIndex.INVALID) || v.getValue() >= table.getNumVertices()) {
            return -1;
        }
        return confidentValenceFromCache(v);
    }

    public void clearValenceCacheInaccurate() {
        vertexValenceCache8Bit.clear();
    }

    public void clearValenceCache() {
        vertexValenceCache32Bit.clear();
    }

    public boolean isCacheEmpty() {
        return vertexValenceCache8Bit.isEmpty() && vertexValenceCache32Bit.isEmpty();
    }
}
