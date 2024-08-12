package com.mndk.bteterrarenderer.draco.compression.attributes;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.draco.attributes.AttributeOctahedronTransform;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.attributes.scheme.PSchemeEncoderFactory;
import com.mndk.bteterrarenderer.draco.compression.attributes.scheme.PSchemeNormalOctahedronCanonicalizedEncodingTransform;
import com.mndk.bteterrarenderer.draco.compression.attributes.scheme.PSchemeTypedEncoderInterface;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.compression.config.SequentialAttributeEncoderType;
import com.mndk.bteterrarenderer.draco.compression.pointcloud.PointCloudEncoder;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;

public class SequentialNormalAttributeEncoder extends SequentialIntegerAttributeEncoder {

    private final AttributeOctahedronTransform attributeOctahedronTransform = new AttributeOctahedronTransform();

    @Override
    public UByte getUniqueId() {
        return UByte.of(SequentialAttributeEncoderType.NORMALS.getValue());
    }

    @Override
    public boolean isLossyEncoder() {
        return true;
    }

    @Override
    public Status encodeDataNeededByPortableTransform(EncoderBuffer outBuffer) {
        return attributeOctahedronTransform.encodeParameters(outBuffer);
    }

    @Override
    public Status init(PointCloudEncoder encoder, int attributeId) {
        StatusChain chain = new StatusChain();

        if(super.init(encoder, attributeId).isError(chain)) return chain.get();
        // Currently this encoder works only for 3-component normal vectors.
        if(!this.getAttribute().getNumComponents().equals(3)) {
            return Status.dracoError("Currently this encoder works only for 3-component normal vectors.");
        }

        // Initialize AttributeOctahedronTransform.
        int quantizationBits = encoder.getOptions().getAttributeInt(attributeId, "quantization_bits", -1);
        if(quantizationBits < 1) {
            return Status.invalidParameter("Quantization bits must be greater than 0");
        }
        attributeOctahedronTransform.setParameters(quantizationBits);
        return Status.ok();
    }

    @Override
    protected Status prepareValues(CppVector<PointIndex> pointIds, int numPoints) {
        StatusChain chain = new StatusChain();

        PointAttribute portableAtt = attributeOctahedronTransform.initTransformedAttribute(
                this.getAttribute(), (int) pointIds.size());
        if(attributeOctahedronTransform.transformAttribute(
                this.getAttribute(), pointIds, portableAtt).isError(chain)) return chain.get();
        this.setPortableAttribute(portableAtt);
        return Status.ok();
    }

    @Override
    protected PSchemeTypedEncoderInterface<Integer, Integer> createIntPredictionScheme(PredictionSchemeMethod method) {
        int quantizationBits = this.getEncoder().getOptions().getAttributeInt(
                this.getAttributeId(), "quantization_bits", -1);
        int maxValue = (1 << quantizationBits) - 1;
        PSchemeNormalOctahedronCanonicalizedEncodingTransform<Integer> transform =
                new PSchemeNormalOctahedronCanonicalizedEncodingTransform<>(DataType.int32(), maxValue);
        PredictionSchemeMethod defaultPredictionMethod =
                PSchemeEncoderFactory.selectPredictionMethod(this.getAttributeId(), this.getEncoder());
        int predictionMethodInt = this.getEncoder().getOptions().getAttributeInt(
                this.getAttributeId(), "prediction_scheme", defaultPredictionMethod.getValue());
        PredictionSchemeMethod predictionMethod = PredictionSchemeMethod.valueOf(predictionMethodInt);

        if(predictionMethod == PredictionSchemeMethod.MESH_GEOMETRIC_NORMAL) {
            return PSchemeEncoderFactory.createPredictionSchemeForEncoder(
                    PredictionSchemeMethod.MESH_GEOMETRIC_NORMAL, this.getAttributeId(), this.getEncoder(), transform);
        }
        if(predictionMethod == PredictionSchemeMethod.DIFFERENCE) {
            return PSchemeEncoderFactory.createPredictionSchemeForEncoder(
                    PredictionSchemeMethod.DIFFERENCE, this.getAttributeId(), this.getEncoder(), transform);
        }
        throw new IllegalStateException("Should never be reached.");
    }
}
