package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.number.UShort;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.config.EncodedGeometryType;
import com.mndk.bteterrarenderer.draco.compression.config.EncoderOptions;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.compression.mesh.MeshEncoder;
import com.mndk.bteterrarenderer.draco.compression.pointcloud.PointCloudEncoder;
import com.mndk.bteterrarenderer.draco.pointcloud.PointCloud;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PSchemeEncoderFactory {

    public PredictionSchemeMethod selectPredictionMethod(int attId, PointCloudEncoder encoder) {
        return selectPredictionMethod(attId, encoder.getOptions(), encoder);
    }

    public PredictionSchemeMethod selectPredictionMethod(int attId, EncoderOptions options, PointCloudEncoder encoder) {
        if(options.getSpeed() >= 10) {
            return PredictionSchemeMethod.DIFFERENCE;
        }
        if(encoder.getGeometryType() == EncodedGeometryType.TRIANGULAR_MESH) {
            int attQuant = options.getAttributeInt(attId, "quantization_bits", -1);
            PointCloud pointCloud = encoder.getPointCloud();
            PointAttribute att = pointCloud.getAttribute(attId);
            if(attQuant != -1
                    && att.getAttributeType() == GeometryAttribute.Type.TEX_COORD
                    && att.getNumComponents().equals(2)) {
                PointAttribute posAtt = pointCloud.getNamedAttribute(GeometryAttribute.Type.POSITION);
                boolean isPosAttValid = false;
                if(posAtt != null) {
                    if(posAtt.getDataType().isDataTypeIntegral()) {
                        isPosAttValid = true;
                    } else {
                        int posAttId = pointCloud.getNamedAttributeId(GeometryAttribute.Type.POSITION);
                        int posQuant = options.getAttributeInt(posAttId, "quantization_bits", -1);
                        if(posQuant > 0 && posQuant <= 21 && 2 * posQuant + attQuant < 64) {
                            isPosAttValid = true;
                        }
                    }
                }
                if(isPosAttValid && options.getSpeed() < 4) {
                    return PredictionSchemeMethod.MESH_TEX_COORDS_PORTABLE;
                }
            }
            if(att.getAttributeType() == GeometryAttribute.Type.NORMAL) {
                if(options.getSpeed() < 4) {
                    PointAttribute posAtt = pointCloud.getNamedAttribute(GeometryAttribute.Type.POSITION);
                    if(posAtt != null && (posAtt.getDataType().isDataTypeIntegral() ||
                            options.getAttributeInt(pointCloud.getNamedAttributeId(GeometryAttribute.Type.POSITION),
                                    "quantization_bits", -1) > 0)) {
                        return PredictionSchemeMethod.MESH_GEOMETRIC_NORMAL;
                    }
                }
                return PredictionSchemeMethod.DIFFERENCE; // default
            }
            if(options.getSpeed() >= 8) {
                return PredictionSchemeMethod.DIFFERENCE;
            }
            if(options.getSpeed() >= 2 || pointCloud.getNumPoints() < 40) {
                return PredictionSchemeMethod.MESH_PARALLELOGRAM;
            }
            return PredictionSchemeMethod.MESH_CONSTRAINED_MULTI_PARALLELOGRAM;
        }
        return PredictionSchemeMethod.DIFFERENCE;
    }

    public <DataT, CorrT> PSchemeEncoder<DataT, CorrT>
    createPredictionSchemeForEncoder(PredictionSchemeMethod method, int attId, PointCloudEncoder encoder,
                                     PSchemeEncodingTransform<DataT, CorrT> transform) {
        PointAttribute att = encoder.getPointCloud().getAttribute(attId);
        if(method == PredictionSchemeMethod.UNDEFINED) {
            method = selectPredictionMethod(attId, encoder);
        }
        if(method == PredictionSchemeMethod.NONE) {
            return null;
        }
        if(encoder.getGeometryType() == EncodedGeometryType.TRIANGULAR_MESH) {
            MeshEncoder meshEncoder = (MeshEncoder) encoder;
            UShort bitStreamVersion = UShort.of(DracoVersions.MESH_BIT_STREAM_VERSION);
            PSchemeEncoder<DataT, CorrT> ret = PSchemeFactory.createMeshPredictionScheme(
                    new MPSchemeEncoderFactory<>(), meshEncoder, method, attId, transform,
                    bitStreamVersion);
            if(ret != null) return ret;
            // Otherwise try to create another prediction scheme.
        }
        // Create delta encoder.
        return new PSchemeDeltaEncoder<>(att, transform);
    }

    public PredictionSchemeMethod getPredictionMethodFromOptions(int attId, EncoderOptions options) {
        int predType = options.getAttributeInt(attId, "prediction_scheme", -1);
        if (predType == -1) {
            return PredictionSchemeMethod.UNDEFINED;
        }
        if (predType < 0 || predType >= PredictionSchemeMethod.NUM_PREDICTION_SCHEMES) {
            return PredictionSchemeMethod.NONE;
        }
        return PredictionSchemeMethod.valueOf(predType);
    }
}
