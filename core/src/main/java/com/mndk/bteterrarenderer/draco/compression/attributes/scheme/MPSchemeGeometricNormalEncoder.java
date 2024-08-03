package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.attributes.OctahedronToolBox;
import com.mndk.bteterrarenderer.draco.compression.bitcoder.RAnsBitEncoder;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.VectorD;

public class MPSchemeGeometricNormalEncoder<DataT, CorrT> extends MPSchemeEncoder<DataT, CorrT> {

    private final MPSchemeGeometricNormalPredictorArea<DataT> predictor;
    private final OctahedronToolBox octahedronToolBox = new OctahedronToolBox();
    private final RAnsBitEncoder flipNormalBitEncoder = new RAnsBitEncoder();

    public MPSchemeGeometricNormalEncoder(PointAttribute attribute,
                                          PSchemeEncodingTransform<DataT, CorrT> transform,
                                          MPSchemeData<?> meshData) {
        super(attribute, transform, meshData);
        this.predictor = new MPSchemeGeometricNormalPredictorArea<>(this.getDataType(), meshData);
    }

    @Override
    public Status computeCorrectionValues(Pointer<DataT> inData, Pointer<CorrT> outCorr, int size, int numComponents,
                                          Pointer<PointIndex> entryToPointIdMap) {
        DataNumberType<DataT> dataType = this.getDataType().asNumber();
        DataNumberType<CorrT> corrType = this.getCorrType().asNumber();
        this.setQuantizationBits(this.getTransform().getQuantizationBits());
        this.predictor.setEntryToPointIdMap(entryToPointIdMap);
        if(!this.isInitialized()) {
            return Status.dracoError("Predictor is not initialized");
        }
        if(numComponents != 2) {
            return Status.invalidParameter("Expecting in_data in octahedral coordinates");
        }

        flipNormalBitEncoder.startEncoding();

        int cornerMapSize = this.getMeshData().getDataToCornerMap().size();

        VectorD.I3 predNormal3D = new VectorD.I3();
        VectorD.I2 posPredNormalOct = new VectorD.I2();
        VectorD.I2 negPredNormalOct = new VectorD.I2();
        VectorD.I2 posCorrection = new VectorD.I2();
        VectorD.I2 negCorrection = new VectorD.I2();
        for(int dataId = 0; dataId < cornerMapSize; dataId++) {
            CornerIndex cornerId = this.getMeshData().getDataToCornerMap().get(dataId);
            this.predictor.computePredictedValue(cornerId, predNormal3D.getPointer().asRawTo(dataType));

            // Compute predicted octahedral coordinates
            octahedronToolBox.canonicalizeIntegerVector(predNormal3D.getPointer().asRawTo(dataType));
            if(predNormal3D.absSum() != octahedronToolBox.getCenterValue()) {
                return Status.dracoError("Invalid normal");
            }

            // Compute octahedral coordinates for both possible directions
            octahedronToolBox.integerVectorToQuantizedOctahedralCoords(
                    predNormal3D.getPointer(), posPredNormalOct.getPointer(0), posPredNormalOct.getPointer(1));
            predNormal3D = predNormal3D.negate();
            octahedronToolBox.integerVectorToQuantizedOctahedralCoords(
                    predNormal3D.getPointer(), negPredNormalOct.getPointer(0), negPredNormalOct.getPointer(1));

            // Choose the one with the best correction value
            int dataOffset = dataId * 2;
            this.getTransform().computeCorrection(inData.add(dataOffset),
                    posPredNormalOct.getPointer().asRawTo(dataType), posCorrection.getPointer().asRawTo(corrType));
            this.getTransform().computeCorrection(inData.add(dataOffset),
                    negPredNormalOct.getPointer().asRawTo(dataType), negCorrection.getPointer().asRawTo(corrType));
            posCorrection.set(0, octahedronToolBox.modMax(posCorrection.get(0)));
            posCorrection.set(1, octahedronToolBox.modMax(posCorrection.get(1)));
            negCorrection.set(0, octahedronToolBox.modMax(negCorrection.get(0)));
            negCorrection.set(1, octahedronToolBox.modMax(negCorrection.get(1)));
            Pointer<CorrT> outOffset = outCorr.add(dataOffset);
            if(posCorrection.absSum() < negCorrection.absSum()) {
                flipNormalBitEncoder.encodeBit(false);
                outOffset.set(0, corrType.from(octahedronToolBox.makePositive(posCorrection.get(0))));
                outOffset.set(1, corrType.from(octahedronToolBox.makePositive(posCorrection.get(1))));
            }
            else {
                flipNormalBitEncoder.encodeBit(true);
                outOffset.set(0, corrType.from(octahedronToolBox.makePositive(negCorrection.get(0))));
                outOffset.set(1, corrType.from(octahedronToolBox.makePositive(negCorrection.get(1))));
            }
        }
        return Status.ok();
    }

    @Override
    public Status encodePredictionData(EncoderBuffer buffer) {
        Status status = this.getTransform().encodeTransformData(buffer);
        if(status.isError()) return status;
        flipNormalBitEncoder.endEncoding(buffer);
        return Status.ok();
    }

    @Override
    public PredictionSchemeMethod getPredictionMethod() {
        return PredictionSchemeMethod.MESH_GEOMETRIC_NORMAL;
    }

    @Override
    public boolean isInitialized() {
        return this.predictor.isInitialized();
    }

    @Override
    public int getNumParentAttributes() {
        return 1;
    }

    @Override
    public GeometryAttribute.Type getParentAttributeType(int i) {
        if(i != 0) throw new IllegalArgumentException("Invalid parent attribute index");
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

    private void setQuantizationBits(int q) {
        if(q < 2 || q > 30) {
            throw new IllegalArgumentException("Quantization bits must be between 2 and 30");
        }
        octahedronToolBox.setQuantizationBits(q);
    }
}
