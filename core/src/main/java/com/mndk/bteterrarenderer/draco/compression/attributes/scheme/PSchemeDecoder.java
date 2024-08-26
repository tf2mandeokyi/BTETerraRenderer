package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.DataNumberType;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeTransformType;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class PSchemeDecoder<DataT, CorrT> implements PSchemeTypedDecoderInterface<DataT, CorrT> {

    private final PointAttribute attribute;
    private final PSchemeDecodingTransform<DataT, CorrT> transform;

    @Override public DataNumberType<DataT> getDataType() { return transform.getDataType(); }
    @Override public DataNumberType<CorrT> getCorrType() { return transform.getCorrType(); }

    @Override
    public Status decodePredictionData(DecoderBuffer buffer) {
        return transform.decodeTransformData(buffer);
    }

    @Override
    public int getNumParentAttributes() {
        return 0;
    }

    @Override
    public PointAttribute.Type getParentAttributeType(int i) {
        return GeometryAttribute.Type.INVALID;
    }

    @Override
    public Status setParentAttribute(PointAttribute att) {
        return Status.unsupportedFeature("Parent attributes are not supported");
    }

    @Override
    public boolean areCorrectionsPositive() {
        return transform.areCorrectionsPositive();
    }

    @Override
    public PredictionSchemeTransformType getTransformType() {
        return transform.getType();
    }
}
