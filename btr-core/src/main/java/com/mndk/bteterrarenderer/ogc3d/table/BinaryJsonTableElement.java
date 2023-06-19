package com.mndk.bteterrarenderer.ogc3d.table;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.mndk.bteterrarenderer.util.BtrUtil;
import com.mndk.bteterrarenderer.util.JsonParserUtil;
import lombok.ToString;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

@ToString
@JsonDeserialize(using = BinaryJsonTableElement.Deserializer.class)
public abstract class BinaryJsonTableElement<T> {

    public final T getValue(byte[] binaryData) {
        return this.getValue(binaryData, 0);
    }

    public abstract T getValue(byte[] binaryData, int additionalOffset);

    static class Deserializer extends JsonDeserializer<BinaryJsonTableElement<?>> implements ContextualDeserializer {
        private JavaType valueType;

        @Override
        public BinaryJsonTableElement<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            Class<?> valueClass = valueType == null ? null : valueType.getRawClass();

            JsonToken token = p.currentToken();
            if(token == JsonToken.START_OBJECT) {
                BinaryValue<?> result = new BinaryValue<>();
                JsonNode node = ctxt.readTree(p);

                result.byteOffset = node.get("byteOffset").asInt();

                if(node.has("type")) {
                    result.type = BinaryType.valueOf(node.get("type").asText());
                } else {
                    result.type = BinaryType.valueOf(valueClass);
                }

                if(node.has("componentType")) {
                    result.componentType = BinaryComponentType.valueOf(node.get("componentType").asText());
                } else {
                    JavaType componentType = valueType;
                    if(result.type.isVector()) {
                        componentType = componentType.containedType(0);
                    }
                    result.componentType = BinaryComponentType.valueOf(componentType.getRawClass(), false);
                }
                return result;
            }

            Object content;
            if(token == JsonToken.START_ARRAY) {
                BinaryType type = BinaryType.valueOf(valueClass);
                if(!type.isVector()) {
                    content = valueType == null ?
                            p.readValueAs(new TypeReference<List<Object>>() {}) :
                            ctxt.readValue(p, valueType);
                } else {
                    JavaType innerType = valueType.containedType(0);
                    List<Object> list = JsonParserUtil.readJsonList(p, false, p1 -> ctxt.readValue(p1, innerType));
                    content = type.getGenerator().apply(BtrUtil.uncheckedCast(list.toArray(new Object[0])));
                }
            }
            else {
                content = valueType == null ? p.readValueAs(Object.class) : ctxt.readValue(p, valueType);
            }
            return new Value<>(content);
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
            JavaType wrapperType = property != null ? property.getType() : ctxt.getContextualType();
            if(wrapperType == null) return new Deserializer();

            JavaType valueType = wrapperType.containedType(0);
            Deserializer deserializer = new Deserializer();
            deserializer.valueType = valueType;
            return deserializer;
        }
    }

    @ToString
    static class Value<T> extends BinaryJsonTableElement<T> {
        public Object content;

        public Value(Object content) {
            this.content = content;
        }

        public T getValue(byte[] binaryData, int additionalOffset) {
            return BtrUtil.uncheckedCast(content);
        }
    }

    @ToString
    static class BinaryValue<T> extends BinaryJsonTableElement<T> {
        public int byteOffset;
        public BinaryType type;
        public BinaryComponentType componentType;

        @Override
        public T getValue(byte[] binaryData, int additionalOffset) {
            ByteBuffer buffer = ByteBuffer.wrap(binaryData);
            buffer.position(byteOffset + additionalOffset);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            return BtrUtil.uncheckedCast(type.readBinary(buffer, componentType));
        }
    }

}
