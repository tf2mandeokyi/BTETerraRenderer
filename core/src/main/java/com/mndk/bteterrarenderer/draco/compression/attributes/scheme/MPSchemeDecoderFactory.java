package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.number.UShort;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;

public class MPSchemeDecoderFactory<DataT, CorrT>
        implements MPSchemeFactory<PSchemeDecoder<DataT, CorrT>,
        PSchemeDecodingTransform<DataT, CorrT>> {

    @Override
    public PSchemeDecoder<DataT, CorrT> create(
            PredictionSchemeMethod method, PointAttribute attribute,
            PSchemeDecodingTransform<DataT, CorrT> transform, MPSchemeData<?> meshData,
            UShort bitstreamVersion)
    {
        switch (method) {
            case MESH_PARALLELOGRAM:
                return new MPSchemeParallelogramDecoder<>(attribute, transform, meshData);
            case MESH_MULTI_PARALLELOGRAM:
                return new MPSchemeMultiParallelogramDecoder<>(attribute, transform, meshData);
            case MESH_CONSTRAINED_MULTI_PARALLELOGRAM:
                return new MPSchemeConstrainedMultiParallelogramDecoder<>(attribute, transform, meshData);
            case MESH_TEX_COORDS_DEPRECATED:
                return new MPSchemeTexCoordsDecoder<>(attribute, transform, meshData, bitstreamVersion);
            case MESH_TEX_COORDS_PORTABLE:
                return new MPSchemeTexCoordsPortableDecoder<>(attribute, transform, meshData);
            case MESH_GEOMETRIC_NORMAL:
                return new MPSchemeGeometricNormalDecoder<>(attribute, transform, meshData);
            default:
                return null;
        }
    }
}
