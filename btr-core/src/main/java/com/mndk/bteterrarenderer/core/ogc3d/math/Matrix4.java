package com.mndk.bteterrarenderer.core.ogc3d.math;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.core.util.JsonParserUtil;

import java.io.IOException;
import java.util.Arrays;

@JsonDeserialize(using = Matrix4.Deserializer.class)
public class Matrix4 extends MatrixN {
    public static final Matrix4 IDENTITY = new Matrix4((r, c) -> r == c ? 1 : 0);

    public Matrix4(ElementFunction elementFunction) {
        super(4, elementFunction);
    }

    public static Matrix4 fromArray(double[] array, MatrixMajor matrixMajor) {
        if(matrixMajor == MatrixMajor.ROW) {
            return new Matrix4((r, c) -> array[r*4+c]);
        } else {
            return new Matrix4((r, c) -> array[c*4+r]);
        }
    }

    public static Matrix4 fromArray(double[] array, int start, MatrixMajor matrixMajor) {
        return fromArray(Arrays.copyOfRange(array, start, start+16), matrixMajor);
    }

    static class Deserializer extends JsonDeserializer<Matrix4> {
        @Override
        public Matrix4 deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            double[] array = JsonParserUtil.readDoubleArray(p, false);
            return Matrix4.fromArray(array, MatrixMajor.COLUMN);
        }
    }
}
