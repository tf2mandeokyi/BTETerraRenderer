package com.mndk.bteterrarenderer.connector.terraplusplus.projection;

import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraplusplus.dep.com.fasterxml.jackson.core.JsonGenerator;
import net.buildtheearth.terraplusplus.dep.com.fasterxml.jackson.core.JsonParser;
import net.buildtheearth.terraplusplus.dep.com.fasterxml.jackson.databind.DeserializationContext;
import net.buildtheearth.terraplusplus.dep.com.fasterxml.jackson.databind.JsonDeserializer;
import net.buildtheearth.terraplusplus.dep.com.fasterxml.jackson.databind.JsonSerializer;
import net.buildtheearth.terraplusplus.dep.com.fasterxml.jackson.databind.SerializerProvider;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;

import java.io.IOException;

@RequiredArgsConstructor
public class IGeographicProjectionImpl implements IGeographicProjection {
    private final GeographicProjection delegate;

    public double[] toGeo(double x, double y) throws Exception {
        return delegate.toGeo(x, y);
    }

    public double[] fromGeo(double longitude, double latitude) throws Exception {
        return delegate.fromGeo(longitude, latitude);
    }

    public static class Serializer extends JsonSerializer<IGeographicProjection> {
        private static final GeographicProjection.Serializer GP_SERIALIZER = new GeographicProjection.Serializer();

        @Override
        public void serialize(IGeographicProjection p, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            GeographicProjection delegate = ((IGeographicProjectionImpl) p).delegate;
            GP_SERIALIZER.serialize(delegate, jsonGenerator, serializerProvider);
        }
    }

    public static class Deserializer extends JsonDeserializer<IGeographicProjection> {
        private static final GeographicProjection.Deserializer GP_DESERIALIZER = new GeographicProjection.Deserializer();

        @Override
        public IGeographicProjection deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new IGeographicProjectionImpl(GP_DESERIALIZER.deserialize(p, ctxt));
        }
    }

    public void registerSerializers() {

    }
}
