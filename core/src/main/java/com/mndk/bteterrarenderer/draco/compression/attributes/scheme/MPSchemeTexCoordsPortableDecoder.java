package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.bitcoder.RAnsBitDecoder;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;

public class MPSchemeTexCoordsPortableDecoder<DataT, CorrT> extends MPSchemeDecoder<DataT, CorrT> {

    private final MPSchemeTexCoordsPortablePredictor<DataT> predictor;

    public MPSchemeTexCoordsPortableDecoder(PointAttribute attribute,
                                            PSchemeDecodingTransform<DataT, CorrT> transform,
                                            MPSchemeData<?> meshData) {
        super(attribute, transform, meshData);
        DataNumberType<DataT> dataType = this.getDataType();
        this.predictor = new MPSchemeTexCoordsPortablePredictor<>(dataType, meshData);
    }

    @Override
    public Status computeOriginalValues(Pointer<CorrT> inCorr, Pointer<DataT> outData,
                                        int size, int numComponents, Pointer<PointIndex> entryToPointIdMap) {
        StatusChain chain = new StatusChain();

        if (numComponents != MPSchemeTexCoordsPortablePredictor.NUM_COMPONENTS) {
            return Status.ioError("Invalid number of components");
        }
        predictor.setEntryToPointIdMap(entryToPointIdMap);
        this.getTransform().init(numComponents);

        int cornerMapSize = this.getMeshData().getDataToCornerMap().size();
        for (int p = 0; p < cornerMapSize; ++p) {
            CornerIndex cornerId = this.getMeshData().getDataToCornerMap().get(p);
            if (predictor.computePredictedValue(cornerId, outData, p, false).isError(chain)) return chain.get();

            int dstOffset = p * numComponents;
            this.getTransform().computeOriginalValue(predictor.getPredictedValue(),
                    inCorr.add(dstOffset), outData.add(dstOffset));
        }
        return Status.ok();
    }

    @Override
    public Status decodePredictionData(DecoderBuffer buffer) {
        StatusChain chain = new StatusChain();

        Pointer<Integer> numOrientationsRef = Pointer.newInt();
        if(buffer.decode(numOrientationsRef).isError(chain)) return chain.get();
        int numOrientations = numOrientationsRef.get();
        if (numOrientations < 0) {
            return Status.ioError("Invalid number of orientations");
        }

        predictor.resizeOrientations(numOrientations);
        boolean lastOrientation = true;
        RAnsBitDecoder decoder = new RAnsBitDecoder();
        if(decoder.startDecoding(buffer).isError(chain)) return chain.get();
        for (int i = 0; i < numOrientations; ++i) {
            if(!decoder.decodeNextBit()) {
                lastOrientation = !lastOrientation;
            }
            predictor.setOrientation(i, lastOrientation);
        }
        decoder.endDecoding();
        return super.decodePredictionData(buffer);
    }

    @Override
    public PredictionSchemeMethod getPredictionMethod() {
        return PredictionSchemeMethod.MESH_TEX_COORDS_PORTABLE;
    }

    @Override
    public boolean isInitialized() {
        return predictor.isInitialized() && this.getMeshData().isInitialized();
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
        if(att == null || att.getAttributeType() != GeometryAttribute.Type.POSITION) {
            return Status.invalidParameter("Invalid attribute type");
        }
        if(!att.getNumComponents().equals(3)) {
            return Status.invalidParameter("Currently works only for 3 component positions");
        }
        predictor.setPositionAttribute(att);
        return Status.ok();
    }
}
