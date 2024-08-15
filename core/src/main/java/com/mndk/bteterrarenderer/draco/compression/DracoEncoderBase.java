package com.mndk.bteterrarenderer.draco.compression;

import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.compression.config.EncoderOptionsBase;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.core.Status;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class DracoEncoderBase<Op extends EncoderOptionsBase<?>> {

    private Op options;
    private int numEncodedPoints;
    private int numEncodedFaces;

    public DracoEncoderBase() {
        this.options = this.createDefaultOptions();
        this.numEncodedPoints = 0;
        this.numEncodedFaces = 0;
    }

    protected abstract Op createDefaultOptions();

    public void setTrackEncodedProperties(boolean flag) {
        options.setGlobalBool("store_number_of_encoded_points", flag);
        options.setGlobalBool("store_number_of_encoded_faces", flag);
    }

    protected void reset(Op options) { this.options = options; }
    protected void reset() { this.options = this.createDefaultOptions(); }

    protected void setSpeedOptions(int encodingSpeed, int decodingSpeed) {
        options.setSpeed(encodingSpeed, decodingSpeed);
    }

    protected void setEncodingMethod(int encodingMethod) {
        options.setGlobalInt("encoding_method", encodingMethod);
    }

    protected void setEncodingSubmethod(int encodingSubmethod) {
        options.setGlobalInt("encoding_submethod", encodingSubmethod);
    }

    protected Status checkPredictionScheme(GeometryAttribute.Type attType, PredictionSchemeMethod predictionScheme) {
        if (predictionScheme == null) {
            return Status.dracoError("Invalid prediction scheme requested.");
        }
        // Deprecated prediction schemes:
        if(predictionScheme == PredictionSchemeMethod.MESH_TEX_COORDS_DEPRECATED) {
            return Status.dracoError("MESH_PREDICTION_TEX_COORDS_DEPRECATED is deprecated.");
        }
        if(predictionScheme == PredictionSchemeMethod.MESH_MULTI_PARALLELOGRAM) {
            return Status.dracoError("MESH_PREDICTION_MULTI_PARALLELOGRAM is deprecated.");
        }
        // Attribute specific checks:
        if(predictionScheme == PredictionSchemeMethod.MESH_TEX_COORDS_PORTABLE) {
            if(attType != GeometryAttribute.Type.TEX_COORD) {
                return Status.dracoError("Invalid prediction scheme for attribute type.");
            }
        }
        if(predictionScheme == PredictionSchemeMethod.MESH_GEOMETRIC_NORMAL) {
            if(attType != GeometryAttribute.Type.NORMAL) {
                return Status.dracoError("Invalid prediction scheme for attribute type.");
            }
        }
        if(attType == GeometryAttribute.Type.NORMAL) {
            if(!(predictionScheme == PredictionSchemeMethod.DIFFERENCE ||
                    predictionScheme == PredictionSchemeMethod.MESH_GEOMETRIC_NORMAL)) {
                return Status.dracoError("Invalid prediction scheme for attribute type.");
            }
        }
        return Status.ok();
    }

}
