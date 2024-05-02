package com.mndk.bteterrarenderer.jsonscript.exp.res;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.*;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;

import javax.annotation.Nonnull;
import java.util.List;

@JsonDeserialize
public class DeclareMultiVariableExpression implements JsonExpression {

    private final List<String> names;
    private final JsonExpression expression;

    @JsonCreator
    @JsonExpressionCreator
    public DeclareMultiVariableExpression(@JsonProperty(value = "names", required = true) List<String> names,
                                          @JsonProperty(value = "value", required = true) JsonExpression expression) {
        this.names = names;
        this.expression = expression;
    }

    @Nonnull
    @Override
    public ExpressionResult run(JsonScriptRuntime runtime) throws ExpressionRunException {
        ExpressionResult result = this.expression.run(runtime);
        if(result.isBreakType()) return result;

        JsonNode node = result.getValue().getAsJsonValue();
        if(node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            if(this.names.size() > arrayNode.size()) {
                throw runtime.exception("cannot decompose array: minimum length of " + this.names.size() +
                        " required but " + arrayNode.size() + " were given");
            }

            for(int i = 0; i < this.names.size(); i++) {
                String name = this.names.get(i);
                runtime.declareVariable(name, JsonScriptValue.json(arrayNode.get(i)));
            }
        }
        else if(node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            for(String name : this.names) {
                if(!objectNode.has(name)) {
                    throw runtime.exception("cannot decompose object: property \"" + name + "\" not found");
                }
                runtime.declareVariable(name, JsonScriptValue.json(objectNode.get(name)));
            }
        }
        else {
            throw runtime.exception("cannot decompose json type " + node.getNodeType());
        }

        return result;
    }
}
