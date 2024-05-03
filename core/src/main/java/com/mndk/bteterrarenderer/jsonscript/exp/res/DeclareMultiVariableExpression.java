package com.mndk.bteterrarenderer.jsonscript.exp.res;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptScope;
import com.mndk.bteterrarenderer.jsonscript.exp.*;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptJsonValue;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;

import javax.annotation.Nonnull;
import java.util.List;

@JsonDeserialize
public class DeclareMultiVariableExpression extends JsonExpression {

    private final List<String> names;
    private final JsonExpression expression;
    private final ExpressionCallerInfo info;

    @JsonCreator
    @JsonExpressionCreator
    public DeclareMultiVariableExpression(@JsonProperty(value = "names", required = true) List<String> names,
                                          @JsonProperty(value = "value", required = true) JsonExpression expression) {
        this.names = names;
        this.expression = expression;
        this.info = new ExpressionCallerInfo(this, this.names.toString());
    }

    @Nonnull
    @Override
    public ExpressionResult runInternal(JsonScriptRuntime runtime) {
        ExpressionResult result = this.expression.run(runtime, this.info);
        if(result.isBreakType()) return result;

        JsonScriptScope currentScope = runtime.getCurrentScope();
        JsonScriptValue value = result.getValue();
        if(!(value instanceof JsonScriptJsonValue)) {
            return ExpressionResult.error("value must be a json type", this.info);
        }

        JsonNode node = ((JsonScriptJsonValue) value).getNode();
        if(node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            if(this.names.size() > arrayNode.size()) {
                return ExpressionResult.error("cannot decompose array: minimum length of " + this.names.size() +
                        " required but " + arrayNode.size() + " were given", this.info);
            }

            for(int i = 0; i < this.names.size(); i++) {
                String name = this.names.get(i);
                currentScope.declareVariable(name, JsonScriptValue.json(arrayNode.get(i)));
            }
        }
        else if(node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            for(String name : this.names) {
                if(!objectNode.has(name)) {
                    return ExpressionResult.error("cannot decompose object: " +
                            "property \"" + name + "\" not found", this.info);
                }
                currentScope.declareVariable(name, JsonScriptValue.json(objectNode.get(name)));
            }
        }
        else {
            return ExpressionResult.error("cannot decompose json type " + node.getNodeType(), this.info);
        }

        return result;
    }
}
