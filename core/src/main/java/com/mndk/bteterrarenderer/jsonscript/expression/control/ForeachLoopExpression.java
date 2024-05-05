package com.mndk.bteterrarenderer.jsonscript.expression.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mndk.bteterrarenderer.jsonscript.JsonScript;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptScope;
import com.mndk.bteterrarenderer.jsonscript.expression.*;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@JsonDeserialize
public class ForeachLoopExpression extends JsonExpression {

    private static final ExpressionCallerInfo INFO = new ExpressionCallerInfo(ForeachLoopExpression.class);
    private static final ExpressionCallerInfo ITERATOR_INFO = INFO.add("initial statement");

    @Nullable
    private final String label;
    private final String name;
    private final JsonExpression iterable;
    private final JsonExpression content;

    @JsonCreator
    @ArrayArgumentAcceptable
    public ForeachLoopExpression(@Nullable @JsonProperty(value = "label") String label,
                                 @JsonProperty(value = "name", required = true) String name,
                                 @JsonProperty(value = "iterable", required = true) JsonExpression iterable,
                                 @JsonProperty(value = "content", required = true) JsonExpression content) {
        this.label = label;
        this.name = name;
        this.iterable = iterable;
        this.content = content;
    }

    @Nonnull
    @Override
    protected ExpressionResult runInternal(JsonScriptRuntime runtime) {
        ResultTransformer.JNode iterableTransformer = this.iterable.run(runtime, ITERATOR_INFO)
                .transformer()
                .asJsonValue(ErrorMessages.valueMustBeJson("iterator"), ITERATOR_INFO)
                .asNode();
        if(iterableTransformer.isBreakType()) return iterableTransformer.getResult();

        JsonNode iterable = iterableTransformer.getWrapped();
        List<JsonNode> split = splitIterable(iterable);
        if(split == null) {
            return ExpressionResult.error("iterable type must be array or object, " +
                    "instead was " + iterable.getNodeType(), ITERATOR_INFO);
        }

        for(JsonNode element : split) {
            try {
                JsonScriptScope loopScope = runtime.pushScope();
                loopScope.declareVariable(this.name, JsonScriptValue.json(element));

                ExpressionResult result = this.content.run(runtime, INFO);
                if(result.isLoopBreak(this.label)) break;
                else if(!result.isLoopContinue(this.label) && result.isBreakType()) {
                    return result;
                }
            } finally {
                runtime.popScope();
            }
        }

        return ExpressionResult.ok();
    }

    @Nullable
    private static List<JsonNode> splitIterable(JsonNode node) {

        if(node.isArray()) {
            List<JsonNode> result = new ArrayList<>();

            ArrayNode arrayNode = (ArrayNode) node;
            for(int i = 0; i < arrayNode.size(); i++) {
                result.add(arrayNode.get(i));
            }
            return result;
        }
        else if(node.isObject()) {
            List<JsonNode> result = new ArrayList<>();

            ObjectNode objectNode = (ObjectNode) node;
            for(Iterator<String> it = objectNode.fieldNames(); it.hasNext(); ) {
                String key = it.next();

                ArrayNode arrayNode = JsonScript.jsonMapper().createArrayNode();
                arrayNode.add(key);
                arrayNode.add(objectNode.get(key));
                result.add(arrayNode);
            }
            return result;
        }
        return null;
    }
}
