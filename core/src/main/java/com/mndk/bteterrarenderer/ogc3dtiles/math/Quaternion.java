package com.mndk.bteterrarenderer.ogc3dtiles.math;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.core.util.json.JsonParserUtil;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix3f;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@Data
@RequiredArgsConstructor
@JsonDeserialize(using = Quaternion.Deserializer.class)
public class Quaternion {
    private final float x, y, z, w;

    public Quaternion toNormalized() {
        float magnitude = (float) Math.sqrt(x*x + y*y + z*z + w*w);
        return new Quaternion(x / magnitude, y / magnitude, z / magnitude, w / magnitude);
    }

    public Matrix3f toRotationMatrix() {
        float tx = 2 * x, ty = 2 * y, tz = 2 * z;
        float twx = tx * w, twy = ty * w, twz = tz * w;
        float txx = tx * x, txy = ty * x, txz = tz * x;
        float tyy = ty * y, tyz = tz * y, tzz = tz * z;

        float[][] elements = new float[][] {
                { 1 - tyy - tzz,     txy - twz,     txz + twy },
                {     txy + twz, 1 - txx - tzz,     tyz - twx },
                {     txz - twy,     tyz + twx, 1 - txx - tyy }
        };
        return new Matrix3f((c, r) -> elements[r][c]);
    }

    public static Quaternion fromArray(float[] array) {
        return new Quaternion(array[0], array[1], array[2], array[3]);
    }

    public static Quaternion fromArray(double[] array) {
        return new Quaternion((float) array[0], (float) array[1], (float) array[2], (float) array[3]);
    }

    static class Deserializer extends JsonDeserializer<Quaternion> {
        @Override
        public Quaternion deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            double[] array = JsonParserUtil.readDoubleArray(p);
            return Quaternion.fromArray(array);
        }
    }
}
