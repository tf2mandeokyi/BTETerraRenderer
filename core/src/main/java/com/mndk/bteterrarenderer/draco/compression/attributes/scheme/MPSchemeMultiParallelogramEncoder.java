package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.DataNumberType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.mesh.ICornerTable;

public class MPSchemeMultiParallelogramEncoder<DataT, CorrT> extends MPSchemeEncoder<DataT, CorrT> {

    public MPSchemeMultiParallelogramEncoder(PointAttribute attribute,
                                             PSchemeEncodingTransform<DataT, CorrT> transform,
                                             MPSchemeData<?> meshData) {
        super(attribute, transform, meshData);
    }

    @Override
    public Status computeCorrectionValues(Pointer<DataT> inData, Pointer<CorrT> outCorr, int size, int numComponents,
                                          Pointer<PointIndex> entryToPointIdMap) {
        this.getTransform().init(inData, size, numComponents);
        MPSchemeData<?> meshData = this.getMeshData();
        ICornerTable table = meshData.getCornerTable();
        CppVector<Integer> vertexToDataMap = meshData.getVertexToDataMap();

        // For storage of prediction values
        DataNumberType<DataT> dataType = this.getDataType();
        Pointer<DataT> predVals = dataType.newArray(numComponents);
        Pointer<DataT> parallelogramPredVals = dataType.newArray(numComponents);

        // We start processing from the end because this prediction uses data from
        // previous entries that could be overwritten when an entry is processed.
        for(int p = (int) (this.getMeshData().getDataToCornerMap().size() - 1); p > 0; p--) {
            CornerIndex startCornerId = this.getMeshData().getDataToCornerMap().get(p);

            // Go over all corners attached to the vertex and compute the predicted
            // value from the parallelograms defined by their opposite faces.
            CornerIndex cornerId = startCornerId;
            int numParallelograms = 0;
            for(int i = 0; i < numComponents; ++i) {
                predVals.set(i, dataType.from(0));
            }
            while(cornerId.isInvalid()) {
                Status status = MPSchemeParallelogram.computeParallelogramPrediction(
                        p, cornerId, table, vertexToDataMap, inData, numComponents, parallelogramPredVals);
                if(status.isOk()) {
                    for(int c = 0; c < numComponents; ++c) {
                        predVals.set(c, dataType.add(predVals.get(c), parallelogramPredVals.get(c)));
                    }
                    ++numParallelograms;
                }
                cornerId = table.swingRight(cornerId);
                if(cornerId.equals(startCornerId)) {
                    cornerId = CornerIndex.INVALID;
                }
            }
            int dstOffset = p * numComponents;
            if(numParallelograms == 0) {
                // No parallelogram was valid.
                // We use the last encoded point as a reference.
                int srcOffset = (p - 1) * numComponents;
                this.getTransform().computeCorrection(
                        inData.add(dstOffset), inData.add(srcOffset), outCorr.add(dstOffset));
            } else {
                // Compute the correction from the predicted value.
                for(int c = 0; c < numComponents; ++c) {
                    predVals.set(c, dataType.div(predVals.get(c), numParallelograms));
                }
                this.getTransform().computeCorrection(inData.add(dstOffset), predVals, outCorr.add(dstOffset));
            }
        }
        // First element is always fixed because it cannot be predicted.
        for(int i = 0; i < numComponents; i++) {
            predVals.set(i, dataType.from(0));
        }
        this.getTransform().computeCorrection(inData, predVals, outCorr);
        return Status.ok();
    }

    @Override
    public PredictionSchemeMethod getPredictionMethod() {
        return PredictionSchemeMethod.MESH_MULTI_PARALLELOGRAM;
    }

    @Override
    public boolean isInitialized() {
        return this.getMeshData().isInitialized();
    }
}
