package com.mndk.bteterrarenderer.ogc3dtiles.math.matrix;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.core.util.json.JsonParserUtil;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3f;

import java.io.IOException;

@JsonDeserialize(using = Matrix4f.Deserializer.class)
public class Matrix4f extends Matrixf {
    public static final Matrix4f IDENTITY = new Matrix4f(IDENTITY_FUNCTION);

    public Matrix4f(ColumnRowFunction columnRowFunction) {
        super(4, 4, columnRowFunction);
    }

    public static Matrix4f fromArray(double[] array, MatrixMajor matrixMajor) {
        if(matrixMajor == MatrixMajor.ROW) {
            return new Matrix4f((c, r) -> (float) array[r*4+c]);
        } else {
            return new Matrix4f((c, r) -> (float) array[c*4+r]);
        }
    }

    public static Matrix4f fromTranslation(Cartesian3f translation) {
        return new Matrix4f((c, r) -> {
            if(c == 3) switch (r) {
                case 0: return translation.getX();
                case 1: return translation.getY();
                case 2: return translation.getZ();
            }
            return r == c ? 1 : 0;
        });
    }

    public static Matrix4f fromScale(Cartesian3f scale) {
        return new Matrix4f((c, r) -> {
            if(r == c) switch (r) {
                case 0: return scale.getX();
                case 1: return scale.getY();
                case 2: return scale.getZ();
                case 3: return 1;
            }
            return 0;
        });
    }

    public static Matrix4f fromScaleMatrix(Matrix3f scaleMatrix) {
        return new Matrix4f((c, r) -> {
            if(r == 3 && c == 3) return 1;
            else if(r == 3 || c == 3) return 0;
            return scaleMatrix.get(c, r);
        });
    }

    static class Deserializer extends JsonDeserializer<Matrix4f> {
        @Override
        public Matrix4f deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            double[] array = JsonParserUtil.readDoubleArray(p);
            return Matrix4f.fromArray(array, MatrixMajor.COLUMN);
        }
    }
}
