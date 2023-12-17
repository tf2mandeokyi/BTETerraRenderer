package com.mndk.bteterrarenderer.ogc3dtiles.math.matrix;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.core.util.json.JsonParserUtil;

import java.io.IOException;

@JsonDeserialize(using = Matrix4.Deserializer.class)
public class Matrix4 extends Matrix {
    public static final Matrix4 IDENTITY = new Matrix4(IDENTITY_FUNCTION);

    public Matrix4(ColumnRowFunction columnRowFunction) {
        super(4, 4, columnRowFunction);
    }

    public static Matrix4 fromArray(double[] array, MatrixMajor matrixMajor) {
        if(matrixMajor == MatrixMajor.ROW) {
            return new Matrix4((c, r) -> array[r*4+c]);
        } else {
            return new Matrix4((c, r) -> array[c*4+r]);
        }
    }

    static class Deserializer extends JsonDeserializer<Matrix4> {
        @Override
        public Matrix4 deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            double[] array = JsonParserUtil.readDoubleArray(p);
            return Matrix4.fromArray(array, MatrixMajor.COLUMN);
        }
    }
}
