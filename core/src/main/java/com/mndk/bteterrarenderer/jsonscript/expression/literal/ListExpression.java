package com.mndk.bteterrarenderer.jsonscript.expression.literal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mndk.bteterrarenderer.jsonscript.JsonScript;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.expression.*;

import javax.annotation.Nonnull;
import java.util.List;

@JsonDeserialize
public class ListExpression extends JsonExpression {

    private static final ExpressionCallerInfo INFO = new ExpressionCallerInfo(ListExpression.class);

    private final List<JsonExpression> values;

    @JsonCreator
    public ListExpression(List<JsonExpression> values) {
        this.values = values;
    }

    @Nonnull
    @Override
    protected ExpressionResult runInternal(JsonScriptRuntime runtime) {
        ArrayNode arrayResult = JsonScript.jsonMapper().createArrayNode();

        for(int i = 0; i < this.values.size(); i++) {
            ExpressionCallerInfo info = INFO.add("expression #" + i);

            ResultTransformer.JNode transformer = this.values.get(i).run(runtime, info)
                    .transformer()
                    .asJsonValue(ErrorMessages.valueMustBeJson("value"), info)
                    .asNode();
            if(transformer.isBreakType()) return transformer.getResult();

            JsonNode node = transformer.getWrapped();
            arrayResult.add(node);
        }

        return ExpressionResult.ok(arrayResult);
    }
}
