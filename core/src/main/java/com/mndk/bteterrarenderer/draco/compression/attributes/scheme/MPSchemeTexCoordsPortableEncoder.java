package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.bitcoder.RAnsBitEncoder;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;

public class MPSchemeTexCoordsPortableEncoder<DataT, CorrT> extends MPSchemeEncoder<DataT, CorrT> {

    private final MPSchemeTexCoordsPortablePredictor<DataT> predictor;

    public MPSchemeTexCoordsPortableEncoder(PointAttribute attribute,
                                            PSchemeEncodingTransform<DataT, CorrT> transform,
                                            MPSchemeData<?> meshData) {
        super(attribute, transform, meshData);
        this.predictor = new MPSchemeTexCoordsPortablePredictor<>(this.getDataType(), meshData);
    }

    @Override
    public Status computeCorrectionValues(Pointer<DataT> inData, Pointer<CorrT> outCorr, int size, int numComponents,
                                          Pointer<PointIndex> entryToPointIdMap) {
        StatusChain chain = new StatusChain();

        predictor.setEntryToPointIdMap(entryToPointIdMap);
        this.getTransform().init(inData, size, numComponents);
        // We start processing from the end because this prediction uses data from
        // previous entries that could be overwritten when an entry is processed.
        for(int p = (int) (this.getMeshData().getDataToCornerMap().size() - 1); p >= 0; p--) {
            CornerIndex cornerId = this.getMeshData().getDataToCornerMap().get(p);
            if(predictor.computePredictedValue(cornerId, inData, p, true).isError(chain)) return chain.get();

            int dstOffset = p * numComponents;
            this.getTransform().computeCorrection(inData.add(dstOffset), predictor.getPredictedValue(), outCorr.add(dstOffset));
        }
        return Status.ok();
    }

    @Override
    public Status encodePredictionData(EncoderBuffer buffer) {
        int numOrientations = (int) predictor.getNumOrientations();
        buffer.encode(DataType.int32(), numOrientations);
        boolean lastOrientation = true;
        RAnsBitEncoder encoder = new RAnsBitEncoder();
        encoder.startEncoding();
        for(int i = 0; i < numOrientations; i++) {
            boolean orientation = predictor.getOrientation(i);
            encoder.encodeBit(orientation == lastOrientation);
            lastOrientation = orientation;
        }
        encoder.endEncoding(buffer);
        return super.encodePredictionData(buffer);
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
}
