package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.mesh.ICornerTable;

public class MPSchemeMultiParallelogramDecoder<DataT, CorrT> extends MPSchemeDecoder<DataT, CorrT> {

    public MPSchemeMultiParallelogramDecoder(PointAttribute attribute,
                                             PSchemeDecodingTransform<DataT, CorrT> transform,
                                             MPSchemeData<?> meshData) {
        super(attribute, transform, meshData);
    }

    @Override
    public Status computeOriginalValues(Pointer<CorrT> inCorr, Pointer<DataT> outData,
                                        int size, int numComponents, Pointer<PointIndex> entryToPointIdMap) {
        this.getTransform().init(numComponents);

        // For storage of prediction values (already initialized to zero).
        DataNumberType<DataT> dataType = this.getDataType();
        CppVector<DataT> predVals = new CppVector<>(dataType, numComponents);
        CppVector<DataT> parallelogramPredVals = new CppVector<>(dataType, numComponents);

        this.getTransform().computeOriginalValue(predVals.getPointer(), inCorr, outData);

        ICornerTable table = this.getMeshData().getCornerTable();
        CppVector<Integer> vertexToDataMap = this.getMeshData().getVertexToDataMap();

        int cornerMapSize = this.getMeshData().getDataToCornerMap().size();
        for(int p = 1; p < cornerMapSize; ++p) {
            CornerIndex startCornerId = this.getMeshData().getDataToCornerMap().get(p);
            int numParallelograms = 0;
            for (int i = 0; i < numComponents; ++i) {
                predVals.set(i, dataType.from(0));
            }
            CornerIndex cornerId = startCornerId;
            while (cornerId.isValid()) {
                Status status = MPSchemeParallelogram.computeParallelogramPrediction(
                        p, cornerId, table, vertexToDataMap, outData, numComponents,
                        parallelogramPredVals.getPointer());
                if(status.isOk()) {
                    for (int c = 0; c < numComponents; ++c) {
                        predVals.set(c, dataType.add(predVals.get(c), parallelogramPredVals.get(c)));
                    }
                    ++numParallelograms;
                }
                cornerId = table.swingRight(cornerId);
                if (cornerId.equals(startCornerId)) {
                    cornerId = CornerIndex.INVALID;
                }
            }
            int dstOffset = p * numComponents;
            if (numParallelograms == 0) {
                // No parallelogram was valid.
                // We use the last decoded point as a reference.
                int srcOffset = (p - 1) * numComponents;
                this.getTransform().computeOriginalValue(outData.add(srcOffset),
                        inCorr.add(dstOffset), outData.add(dstOffset));
            } else {
                // Compute the correction from the predicted value.
                for (int c = 0; c < numComponents; ++c) {
                    predVals.set(c, dataType.div(predVals.get(c), numParallelograms));
                }
                this.getTransform().computeOriginalValue(predVals.getPointer(),
                        inCorr.add(dstOffset), outData.add(dstOffset));
            }
        }
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
