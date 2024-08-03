package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.attributes.OctahedronToolBox;
import com.mndk.bteterrarenderer.draco.compression.bitcoder.RAnsBitDecoder;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.config.NormalPredictionMode;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.core.VectorD;

public class MPSchemeGeometricNormalDecoder<DataT, CorrT> extends MPSchemeDecoder<DataT, CorrT> {

    private final MPSchemeGeometricNormalPredictorArea<DataT> predictor;
    private final OctahedronToolBox octahedronToolBox = new OctahedronToolBox();
    private final RAnsBitDecoder flipNormalBitDecoder = new RAnsBitDecoder();

    public MPSchemeGeometricNormalDecoder(PointAttribute attribute,
                                          PSchemeDecodingTransform<DataT, CorrT> transform,
                                          MPSchemeData<?> meshData) {
        super(attribute, transform, meshData);
        this.predictor = new MPSchemeGeometricNormalPredictorArea<>(this.getDataType(), meshData);
    }

    @Override
    public Status computeOriginalValues(Pointer<CorrT> inCorr, Pointer<DataT> outData,
                                        int size, int numComponents, Pointer<PointIndex> entryToPointIdMap) {
        DataNumberType<DataT> dataType = this.getDataType().asNumber();
        this.setQuantizationBits(this.getTransform().getQuantizationBits());
        predictor.setEntryToPointIdMap(entryToPointIdMap);
        if(!this.isInitialized()) {
            return Status.dracoError("Not initialized");
        }

        // Expecting in_data in octahedral coordinates, i.e., portable attribute.
        if(numComponents != 2) {
            return Status.invalidParameter("Expecting 2 components");
        }

        int cornerMapSize = this.getMeshData().getDataToCornerMap().size();

        VectorD.I3 predNormal3D = new VectorD.I3();
        Pointer<Integer> predNormalOct = Pointer.wrap(new int[2]);

        for(int dataId = 0; dataId < cornerMapSize; ++dataId) {
            CornerIndex cornerId = this.getMeshData().getDataToCornerMap().get(dataId);
            predictor.computePredictedValue(cornerId, predNormal3D.getPointer().asRawTo(dataType));

            // Compute predicted octahedral coordinates.
            octahedronToolBox.canonicalizeIntegerVector(predNormal3D.getPointer());
            if(predNormal3D.absSum() != octahedronToolBox.getCenterValue()) {
                return Status.dracoError("Invalid sum");
            }
            if(flipNormalBitDecoder.decodeNextBit()) {
                predNormal3D = predNormal3D.negate();
            }
            octahedronToolBox.integerVectorToQuantizedOctahedralCoords(
                    predNormal3D.getPointer(), predNormalOct, predNormalOct.add(1));

            int dataOffset = dataId * 2;
            this.getTransform().computeOriginalValue(predNormalOct.asRawTo(dataType),
                    inCorr.add(dataOffset), outData.add(dataOffset));
        }
        flipNormalBitDecoder.endDecoding();
        return Status.ok();
    }

    @Override
    public Status decodePredictionData(DecoderBuffer buffer) {
        StatusChain chain = new StatusChain();

        // Get data needed for transform
        if(this.getTransform().decodeTransformData(buffer).isError(chain)) return chain.get();

        if(buffer.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            Pointer<UByte> predictionModeRef = Pointer.newUByte();
            if(buffer.decode(predictionModeRef).isError(chain)) return chain.get();
            NormalPredictionMode predictionMode = NormalPredictionMode.valueOf(predictionModeRef.get().intValue());
            if(predictionMode == null) {
                return Status.ioError("Invalid prediction mode");
            }
            if(predictor.setNormalPredictionMode(predictionMode).isError(chain)) return chain.get();
        }

        // Init normal flips.
        return flipNormalBitDecoder.startDecoding(buffer);
    }

    @Override
    public PredictionSchemeMethod getPredictionMethod() {
        return PredictionSchemeMethod.MESH_GEOMETRIC_NORMAL;
    }

    @Override
    public boolean isInitialized() {
        return predictor.isInitialized() && this.getMeshData().isInitialized() && octahedronToolBox.isInitialized();
    }

    @Override
    public int getNumParentAttributes() {
        return 1;
    }

    @Override
    public GeometryAttribute.Type getParentAttributeType(int i) {
        if(i != 0) {
            throw new IllegalArgumentException("Invalid parent attribute index");
        }
        return GeometryAttribute.Type.POSITION;
    }

    @Override
    public Status setParentAttribute(PointAttribute att) {
        if(att.getAttributeType() != GeometryAttribute.Type.POSITION) {
            return Status.invalidParameter("Invalid attribute type");
        }
        if(!att.getNumComponents().equals(3)) {
            return Status.invalidParameter("Currently works only for 3 component positions");
        }
        predictor.setPositionAttribute(att);
        return Status.ok();
    }

    public void setQuantizationBits(int q) {
        octahedronToolBox.setQuantizationBits(q);
    }
}
