package com.mndk.bteterrarenderer.jsonscript.value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.mndk.bteterrarenderer.core.util.json.JsonParserUtil;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JsonScriptJsonValue implements JsonScriptValue {
    public static final JsonScriptJsonValue NULL = new JsonScriptJsonValue(NullNode.getInstance());

    private final JsonNode node;

    public JsonNode getNode() {
        return JsonParserUtil.toBiggerPrimitiveNode(node);
    }

    public String toString() {
        return this.node.toString();
    }
}
