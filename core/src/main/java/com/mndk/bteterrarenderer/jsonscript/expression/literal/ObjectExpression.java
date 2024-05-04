package com.mndk.bteterrarenderer.jsonscript.expression.literal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mndk.bteterrarenderer.jsonscript.JsonScript;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionCallerInfo;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.expression.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.expression.ResultTransformer;

import javax.annotation.Nonnull;
import java.util.Map;

@JsonDeserialize
public class ObjectExpression extends JsonExpression {

    private static final ExpressionCallerInfo INFO = new ExpressionCallerInfo(ListExpression.class);

    private final Map<String, JsonExpression> values;

    @JsonCreator
    public ObjectExpression(Map<String, JsonExpression> values) {
        this.values = values;
    }

    @Nonnull
    @Override
    protected ExpressionResult runInternal(JsonScriptRuntime runtime) {
        ObjectNode objectResult = JsonScript.jsonMapper().createObjectNode();

        for(Map.Entry<String, JsonExpression> entry : this.values.entrySet()) {
            String key = entry.getKey();
            ExpressionCallerInfo info = INFO.add("key '" + key + "'");

            ResultTransformer.JNode transformer = entry.getValue().run(runtime, info)
                    .transformer()
                    .asJsonValue("value must be a json type", info)
                    .asNode();
            if(transformer.isBreakType()) return transformer.getResult();

            JsonNode node = transformer.getWrapped();
            objectResult.set(key, node);
        }

        return ExpressionResult.ok(objectResult);
    }
}