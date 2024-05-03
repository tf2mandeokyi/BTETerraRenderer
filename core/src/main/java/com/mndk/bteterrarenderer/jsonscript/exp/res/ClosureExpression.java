package com.mndk.bteterrarenderer.jsonscript.exp.res;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionCallerInfo;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.exp.JsonExpression;

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
            runtime.pushScope(null, runtime.getCurrentScope());
            ExpressionResult result = ExpressionResult.ok();

            int i = 1;
            for (JsonExpression expression : this.parameters) {
                ExpressionCallerInfo info = new ExpressionCallerInfo(this, "expression #" + i);
                result = expression.run(runtime, info);
                if(result.isBreakType()) break;
                i++;
            }
            return result;
        }
        finally {
            runtime.popScope();
        }
    }
}
