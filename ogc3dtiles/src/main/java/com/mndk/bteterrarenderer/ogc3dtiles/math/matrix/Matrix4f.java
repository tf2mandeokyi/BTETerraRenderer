package com.mndk.bteterrarenderer.ogc3dtiles.math.matrix;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3f;
import com.mndk.bteterrarenderer.util.json.JsonParserUtil;

import java.io.IOException;

@JsonDeserialize(using = Matrix4f.Deserializer.class)
public class Matrix4f extends Matrixf {
    public static final Matrix4f IDENTITY = new Matrix4f(IDENTITY_FUNCTION);

    public Matrix4f(ColumnRowFunction columnRowFunction) {
        super(4, 4, columnRowFunction);
    }

    public Matrix4f multiply(Matrix4f other) { return super.multiply(other).toMatrix4(); }
    @Override public Matrix4f inverse() {
        Matrixf inverse = super.inverse();
        return inverse == null ? null : inverse.toMatrix4();
    }
    @Override public Matrix4f toMatrix4() { return this; }

    public Cartesian3f[] getScaleRowVectors() {
        Cartesian3f sx = new Cartesian3f(this.get(0, 0), this.get(0, 1), this.get(0, 2));
        Cartesian3f sy = new Cartesian3f(this.get(1, 0), this.get(1, 1), this.get(1, 2));
        Cartesian3f sz = new Cartesian3f(this.get(2, 0), this.get(2, 1), this.get(2, 2));
        return new Cartesian3f[] { sx, sy, sz };
    }

    public static Matrix4f fromArray(double[] array, MatrixMajor matrixMajor) {
        if (matrixMajor == MatrixMajor.ROW) {
            return new Matrix4f((c, r) -> (float) array[r*4+c]);
        } else {
            return new Matrix4f((c, r) -> (float) array[c*4+r]);
        }
    }

    public static Matrix4f fromTranslation(Cartesian3f translation) {
        return new Matrix4f((c, r) -> {
            if (c != 3) return r == c ? 1 : 0;
            switch (r) {
                case 0: return translation.getX();
                case 1: return translation.getY();
                case 2: return translation.getZ();
                default: return 1;
            }
        });
    }

    public static Matrix4f fromScale(Cartesian3f scale) {
        return new Matrix4f((c, r) -> {
            if (r != c) return 0;
            switch (r) {
                case 0: return scale.getX();
                case 1: return scale.getY();
                case 2: return scale.getZ();
                default: return 1;
            }
        });
    }

    public static Matrix4f fromScaleMatrix(Matrix3f scaleMatrix) {
        return new Matrix4f((c, r) -> {
            if (r == 3 && c == 3) return 1;
            else if (r == 3 || c == 3) return 0;
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
