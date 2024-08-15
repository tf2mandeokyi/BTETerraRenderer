package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.number.UShort;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.compression.attributes.MeshAttributeIndicesEncodingData;
import com.mndk.bteterrarenderer.draco.compression.config.EncodedGeometryType;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.compression.mesh.MeshEncodingDataSource;
import com.mndk.bteterrarenderer.draco.mesh.CornerTable;
import com.mndk.bteterrarenderer.draco.mesh.MeshAttributeCornerTable;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PSchemeFactory {

    public <S, T> S createMeshPredictionScheme(
            MPSchemeFactory<S, T> factory,
            MeshEncodingDataSource source, PredictionSchemeMethod method,
            int attId, T transform, UShort bitstreamVersion)
    {
        PointAttribute att = source.getPointCloud().getAttribute(attId);
        if(source.getGeometryType() == EncodedGeometryType.TRIANGULAR_MESH && (
                method == PredictionSchemeMethod.MESH_PARALLELOGRAM ||
                method == PredictionSchemeMethod.MESH_MULTI_PARALLELOGRAM ||
                method == PredictionSchemeMethod.MESH_CONSTRAINED_MULTI_PARALLELOGRAM ||
                method == PredictionSchemeMethod.MESH_TEX_COORDS_PORTABLE ||
                method == PredictionSchemeMethod.MESH_GEOMETRIC_NORMAL ||
                method == PredictionSchemeMethod.MESH_TEX_COORDS_DEPRECATED)) {
            CornerTable ct = source.getCornerTable();
            MeshAttributeIndicesEncodingData encodingData = source.getAttributeEncodingData(attId);
            if(ct == null || encodingData == null) {
                // No connectivity data found.
                return null;
            }
            // Connectivity data exists.
            MeshAttributeCornerTable attCt = source.getAttributeCornerTable(attId);
            if(attCt != null) {
                MPSchemeData<MeshAttributeCornerTable> md = new MPSchemeData<>();
                md.set(source.getMesh(), attCt,
                        encodingData.getEncodedAttributeValueIndexToCornerMap(),
                        encodingData.getVertexToEncodedAttributeValueIndexMap());
                return factory.create(method, att, transform, md, bitstreamVersion);
            } else {
                MPSchemeData<CornerTable> md = new MPSchemeData<>();
                md.set(source.getMesh(), ct,
                        encodingData.getEncodedAttributeValueIndexToCornerMap(),
                        encodingData.getVertexToEncodedAttributeValueIndexMap());
                return factory.create(method, att, transform, md, bitstreamVersion);
            }
        }
        return null;
    }
}
