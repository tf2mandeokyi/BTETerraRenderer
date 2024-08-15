package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.UShort;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.bitcoder.RAnsBitDecoder;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.core.VectorD;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;

public class MPSchemeTexCoordsDecoder<DataT, CorrT> extends MPSchemeDecoder<DataT, CorrT> {

    private PointAttribute posAttribute = null;
    private Pointer<PointIndex> entryToPointIdMap = null;
    private Pointer<DataT> predictedValue;
    private int numComponents = 0;
    /** Encoded / decoded array of UV flips. */
    private final CppVector<Boolean> orientations = new CppVector<>(DataType.bool());
    private final int version;

    public MPSchemeTexCoordsDecoder(PointAttribute attribute,
                                    PSchemeDecodingTransform<DataT, CorrT> transform,
                                    MPSchemeData<?> meshData, UShort bitstreamVersion) {
        super(attribute, transform, meshData);
        this.version = bitstreamVersion.intValue();
    }

    @Override
    public Status computeOriginalValues(Pointer<CorrT> inCorr, Pointer<DataT> outData, int size, int numComponents,
                                        Pointer<PointIndex> entryToPointIdMap) {
        StatusChain chain = new StatusChain();

        if(numComponents != 2) {
            return Status.invalidParameter("Two output components are required");
        }
        this.numComponents = numComponents;
        this.entryToPointIdMap = entryToPointIdMap;
        this.predictedValue = this.getDataType().newArray(numComponents);
        this.getTransform().init(numComponents);

        for(int p = 0; p < this.getMeshData().getDataToCornerMap().size(); ++p) {
            CornerIndex cornerId = this.getMeshData().getDataToCornerMap().get(p);
            if(this.computePredictedValue(cornerId, outData, p).isError(chain)) return chain.get();
            int dstOffset = p * numComponents;
            this.getTransform().computeOriginalValue(
                    predictedValue, inCorr.add(dstOffset), outData.add(dstOffset));
        }
        return Status.ok();
    }

    @Override
    public Status decodePredictionData(DecoderBuffer buffer) {
        StatusChain chain = new StatusChain();

        // Decode the delta coded orientations.
        Pointer<UInt> numOrientationsRef = Pointer.newUInt();
        if(buffer.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            if(buffer.decode(numOrientationsRef).isError(chain)) return chain.get();
        } else {
            if(buffer.decodeVarint(numOrientationsRef).isError(chain)) return chain.get();
        }
        int numOrientations = numOrientationsRef.get().intValue();
        if(numOrientations == 0) {
            return Status.ioError("Number of orientations is 0");
        }
        if(numOrientations > this.getMeshData().getCornerTable().getNumCorners()) {
            // We can't have more orientations than the maximum number of decoded
            // values.
            return Status.ioError("Number of orientations is greater than the number of corners");
        }
        orientations.resize(numOrientations);
        boolean lastOrientation = true;
        RAnsBitDecoder decoder = new RAnsBitDecoder();
        if(decoder.startDecoding(buffer).isError(chain)) return chain.get();
        for(int i = 0; i < numOrientations; ++i) {
            boolean orientation = decoder.decodeNextBit();
            if(orientation) {
                lastOrientation = !lastOrientation;
            }
            orientations.set(i, lastOrientation);
        }
        decoder.endDecoding();
        return super.decodePredictionData(buffer);
    }

    @Override
    public PredictionSchemeMethod getPredictionMethod() {
        return PredictionSchemeMethod.MESH_TEX_COORDS_DEPRECATED;
    }

    @Override
    public boolean isInitialized() {
        return posAttribute != null && this.getMeshData().isInitialized();
    }

    @Override
    public int getNumParentAttributes() {
        return 1;
    }

    @Override
    public GeometryAttribute.Type getParentAttributeType(int i) {
        if(i != 0) throw new IllegalArgumentException();
        return GeometryAttribute.Type.POSITION;
    }

    @Override
    public Status setParentAttribute(PointAttribute att) {
        if(att == null) {
            return Status.invalidParameter("Attribute is null");
        }
        if(att.getAttributeType() != GeometryAttribute.Type.POSITION) {
            return Status.invalidParameter("Invalid attribute type");
        }
        if(!att.getNumComponents().equals(3)) {
            return Status.invalidParameter("Currently works only for 3 component positions");
        }
        posAttribute = att;
        return Status.ok();
    }

    protected VectorD.F3 getPositionForEntryId(int entryId) {
        PointIndex pointId = entryToPointIdMap.get(entryId);
        VectorD.F3 pos = new VectorD.F3();
        posAttribute.convertValue(posAttribute.getMappedIndex(pointId), pos.getPointer());
        return pos;
    }

    protected VectorD.F2 getTexCoordForEntryId(int entryId, Pointer<DataT> data) {
        int dataOffset = entryId * numComponents;
        DataNumberType<DataT> dataType = this.getDataType();
        return new VectorD.F2(dataType.toFloat(data.get(dataOffset)), dataType.toFloat(data.get(dataOffset + 1)));
    }

