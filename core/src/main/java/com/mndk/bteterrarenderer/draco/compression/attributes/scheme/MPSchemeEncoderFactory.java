package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.number.UShort;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;

public class MPSchemeEncoderFactory<DataT, CorrT>
        implements MPSchemeFactory<PSchemeEncoder<DataT, CorrT>, PSchemeEncodingTransform<DataT, CorrT>> {

    @Override
    public PSchemeEncoder<DataT, CorrT> create(
            PredictionSchemeMethod method, PointAttribute attribute,
            PSchemeEncodingTransform<DataT, CorrT> transform, MPSchemeData<?> meshData,
            UShort bitstreamVersion)
    {
        switch(method) {
            case MESH_PARALLELOGRAM:
                return new MPSchemeParallelogramEncoder<>(attribute, transform, meshData);
            case MESH_MULTI_PARALLELOGRAM:
                return new MPSchemeMultiParallelogramEncoder<>(attribute, transform, meshData);
            case MESH_CONSTRAINED_MULTI_PARALLELOGRAM:
                return new MPSchemeConstrainedMultiParallelogramEncoder<>(attribute, transform, meshData);
            case MESH_TEX_COORDS_PORTABLE:
                return new MPSchemeTexCoordsPortableEncoder<>(attribute, transform, meshData);
            case MESH_GEOMETRIC_NORMAL:
                return new MPSchemeGeometricNormalEncoder<>(attribute, transform, meshData);
            default:
                return null;
        }
    }
}
