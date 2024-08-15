package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.core.Status;

public class PSchemeDeltaEncoder<DataT, CorrT> extends PSchemeEncoder<DataT, CorrT> {

    public PSchemeDeltaEncoder(PointAttribute attribute,
                               PSchemeEncodingTransform<DataT, CorrT> transform) {
        super(attribute, transform);
    }

    @Override
    public Status computeCorrectionValues(Pointer<DataT> inData, Pointer<CorrT> outCorr, int size, int numComponents,
                                          Pointer<PointIndex> entryToPointIdMap) {
        this.getTransform().init(inData, size, numComponents);
        for (int i = size - numComponents; i > 0; i -= numComponents) {
            this.getTransform().computeCorrection(inData.add(i), inData.add(i - numComponents), outCorr.add(i));
        }
        // Encode correction for the first element.
        Pointer<DataT> zeroVals = this.getDataType().newArray(numComponents);
        this.getTransform().computeCorrection(inData, zeroVals, outCorr);
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
