package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.mesh.ICornerTable;

public class MPSchemeParallelogramDecoder<DataT, CorrT> extends MPSchemeDecoder<DataT, CorrT> {

    public MPSchemeParallelogramDecoder(PointAttribute attribute,
                                        PSchemeDecodingTransform<DataT, CorrT> transform,
                                        MPSchemeData<?> meshData) {
        super(attribute, transform, meshData);
    }

    @Override
    public Status computeOriginalValues(Pointer<CorrT> inCorr, Pointer<DataT> outData,
                                        int size, int numComponents, Pointer<PointIndex> entryToPointIdMap) {
        this.getTransform().init(numComponents);

        ICornerTable table = this.getMeshData().getCornerTable();
        CppVector<Integer> vertexToDataMap = this.getMeshData().getVertexToDataMap();

        // For storage of prediction values (already initialized to zero).
        Pointer<DataT> predVals = this.getDataType().newArray(numComponents);

        // Restore the first value.
        this.getTransform().computeOriginalValue(predVals, inCorr, outData);

        int cornerMapSize = (int) this.getMeshData().getDataToCornerMap().size();
        for(int p = 1; p < cornerMapSize; ++p) {
            CornerIndex cornerId = this.getMeshData().getDataToCornerMap().get(p);
            int dstOffset = p * numComponents;
            Status status = MPSchemeParallelogram.computeParallelogramPrediction(
                    p, cornerId, table, vertexToDataMap, outData, numComponents, predVals);
            if(status.isError()) {
                // Parallelogram could not be computed, Possible because some of the
                // vertices are not valid (not encoded yet).
                // We use the last encoded point as a reference (delta coding).
                int srcOffset = (p - 1) * numComponents;
                this.getTransform().computeOriginalValue(
                        outData.add(srcOffset), inCorr.add(dstOffset), outData.add(dstOffset));
            } else {
                // Apply the parallelogram prediction.
                this.getTransform().computeOriginalValue(predVals, inCorr.add(dstOffset), outData.add(dstOffset));
            }
        }
        return Status.ok();
    }

    @Override
    public PredictionSchemeMethod getPredictionMethod() {
        return PredictionSchemeMethod.MESH_PARALLELOGRAM;
    }

    @Override
    public boolean isInitialized() {
        return this.getMeshData().isInitialized();
    }
}
