package com.mndk.bteterrarenderer.jsonscript.value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
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

    @Override
    public JsonScriptValueType getType() {
        if     (node instanceof BigIntegerNode) return JsonScriptValueType.INT;
        else if(node instanceof DecimalNode)    return JsonScriptValueType.FLOAT;
        else if(node instanceof TextNode)       return JsonScriptValueType.STRING;
        else if(node instanceof BooleanNode)    return JsonScriptValueType.BOOLEAN;
        else if(node instanceof ArrayNode)      return JsonScriptValueType.ARRAY;
        else if(node instanceof ObjectNode)     return JsonScriptValueType.OBJECT;
        else if(node instanceof NullNode)       return JsonScriptValueType.NULL;
        else throw new IllegalArgumentException("unknown node type: " + node.getNodeType());
    }
}
