package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.core.DracoMathUtils;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.VectorD;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MPSchemeTexCoordsPortablePredictor<DataT> {

    public static final int NUM_COMPONENTS = 2;

    private final DataNumberType<DataT> dataType;
    private PointAttribute positionAttribute = null;
    private Pointer<PointIndex> entryToPointIdMap = null;
    private final Pointer<DataT> predictedValue;
    /* Encoded / decoded array of UV flips. */
    private final CppVector<Boolean> orientations = new CppVector<>(DataType.bool());
    private final MPSchemeData<?> meshData;

    public MPSchemeTexCoordsPortablePredictor(DataNumberType<DataT> dataType, MPSchemeData<?> meshData) {
        this.dataType = dataType;
        this.predictedValue = dataType.newArray(NUM_COMPONENTS);
        this.meshData = meshData;
    }

    public boolean getOrientation(int i) { return orientations.get(i); }
    public void setOrientation(int i, boolean v) { orientations.set(i, v); }
    public int getNumOrientations() { return orientations.size(); }
    public void resizeOrientations(int numOrientations) { orientations.resize(numOrientations); }

    public boolean isInitialized() {
        return positionAttribute != null;
    }

    public VectorD.L3 getPositionForEntryId(int entryId) {
        PointIndex pointId = entryToPointIdMap.get(entryId);
        VectorD.L3 pos = new VectorD.L3();
        positionAttribute.convertValue(positionAttribute.getMappedIndex(pointId), pos.getPointer());
        return pos;
    }

    public VectorD.L2 getTexCoordForEntryId(int entryId, Pointer<DataT> data) {
        int dataOffset = entryId * NUM_COMPONENTS;
        return new VectorD.L2(dataType.toLong(data.get(dataOffset)), dataType.toLong(data.get(dataOffset + 1)));
    }

    public Status computePredictedValue(CornerIndex cornerId, Pointer<DataT> data, int dataId, boolean isEncoder) {
        // Compute the predicted UV coordinate from the positions on all corners
        // of the processed triangle.
        CornerIndex nextCornerId = meshData.getCornerTable().next(cornerId);
        CornerIndex prevCornerId = meshData.getCornerTable().previous(cornerId);
        // Get the encoded data ids from the next and previous corners.
        int nextVertId = meshData.getCornerTable().getVertex(nextCornerId).getValue();
        int prevVertId = meshData.getCornerTable().getVertex(prevCornerId).getValue();
        int nextDataId = meshData.getVertexToDataMap().get(nextVertId);
        int prevDataId = meshData.getVertexToDataMap().get(prevVertId);

        if(prevDataId < dataId && nextDataId < dataId) {
            // Both other corners have available UV coordinates for prediction.
            VectorD.L2 nUV = getTexCoordForEntryId(nextDataId, data);
            VectorD.L2 pUV = getTexCoordForEntryId(prevDataId, data);
            if(pUV.equals(nUV)) {
                // We cannot do a reliable prediction on degenerated UV triangles.
                predictedValue.set(0, dataType.from(pUV.get(0)));
                predictedValue.set(1, dataType.from(pUV.get(1)));
                return Status.ok();
            }

            // Get positions at all corners.
            VectorD.L3 tipPos = getPositionForEntryId(dataId);
            VectorD.L3 nextPos = getPositionForEntryId(nextDataId);
            VectorD.L3 prevPos = getPositionForEntryId(prevDataId);
            // We use the positions of the above triangle to predict the texture
            // coordinate on the tip corner C.
            VectorD.L3 pn = prevPos.subtract(nextPos);
            long pnNorm2Squared = pn.squaredNorm();
            if(pnNorm2Squared != 0) {
                // Compute the projection of C onto PN by computing dot product of CN with
                // PN and normalizing it by length of PN.
                long cnDotPn = pn.dot(tipPos.subtract(nextPos));
                VectorD.L2 pnUV = pUV.subtract(nUV);
                long nUVAbsMaxElement = Math.max(Math.abs(nUV.get(0)), Math.abs(nUV.get(1)));
                if(nUVAbsMaxElement > Long.MAX_VALUE / pnNorm2Squared) {
                    return Status.ioError("Overflow");
                }
                long pnUVAbsMaxElement = Math.max(Math.abs(pnUV.get(0)), Math.abs(pnUV.get(1)));
                if(Math.abs(cnDotPn) > Long.MAX_VALUE / pnUVAbsMaxElement) {
                    return Status.ioError("Overflow");
                }
                VectorD.L2 xUV = nUV.multiply(pnNorm2Squared).add(pnUV.multiply(cnDotPn));
                long pnAbsMaxElement = Math.max(Math.max(Math.abs(pn.get(0)), Math.abs(pn.get(1))), Math.abs(pn.get(2)));
                if(Math.abs(cnDotPn) > Long.MAX_VALUE / pnAbsMaxElement) {
                    return Status.ioError("Overflow");
                }

                // Compute squared length of vector CX in position coordinate system:
                VectorD.L3 xPos = nextPos.add(pn.multiply(cnDotPn).divide(pnNorm2Squared));
                long cxNorm2Squared = tipPos.subtract(xPos).squaredNorm();

                // Compute vector CX_UV in the uv space by rotating vector PN_UV by 90
                // degrees and scaling it with factor CX.Norm2() / PN.Norm2():
                VectorD.L2 cxUV = new VectorD.L2(pnUV.get(1), -pnUV.get(0));
                long normSquared = DracoMathUtils.intSqrt(cxNorm2Squared * pnNorm2Squared);
                cxUV = cxUV.multiply(normSquared);

                // Predicted uv coordinate is then computed by either adding or
                // subtracting CX_UV to/from X_UV.
                VectorD.L2 predictedUV;
                if(isEncoder) {
                    VectorD.L2 predictedUV0 = xUV.add(cxUV).divide(pnNorm2Squared);
                    VectorD.L2 predictedUV1 = xUV.subtract(cxUV).divide(pnNorm2Squared);
                    VectorD.L2 cUV = getTexCoordForEntryId(dataId, data);
                    if(cUV.subtract(predictedUV0).squaredNorm() < cUV.subtract(predictedUV1).squaredNorm()) {
                        predictedUV = predictedUV0;
                        orientations.pushBack(true);
                    } else {
                        predictedUV = predictedUV1;
                        orientations.pushBack(false);
                    }
                } else {
                    if(orientations.isEmpty()) {
                        return Status.ioError("Orientation is empty");
                    }
                    boolean orientation = orientations.popBack();
                    VectorD.UL2 xUVu = new VectorD.UL2(xUV);
                    VectorD.UL2 cxUVu = new VectorD.UL2(cxUV);
                    if(orientation) {
                        predictedUV = new VectorD.L2(xUVu.add(cxUVu)).divide(pnNorm2Squared);
                    } else {
                        predictedUV = new VectorD.L2(xUVu.subtract(cxUVu)).divide(pnNorm2Squared);
                    }
                }
                predictedValue.set(0, dataType.from(predictedUV.get(0)));
                predictedValue.set(1, dataType.from(predictedUV.get(1)));
                return Status.ok();
            }
        }
        // Else we don't have available textures on both corners or the position data
        // is invalid.
        int dataOffset = 0;
        if(prevDataId < dataId) {
            dataOffset = prevDataId * NUM_COMPONENTS;
        }
        if(nextDataId < dataId) {
            dataOffset = nextDataId * NUM_COMPONENTS;
        } else {
            if(dataId > 0) {
                dataOffset = (dataId - 1) * NUM_COMPONENTS;
            } else {
                for(int i = 0; i < NUM_COMPONENTS; ++i) {
                    predictedValue.set(i, dataType.from(0));
                }
                return Status.ok();
            }
        }
        for(int i = 0; i < NUM_COMPONENTS; ++i) {
            predictedValue.set(i, data.get(dataOffset + i));
        }
        return Status.ok();
    }
}
