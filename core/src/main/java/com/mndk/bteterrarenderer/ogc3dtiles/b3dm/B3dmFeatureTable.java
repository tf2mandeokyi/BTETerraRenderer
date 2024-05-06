package com.mndk.bteterrarenderer.ogc3dtiles.b3dm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mndk.bteterrarenderer.ogc3dtiles.Ogc3dTiles;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3;
import com.mndk.bteterrarenderer.ogc3dtiles.table.BinaryJsonTableElement;
import com.mndk.bteterrarenderer.ogc3dtiles.table.BinaryVector;
import lombok.*;

import javax.annotation.Nullable;

@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class B3dmFeatureTable {

    private final int batchLength;
    @Nullable private final Cartesian3 rtcCenter;

    public static B3dmFeatureTable from(String json, byte[] binary) throws JsonProcessingException {
        RawFeatureTableJson jsonParsed = Ogc3dTiles.jsonMapper().readValue(json, RawFeatureTableJson.class);
        int batchLength = jsonParsed.batchLength.getValue(binary);

        Cartesian3 rtcCenter = jsonParsed.rtcCenter == null ? null :
                Cartesian3.fromArray(jsonParsed.rtcCenter.getValue(binary).getElements());

        return new B3dmFeatureTable(batchLength, rtcCenter);
    }

    @Data
    static class RawFeatureTableJson {
        final BinaryJsonTableElement<Integer> batchLength;
        @Nullable final BinaryJsonTableElement<BinaryVector.Vec3<Float>> rtcCenter;

        public RawFeatureTableJson(
                @JsonProperty(value = "BATCH_LENGTH", required = true)
                BinaryJsonTableElement<Integer> batchLength,
                @Nullable @JsonProperty(value = "RTC_CENTER")
                BinaryJsonTableElement<BinaryVector.Vec3<Float>> rtcCenter
        ) {
            this.batchLength = batchLength;
            this.rtcCenter = rtcCenter;
        }
    }
}
