package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.core.Status;

public class PSchemeDeltaDecoder<DataT, CorrT> extends PSchemeDecoder<DataT, CorrT> {

    public PSchemeDeltaDecoder(PointAttribute attribute,
                               PSchemeDecodingTransform<DataT, CorrT> transform) {
        super(attribute, transform);
    }

    @Override
    public Status computeOriginalValues(Pointer<CorrT> inCorr, Pointer<DataT> outData,
                                        int size, int numComponents, Pointer<PointIndex> entryToPointIdMap) {
        this.getTransform().init(numComponents);
        // Decode the original value for the first element.
        Pointer<DataT> zeroVals = this.getDataType().newArray(numComponents);
        this.getTransform().computeOriginalValue(zeroVals, inCorr, outData);

        // Decode data from the front using D(i) = D(i) + D(i - 1).
        for (int i = numComponents; i < size; i += numComponents) {
            this.getTransform().computeOriginalValue(outData.add(i - numComponents),
                    inCorr.add(i), outData.add(i));
        }
        return Status.ok();
    }

    @Override
    public PredictionSchemeMethod getPredictionMethod() {
        return PredictionSchemeMethod.DIFFERENCE;
    }

    @Override
    public boolean isInitialized() {
        return true;
    }
}
