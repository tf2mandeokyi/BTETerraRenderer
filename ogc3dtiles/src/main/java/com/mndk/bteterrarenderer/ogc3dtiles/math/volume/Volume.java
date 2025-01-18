package com.mndk.bteterrarenderer.ogc3dtiles.math.volume;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.ogc3dtiles.math.BoundingSphere;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Plane;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidCoordinatesConverter;
import com.mndk.bteterrarenderer.util.json.JsonParserUtil;
import org.joml.Matrix4d;

import java.io.IOException;

@JsonDeserialize(using = Volume.Deserializer.class)
public abstract class Volume {

    public abstract boolean intersectsPositiveSides(Plane[] planes, Matrix4d thisTransform,
                                                    SpheroidCoordinatesConverter converter);

    public abstract BoundingSphere getLevelOfDetailSphere(Matrix4d thisTransform, SpheroidCoordinatesConverter converter);

    static class Deserializer extends JsonDeserializer<Volume> {

        @Override
        public Volume deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String fieldName = p.nextFieldName();
            if (fieldName == null) {
                throw JsonMappingException.from(p, "expected volume type name, found: " + p.currentToken());
            }

            Volume result;
            p.nextToken();
            double[] array = JsonParserUtil.readDoubleArray(p);
            switch (fieldName) {
                case "region": result = Region.fromArray(array); break;
                case "box": result = Parallelepiped.fromArray(array); break;
                case "sphere": result = Ellipsoid.fromArray(array); break;
                default: throw JsonMappingException.from(p, "unknown volume type: " + fieldName);
            }

            if (p.nextToken() != JsonToken.END_OBJECT) {
                throw JsonMappingException.from(p, "expected json object end, but found: " + p.currentToken());
            }

            return result;
        }
    }
}
