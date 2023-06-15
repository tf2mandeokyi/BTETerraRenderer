package com.mndk.bteterrarenderer.ogc3d.format.b3dm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.ogc3d.format.table.BinaryJsonTableElement;
import com.mndk.bteterrarenderer.ogc3d.format.table.BinaryVector;
import com.mndk.bteterrarenderer.ogc3d.math.Cartesian3;
import lombok.*;

import javax.annotation.Nullable;

@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class B3dmFeatureTable {

    private final int batchLength;
    @Nullable
    private final Cartesian3 rtcCenter;

    public static B3dmFeatureTable from(String json, byte[] binary) throws JsonProcessingException {
        RawFeatureTableJson jsonParsed =
                BTETerraRendererConstants.JSON_MAPPER.readValue(json, RawFeatureTableJson.class);
        int batchLength = jsonParsed.batchLength.getValue(binary);

        Cartesian3 rtcCenter;
        if(jsonParsed.rtcCenter == null) {
            rtcCenter = null;
        }
        else {
            double[] rtcArray = new double[3];
            Float[] rtcFloatArray = jsonParsed.rtcCenter.getValue(binary).getElements();
            for (int i = 0; i < 3; i++) {
                rtcArray[i] = rtcFloatArray[i];
            }
            rtcCenter = Cartesian3.fromArray(rtcArray);
        }
        return new B3dmFeatureTable(batchLength, rtcCenter);
    }

    @Data
    static class RawFeatureTableJson {
        BinaryJsonTableElement<Integer> batchLength;
        @Nullable
        BinaryJsonTableElement<BinaryVector.Vec3<Float>> rtcCenter;

        public RawFeatureTableJson(
                @JsonProperty(value = "BATCH_LENGTH", required = true) BinaryJsonTableElement<Integer> batchLength,
                @Nullable @JsonProperty(value = "RTC_CENTER") BinaryJsonTableElement<BinaryVector.Vec3<Float>> rtcCenter
        ) {
            this.batchLength = batchLength;
            this.rtcCenter = rtcCenter;
        }
    }

}
