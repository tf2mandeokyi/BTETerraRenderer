package com.mndk.bteterrarenderer.draco.compression;

import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.compression.config.EncoderOptions;
import com.mndk.bteterrarenderer.draco.compression.config.MeshEncoderMethod;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.compression.mesh.MeshEdgebreakerEncoder;
import com.mndk.bteterrarenderer.draco.compression.mesh.MeshEncoder;
import com.mndk.bteterrarenderer.draco.compression.mesh.MeshSequentialEncoder;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import com.mndk.bteterrarenderer.draco.pointcloud.PointCloud;

public class DracoExpertEncoder extends DracoEncoderBase<EncoderOptions> {

    private final PointCloud pointCloud;
    private final Mesh mesh;

    public DracoExpertEncoder(PointCloud pointCloud) {
        this.pointCloud = pointCloud;
        this.mesh = null;
    }

    public DracoExpertEncoder(Mesh mesh) {
        this.pointCloud = mesh;
        this.mesh = mesh;
    }

    public Status encodeToBuffer(EncoderBuffer outBuffer) {
        if (pointCloud == null) return Status.dracoError("Invalid input geometry.");
        if (mesh == null) return this.encodePointCloudToBuffer(pointCloud, outBuffer);
        return this.encodeMeshToBuffer(mesh, outBuffer);
    }

    public void setAttributeQuantization(int attributeId, int quantizationBits) {
        this.getOptions().setAttributeInt(attributeId, "quantization_bits", quantizationBits);
    }

    public void setAttributeExplicitQuantization(int attributeId, int quantizationBits, int numDims,
                                                 float[] origin, float range) {
        this.getOptions().setAttributeInt(attributeId, "quantization_bits", quantizationBits);
        this.getOptions().setAttributeVector(attributeId, "quantization_origin", numDims, Pointer.wrap(origin));
        this.getOptions().setAttributeFloat(attributeId, "quantization_range", range);
    }

    public void setUseBuiltInAttributeCompression(boolean enabled) {
        this.getOptions().setGlobalBool("use_built_in_attribute_compression", enabled);
    }

    public void setEncodingMethod(int encodingMethod) {
        super.setEncodingMethod(encodingMethod);
    }

    public void setEncodingSubmethod(int encodingSubmethod) {
        super.setEncodingSubmethod(encodingSubmethod);
    }

    public void setSpeedOptions(int encodingSpeed, int decodingSpeed) {
        super.setSpeedOptions(encodingSpeed, decodingSpeed);
    }

    public Status setAttributePredictionScheme(int attributeId, PredictionSchemeMethod predictionSchemeMethod) {
        PointAttribute att = pointCloud.getAttribute(attributeId);
        GeometryAttribute.Type attType = att.getAttributeType();
        Status status = checkPredictionScheme(attType, predictionSchemeMethod);
        if (status.isError()) return status;
        this.getOptions().setAttributeInt(attributeId, "prediction_scheme", predictionSchemeMethod.getValue());
        return status;
    }

    private Status encodePointCloudToBuffer(PointCloud pc, EncoderBuffer outBuffer) {
        // TODO: Implement this
        return Status.dracoError("Point cloud encoding is not enabled.");
    }

    private Status encodeMeshToBuffer(Mesh m, EncoderBuffer outBuffer) {
        MeshEncoder encoder;
        MeshEncoderMethod encodingMethod = this.getOptions()
                .getGlobalEnum("encoding_method", MeshEncoderMethod::valueOf, null);
        if (encodingMethod == null) {
            if (this.getOptions().getSpeed() == 10) {
                encodingMethod = MeshEncoderMethod.SEQUENTIAL;
            } else {
                encodingMethod = MeshEncoderMethod.EDGEBREAKER;
            }
        }
        if (encodingMethod == MeshEncoderMethod.EDGEBREAKER) {
            encoder = new MeshEdgebreakerEncoder();
        } else {
            encoder = new MeshSequentialEncoder();
        }
        encoder.setMesh(m);

        Status status = encoder.encode(this.getOptions(), outBuffer);
        if(status.isError()) return status;

        this.setNumEncodedPoints(encoder.getNumEncodedPoints());
        this.setNumEncodedFaces(encoder.getNumEncodedFaces());
        return Status.ok();
    }

    @Override protected EncoderOptions createDefaultOptions() {
        return EncoderOptions.createDefaultOptions();
    }
}
