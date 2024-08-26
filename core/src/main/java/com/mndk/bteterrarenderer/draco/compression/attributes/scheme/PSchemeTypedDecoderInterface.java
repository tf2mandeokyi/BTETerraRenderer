package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.DataNumberType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.core.Status;

public interface PSchemeTypedDecoderInterface<DataT, CorrT> extends PSchemeDecoderInterface {
    DataNumberType<DataT> getDataType();
    DataNumberType<CorrT> getCorrType();
    Status computeOriginalValues(Pointer<CorrT> inCorr, Pointer<DataT> outData,
                                 int size, int numComponents, Pointer<PointIndex> entryToPointIdMap);
}