    protected Status computePredictedValue(CornerIndex cornerId, Pointer<DataT> data, int dataId) {
        DataNumberType<DataT> dataType = this.getDataType();
        // Compute the predicted UV coordinate from the positions on all corners
        // of the processed triangle.
        CornerIndex nextCornerId = this.getMeshData().getCornerTable().next(cornerId);
        CornerIndex prevCornerId = this.getMeshData().getCornerTable().previous(cornerId);
        // Get the encoded data ids from the next and previous corners.
        int nextVertId = this.getMeshData().getCornerTable().getVertex(nextCornerId).getValue();
        int prevVertId = this.getMeshData().getCornerTable().getVertex(prevCornerId).getValue();
        int nextDataId = this.getMeshData().getVertexToDataMap().get(nextVertId);
        int prevDataId = this.getMeshData().getVertexToDataMap().get(prevVertId);
        if (prevDataId < dataId && nextDataId < dataId) {
            // Both other corners have available UV coordinates for prediction.
            VectorD.F2 nUV = getTexCoordForEntryId(nextDataId, data);
            VectorD.F2 pUV = getTexCoordForEntryId(prevDataId, data);
            if (pUV.equals(nUV)) {
                // We cannot do a reliable prediction on degenerated UV triangles.
                for (int i = 0; i <= 1; i++) {
                    double pUVid = pUV.get(i);
                    if (Float.isNaN(pUV.get(i)) || pUVid > Integer.MAX_VALUE || pUVid < Integer.MIN_VALUE) {
                        predictedValue.set(i, dataType.from(Integer.MIN_VALUE));
                    } else {
                        predictedValue.set(i, dataType.from(pUV.get(i)));
                    }
                }
                return Status.ok();
            }
            // Get positions at all corners.
            VectorD.F3 tipPos = getPositionForEntryId(dataId);
            VectorD.F3 nextPos = getPositionForEntryId(nextDataId);
            VectorD.F3 prevPos = getPositionForEntryId(prevDataId);
            // Use the positions of the above triangle to predict the texture coordinate
            // on the tip corner C.
            VectorD.F3 pn = prevPos.subtract(nextPos);
            VectorD.F3 cn = tipPos.subtract(nextPos);
            float pnNorm2Squared = pn.squaredNorm();
            float s, t;
            if (version < DracoVersions.getBitstreamVersion(1, 2) || pnNorm2Squared > 0) {
                s = pn.dot(cn) / pnNorm2Squared;
                t = (float) Math.sqrt((cn.subtract(pn.multiply(s)).squaredNorm() / pnNorm2Squared));
            } else {
                s = 0;
                t = 0;
            }

            // Now we need to transform the point (s, t) to the texture coordinate space UV.
            VectorD.F2 pnUV = pUV.subtract(nUV);
            float pnus = pnUV.get(0) * s + nUV.get(0);
            float pnut = pnUV.get(0) * t;
            float pnvs = pnUV.get(1) * s + nUV.get(1);
            float pnvt = pnUV.get(1) * t;
            VectorD.F2 predictedUV;
            if (orientations.isEmpty()) {
                return Status.ioError("No orientations");
            }

            // When decoding the data, we already know which orientation to use.
            boolean orientation = orientations.popBack();
            if (orientation) {
                predictedUV = new VectorD.F2(pnus - pnvt, pnvs + pnut);
            } else {
                predictedUV = new VectorD.F2(pnus + pnvt, pnvs - pnut);
            }
            if(dataType.isIntegral()) {
                // Round the predicted value for integer types.
                double u = Math.floor(predictedUV.get(0) + 0.5);
                if (Double.isNaN(u) || u > Integer.MAX_VALUE || u < Integer.MIN_VALUE) {
                    predictedValue.set(0, dataType.from(Integer.MIN_VALUE));
                } else {
                    predictedValue.set(0, dataType.from((int) u));
                }
                double v = Math.floor(predictedUV.get(1) + 0.5);
                if (Double.isNaN(v) || v > Integer.MAX_VALUE || v < Integer.MIN_VALUE) {
                    predictedValue.set(1, dataType.from(Integer.MIN_VALUE));
                } else {
                    predictedValue.set(1, dataType.from((int) v));
                }
            } else {
                predictedValue.set(0, dataType.from(predictedUV.get(0)));
                predictedValue.set(1, dataType.from(predictedUV.get(1)));
            }
            return Status.ok();
        }
        // Else we don't have available textures on both corners.
        int dataOffset = 0;
        if (prevDataId < dataId) {
            // Use the value on the previous corner as the prediction.
            dataOffset = prevDataId * numComponents;
        }
        if (nextDataId < dataId) {
            // Use the value on the next corner as the prediction.
            dataOffset = nextDataId * numComponents;
        } else {
            // None of the other corners have a valid value. Use the last encoded value
            // as the prediction if possible.
            if (dataId > 0) {
                dataOffset = (dataId - 1) * numComponents;
            } else {
                // We are encoding the first value. Predict 0.
                for (int i = 0; i < numComponents; ++i) {
                    predictedValue.set(i, dataType.from(0));
                }
                return Status.ok();
            }
        }
        for (int i = 0; i < numComponents; ++i) {
            predictedValue.set(i, data.get(dataOffset + i));
        }
        return Status.ok();
    }
}
