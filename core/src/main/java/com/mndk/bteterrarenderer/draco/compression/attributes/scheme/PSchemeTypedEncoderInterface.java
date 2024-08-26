package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.DataNumberType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.core.Status;

public interface PSchemeTypedEncoderInterface<DataT, CorrT> extends PSchemeEncoderInterface {
    DataNumberType<DataT> getDataType();
    DataNumberType<CorrT> getCorrType();
    Status computeCorrectionValues(Pointer<DataT> inData, Pointer<CorrT> outCorr,
                                   int size, int numComponents, Pointer<PointIndex> entryToPointIdMap);
}
