/*


// A specialized version of the prediction scheme interface for specific
// input and output data types.
// |entry_to_point_id_map| is the mapping between value entries to point ids
// of the associated point cloud, where one entry is defined as |num_components|
// values of the |in_data|.
// DataTypeT is the data type of input and predicted values.
// CorrTypeT is the data type used for storing corrected values.
template <typename DataTypeT, typename CorrTypeT = DataTypeT>
class PredictionSchemeTypedDecoderInterface
    : public PredictionSchemeDecoderInterface {
 public:
  // Reverts changes made by the prediction scheme during encoding.
  virtual bool ComputeOriginalValues(
      const CorrTypeT *in_corr, DataTypeT *out_data, int size,
      int num_components, const PointIndex *entry_to_point_id_map) = 0;
};

}  // namespace draco

#endif  // DRACO_COMPRESSION_ATTRIBUTES_PREDICTION_SCHEMES_PREDICTION_SCHEME_DECODER_INTERFACE_H_
 */

package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;

public interface PredictionSchemeTypedDecoderInterface<DataT, CorrT> extends PredictionSchemeDecoderInterface {
    DataNumberType<DataT, ?> getDataType();
    DataNumberType<CorrT, ?> getCorrType();
    Status computeOriginalValues(CppVector<CorrT> inCorr, CppVector<DataT> outData,
                                 int size, int numComponents, CppVector<PointIndex> entryToPointIdMap);
}
