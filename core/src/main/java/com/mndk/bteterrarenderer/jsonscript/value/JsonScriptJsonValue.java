package com.mndk.bteterrarenderer.jsonscript.value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import lombok.Data;

@Data
public class JsonScriptJsonValue implements JsonScriptValue {
    public static final JsonScriptJsonValue NULL = new JsonScriptJsonValue(NullNode.getInstance());

    private final JsonNode node;

    public String toString() {
        return this.node.toString();
    }
}
