package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.number.UShort;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.compression.config.EncodedGeometryType;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.compression.mesh.MeshDecoder;
import com.mndk.bteterrarenderer.draco.compression.pointcloud.PointCloudDecoder;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PSchemeDecoderFactory {

    public <DataT, CorrT> PSchemeDecoder<DataT, CorrT> createPredictionSchemeForDecoder(
            PredictionSchemeMethod method, int attId, PointCloudDecoder decoder,
            PSchemeDecodingTransform<DataT, CorrT> transform)
    {
        if(method == PredictionSchemeMethod.NONE) {
            return null;
        }
        PointAttribute att = decoder.getPointCloud().getAttribute(attId);
        if(decoder.getGeometryType() == EncodedGeometryType.TRIANGULAR_MESH) {
            // Cast the decoder to mesh decoder.
            MeshDecoder meshDecoder = (MeshDecoder) decoder;
            PSchemeDecoder<DataT, CorrT> ret = PSchemeFactory.createMeshPredictionScheme(
                    new MPSchemeDecoderFactory<>(), meshDecoder, method, attId, transform,
                    UShort.of(decoder.getBitstreamVersion()));
            if(ret != null) return ret;
            // Otherwise try to create another prediction scheme.
        }
        return new PSchemeDeltaDecoder<>(att, transform);
    }

}
