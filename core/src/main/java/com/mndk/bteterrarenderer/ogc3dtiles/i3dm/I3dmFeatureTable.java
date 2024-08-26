package com.mndk.bteterrarenderer.ogc3dtiles.i3dm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mndk.bteterrarenderer.ogc3dtiles.Ogc3dTiles;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidCoordinatesConverter;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Spheroid3;
import com.mndk.bteterrarenderer.ogc3dtiles.table.BinaryJsonTableElement;
import com.mndk.bteterrarenderer.ogc3dtiles.table.BinaryVector;
import com.mndk.bteterrarenderer.ogc3dtiles.util.QuantizationUtil;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class I3dmFeatureTable {

    @Nullable private final Cartesian3 rtcCenter;
    private final Instance[] instances;

    public static I3dmFeatureTable from(String json, byte[] binary, SpheroidCoordinatesConverter coordConverter)
            throws JsonProcessingException
    {
        RawFeatureTableJson jsonParsed = Ogc3dTiles.jsonMapper().readValue(json, RawFeatureTableJson.class);

        Cartesian3 rtcCenter = jsonParsed.globalRtcCenter == null ? null :
                Cartesian3.fromArray(jsonParsed.globalRtcCenter.getValue(binary).getElements());
        Cartesian3 quantizedVolumeOffset = jsonParsed.globalQuantizedVolumeOffset == null ? null :
                Cartesian3.fromArray(jsonParsed.globalQuantizedVolumeOffset.getValue(binary).getElements());
        Cartesian3 quantizedVolumeScale = jsonParsed.globalQuantizedVolumeScale == null ? null :
                Cartesian3.fromArray(jsonParsed.globalQuantizedVolumeScale.getValue(binary).getElements());
        boolean eastNorthUp = jsonParsed.globalEastNorthUp != null && jsonParsed.globalEastNorthUp.getValue(binary);

        int instanceLength = jsonParsed.globalInstancesLength.getValue(binary);
        Instance[] instances = new Instance[instanceLength];
        for(int i = 0; i < instanceLength; i++) {

            // Position vectors
            // "If both POSITION and POSITION_QUANTIZED are defined for an instance,
            //  the higher precision POSITION will be used."
            Cartesian3 position;
            if(jsonParsed.instancePosition != null) {
                position = Cartesian3.fromArray(jsonParsed.instancePosition.getValue(binary, i).getElements());
            }
            else if(jsonParsed.instanceQuantizedPosition != null) {
                if(quantizedVolumeOffset == null || quantizedVolumeScale == null) {
                    throw new RuntimeException("Malformed i3dm: quantized position exists, but global offset and/or scale doesn't");
                }

                Short[] quantized = jsonParsed.instanceQuantizedPosition.getValue(binary, i).getElements();
                float[] array = QuantizationUtil.normalizeShorts(quantized, false);
                position = Cartesian3.fromArray(array).scale(quantizedVolumeScale).add(quantizedVolumeOffset);
            }
            else throw new RuntimeException("Malformed i3dm: Nor position or quantized position for instances exist");

            // Normal vectors
            // "If NORMAL_UP, NORMAL_RIGHT, NORMAL_UP_OCT32P, and NORMAL_RIGHT_OCT32P are defined for an instance,
            //  the higher precision NORMAL_UP and NORMAL_RIGHT will be used."
            Cartesian3 normalUp = null, normalRight = null;
            if(jsonParsed.instanceNormalUp != null && jsonParsed.instanceNormalRight != null) {
                normalUp = Cartesian3.fromArray(jsonParsed.instanceNormalUp.getValue(binary, i).getElements());
                normalRight = Cartesian3.fromArray(jsonParsed.instanceNormalRight.getValue(binary, i).getElements());
            }
            else if(jsonParsed.instanceNormalUpOct32p != null && jsonParsed.instanceNormalRightOct32p != null) {
                float[] sNormUp = QuantizationUtil.sNormalizeShorts(
                        jsonParsed.instanceNormalUpOct32p.getValue(binary, i).getElements(), true);
                float[] sNormRight = QuantizationUtil.sNormalizeShorts(
                        jsonParsed.instanceNormalRightOct32p.getValue(binary, i).getElements(), true);

                normalUp = Cartesian3.fromOctEncoding(sNormUp[0], sNormUp[1]);
                normalRight = Cartesian3.fromOctEncoding(sNormRight[0], sNormRight[1]);
            }
            else if(eastNorthUp) {
                // Hacky implementation, maybe TODO add a test code for this?
                double epsilon = 1e-7;
                Spheroid3 spheroid3 = coordConverter.toSpheroid(position);
                Cartesian3 dphi = coordConverter.toCartesian(spheroid3.add(Spheroid3.fromRadians(epsilon, 0, 0)))
                        .subtract(position);
                Cartesian3 dh = coordConverter.toCartesian(spheroid3.add(Spheroid3.fromRadians(0, 0, epsilon)))
                        .subtract(position);
                normalUp = dh.scale(1 / epsilon).toNormalized();
                normalRight = dphi.scale(1 / epsilon).toNormalized();
            }

            Cartesian3 scaled = Cartesian3.UNIT_AXES;
            if(jsonParsed.instanceScale != null) {
                scaled = scaled.scale(jsonParsed.instanceScale.getValue(binary, i));
            }
            if(jsonParsed.instanceScaleNonUniform != null) {
                Cartesian3 scale = Cartesian3.fromArray(jsonParsed.instanceScaleNonUniform.getValue(binary, i).getElements());
                scaled = scaled.scale(scale);
            }

            int batchId = jsonParsed.instanceBatchId == null ? -1 : jsonParsed.instanceBatchId.getValue(binary, i);

            instances[i] = new Instance(batchId, position, scaled, normalUp, normalRight);
        }

        return new I3dmFeatureTable(rtcCenter, instances);
    }

    @SuppressWarnings("unused")
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Instance {
        /** Default value is -1 */
        private final int batchId;
        private final Cartesian3 position, scale;
        @Nullable
        private final Cartesian3 normalUp, normalRight;
    }

    @Data
    static class RawFeatureTableJson {
        final BinaryJsonTableElement<Integer> globalInstancesLength;
        @Nullable final BinaryJsonTableElement<BinaryVector.Vec3<Float>> globalRtcCenter;
        @Nullable final BinaryJsonTableElement<BinaryVector.Vec3<Float>> globalQuantizedVolumeOffset;
        @Nullable final BinaryJsonTableElement<BinaryVector.Vec3<Float>> globalQuantizedVolumeScale;
        @Nullable final BinaryJsonTableElement<Boolean> globalEastNorthUp;

        @Nullable final BinaryJsonTableElement<BinaryVector.Vec3<Float>> instancePosition;
        @Nullable final BinaryJsonTableElement<BinaryVector.Vec3<Short>> instanceQuantizedPosition;
        @Nullable final BinaryJsonTableElement<BinaryVector.Vec3<Float>> instanceNormalUp;
        @Nullable final BinaryJsonTableElement<BinaryVector.Vec3<Float>> instanceNormalRight;
        @Nullable final BinaryJsonTableElement<BinaryVector.Vec2<Short>> instanceNormalUpOct32p;
        @Nullable final BinaryJsonTableElement<BinaryVector.Vec2<Short>> instanceNormalRightOct32p;
        @Nullable final BinaryJsonTableElement<Float> instanceScale;
        @Nullable final BinaryJsonTableElement<BinaryVector.Vec3<Float>> instanceScaleNonUniform;
        @Nullable final BinaryJsonTableElement<Short> instanceBatchId; // Ugh

        public RawFeatureTableJson(
                @JsonProperty(value = "INSTANCES_LENGTH", required = true)
                BinaryJsonTableElement<Integer> globalInstancesLength,
                @Nullable @JsonProperty(value = "RTC_CENTER")
                BinaryJsonTableElement<BinaryVector.Vec3<Float>> globalRtcCenter,
                @Nullable @JsonProperty(value = "QUANTIZED_VOLUME_OFFSET")
                BinaryJsonTableElement<BinaryVector.Vec3<Float>> globalQuantizedVolumeOffset,
                @Nullable @JsonProperty(value = "QUANTIZED_VOLUME_SCALE")
                BinaryJsonTableElement<BinaryVector.Vec3<Float>> globalQuantizedVolumeScale,
                @Nullable @JsonProperty(value = "EAST_NORTH_UP")
                BinaryJsonTableElement<Boolean> globalEastNorthUp,

                @Nullable @JsonProperty(value = "POSITION")
                BinaryJsonTableElement<BinaryVector.Vec3<Float>> instancePosition,
                @Nullable @JsonProperty(value = "POSITION_QUANTIZED")
                BinaryJsonTableElement<BinaryVector.Vec3<Short>> instanceQuantizedPosition,
                @Nullable @JsonProperty(value = "NORMAL_UP")
                BinaryJsonTableElement<BinaryVector.Vec3<Float>> instanceNormalUp,
                @Nullable @JsonProperty(value = "NORMAL_RIGHT")
                BinaryJsonTableElement<BinaryVector.Vec3<Float>> instanceNormalRight,
                @Nullable @JsonProperty(value = "NORMAL_UP_OCT32P")
                BinaryJsonTableElement<BinaryVector.Vec2<Short>> instanceNormalUpOct32p,
                @Nullable @JsonProperty(value = "NORMAL_RIGHT_OCT32P")
                BinaryJsonTableElement<BinaryVector.Vec2<Short>> instanceNormalRightOct32p,
                @Nullable @JsonProperty(value = "SCALE")
                BinaryJsonTableElement<Float> instanceScale,
                @Nullable @JsonProperty(value = "SCALE_NON_UNIFORM")
                BinaryJsonTableElement<BinaryVector.Vec3<Float>> instanceScaleNonUniform,
                @Nullable @JsonProperty(value = "BATCH_ID")
                BinaryJsonTableElement<Short> instanceBatchId
        ) {
            this.globalInstancesLength = globalInstancesLength;
            this.globalRtcCenter = globalRtcCenter;
            this.globalQuantizedVolumeOffset = globalQuantizedVolumeOffset;
            this.globalQuantizedVolumeScale = globalQuantizedVolumeScale;
            this.globalEastNorthUp = globalEastNorthUp;

            this.instancePosition = instancePosition;
            this.instanceQuantizedPosition = instanceQuantizedPosition;
            this.instanceNormalUp = instanceNormalUp;
            this.instanceNormalRight = instanceNormalRight;
            this.instanceNormalUpOct32p = instanceNormalUpOct32p;
            this.instanceNormalRightOct32p = instanceNormalRightOct32p;
            this.instanceScale = instanceScale;
            this.instanceScaleNonUniform = instanceScaleNonUniform;
            this.instanceBatchId = instanceBatchId;
        }
    }
}
