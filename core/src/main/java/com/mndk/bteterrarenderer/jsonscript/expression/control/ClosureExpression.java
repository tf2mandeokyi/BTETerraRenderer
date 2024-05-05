package com.mndk.bteterrarenderer.jsonscript.expression.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionCallerInfo;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.expression.JsonExpression;

import javax.annotation.Nonnull;
import java.util.List;

@JsonDeserialize
public class ClosureExpression extends JsonExpression {

    private final List<JsonExpression> parameters;

    @JsonCreator
    public ClosureExpression(List<JsonExpression> parameters) {
        this.parameters = parameters;
    }

    @Nonnull
    @Override
    public ExpressionResult runInternal(JsonScriptRuntime runtime) {
        try {
            runtime.pushScope();
            ExpressionResult result = ExpressionResult.ok();

            List<JsonExpression> jsonExpressions = this.parameters;
            for(int i = 0; i < jsonExpressions.size(); i++) {
                ExpressionCallerInfo info = new ExpressionCallerInfo(this, "expression #" + (i+1));

                JsonExpression expression = jsonExpressions.get(i);
                result = expression.run(runtime, info);
                if (result.isBreakType()) break;
            }
            return result;
        }
        finally {
            runtime.popScope();
        }
    }
}
