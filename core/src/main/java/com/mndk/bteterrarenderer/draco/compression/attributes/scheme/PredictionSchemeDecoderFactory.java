/*
// Creates a prediction scheme for a given decoder and given prediction method.
// The prediction schemes are automatically initialized with decoder specific
// data if needed.
template <typename DataTypeT, class TransformT>
std::unique_ptr<PredictionSchemeDecoder<DataTypeT, TransformT>>
CreatePredictionSchemeForDecoder(PredictionSchemeMethod method, int att_id,
                                 const PointCloudDecoder *decoder,
                                 const TransformT &transform) {
  if (method == PREDICTION_NONE) {
    return nullptr;
  }
  const PointAttribute *const att = decoder->point_cloud()->attribute(att_id);
  if (decoder->GetGeometryType() == TRIANGULAR_MESH) {
    // Cast the decoder to mesh decoder. This is not necessarily safe if there
    // is some other decoder decides to use TRIANGULAR_MESH as the return type,
    // but unfortunately there is not nice work around for this without using
    // RTTI (double dispatch and similar concepts will not work because of the
    // template nature of the prediction schemes).
    const MeshDecoder *const mesh_decoder =
        static_cast<const MeshDecoder *>(decoder);

    auto ret = CreateMeshPredictionScheme<
        MeshDecoder, PredictionSchemeDecoder<DataTypeT, TransformT>,
        MeshPredictionSchemeDecoderFactory<DataTypeT>>(
        mesh_decoder, method, att_id, transform, decoder->bitstream_version());
    if (ret) {
      return ret;
    }
    // Otherwise try to create another prediction scheme.
  }
  // Create delta decoder.
  return std::unique_ptr<PredictionSchemeDecoder<DataTypeT, TransformT>>(
      new PredictionSchemeDeltaDecoder<DataTypeT, TransformT>(att, transform));
}

// Create a prediction scheme using a default transform constructor.
template <typename DataTypeT, class TransformT>
std::unique_ptr<PredictionSchemeDecoder<DataTypeT, TransformT>>
CreatePredictionSchemeForDecoder(PredictionSchemeMethod method, int att_id,
                                 const PointCloudDecoder *decoder) {
  return CreatePredictionSchemeForDecoder<DataTypeT, TransformT>(
      method, att_id, decoder, TransformT());
}

}  // namespace draco

#endif  // DRACO_COMPRESSION_ATTRIBUTES_PREDICTION_SCHEMES_PREDICTION_SCHEME_DECODER_FACTORY_H_

 */

package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.number.UShort;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.compression.config.EncodedGeometryType;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.compression.mesh.MeshDecoder;
import com.mndk.bteterrarenderer.draco.compression.pointcloud.PointCloudDecoder;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PredictionSchemeDecoderFactory {

    public <DataT, CorrT> PredictionSchemeDecoder<DataT, CorrT> createPredictionSchemeForDecoder(
            PredictionSchemeMethod method, int attId, PointCloudDecoder decoder,
            PredictionSchemeDecodingTransform<DataT, CorrT> transform)
    {
        if(method == PredictionSchemeMethod.PREDICTION_NONE) {
            return null;
        }
        PointAttribute att = decoder.getPointCloud().getAttribute(attId);
        if(decoder.getGeometryType() == EncodedGeometryType.TRIANGULAR_MESH) {
            // Cast the decoder to mesh decoder. This is not necessarily safe if there
            // is some other decoder decides to use TRIANGULAR_MESH as the return type,
            // but unfortunately there is not nice work around for this without using
            // RTTI (double dispatch and similar concepts will not work because of the
            // template nature of the prediction schemes).
            MeshDecoder meshDecoder = (MeshDecoder) decoder;
            PredictionSchemeDecoder<DataT, CorrT> ret =
                    PredictionSchemeFactory.createMeshPredictionScheme(
                            meshDecoder, method, attId, transform, UShort.of(decoder.getBitstreamVersion()));
            if(ret != null) return ret;
            // Otherwise try to create another prediction scheme.
        }
        return new PredictionSchemeDeltaDecoder<>(att, transform);
    }

}
